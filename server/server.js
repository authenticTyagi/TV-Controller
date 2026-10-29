// Relay between the TV app and the parent's browser.
// TV  <--ws-->  relay  <--ws/http-->  dashboard
const fs = require('fs');
const path = require('path');
const http = require('http');
const express = require('express');
const { WebSocketServer } = require('ws');

const PORT = process.env.PORT || 8080;
const TV_TOKEN = process.env.TV_TOKEN || 'change-me-tv';
const VIEWER_PASSWORD = process.env.VIEWER_PASSWORD || 'change-me-viewer';
const DATA_FILE = process.env.DATA_FILE || path.join(__dirname, 'data.json');

let db = { sessions: [], limitMinutes: 0 };
try { db = { ...db, ...JSON.parse(fs.readFileSync(DATA_FILE, 'utf8')) }; } catch {}
const save = () => fs.writeFile(DATA_FILE, JSON.stringify(db), () => {});

let tv = null;            // the connected TV socket
let now = null;           // what the TV is showing right now
let cameraWanted = false;
const viewers = new Set();

const app = express();
app.use(express.json({ limit: '4mb' }));
app.use(express.static(path.join(__dirname, 'public')));

const auth = (req, res, next) => {
  if (req.get('x-password') === VIEWER_PASSWORD) return next();
  res.status(401).json({ error: 'unauthorized' });
};

const sendTv = (obj) => {
  if (tv && tv.readyState === 1) { tv.send(JSON.stringify(obj)); return true; }
  return false;
};
const toViewers = (data, binary = false) =>
  viewers.forEach((v) => v.readyState === 1 && v.send(data, { binary }));

app.get('/api/status', auth, (req, res) =>
  res.json({ tvOnline: !!tv, now, limitMinutes: db.limitMinutes, cameraOn: cameraWanted }));

app.get('/api/usage', auth, (req, res) => {
  const days = Math.min(parseInt(req.query.days || '1', 10), 30);
  const since = new Date(); since.setHours(0, 0, 0, 0); since.setDate(since.getDate() - (days - 1));
  res.json(db.sessions.filter((s) => s.end >= since.getTime()));
});

app.post('/api/message', auth, (req, res) => {
  const { text = '', image = null, seconds = 15 } = req.body;
  if (!text && !image) return res.status(400).json({ error: 'empty' });
  res.json({ delivered: sendTv({ type: 'message', text, image, seconds }) });
});

app.post('/api/limit', auth, (req, res) => {
  db.limitMinutes = Math.max(0, parseInt(req.body.minutes || 0, 10));
  save(); sendTv({ type: 'limit', minutes: db.limitMinutes });
  res.json({ ok: true, limitMinutes: db.limitMinutes });
});

app.post('/api/camera', auth, (req, res) => {
  cameraWanted = !!req.body.on;
  res.json({ delivered: sendTv({ type: 'camera', on: cameraWanted }) });
});

const server = http.createServer(app);
const wss = new WebSocketServer({ noServer: true, maxPayload: 8 * 1024 * 1024 });

server.on('upgrade', (req, socket, head) => {
  const url = new URL(req.url, 'http://x');
  const role = url.pathname === '/tv' ? 'tv' : url.pathname === '/viewer' ? 'viewer' : null;
  const ok = (role === 'tv' && url.searchParams.get('token') === TV_TOKEN) ||
             (role === 'viewer' && url.searchParams.get('password') === VIEWER_PASSWORD);
  if (!ok) { socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n'); return socket.destroy(); }
  wss.handleUpgrade(req, socket, head, (ws) => wss.emit('connection', ws, role));
});

wss.on('connection', (ws, role) => {
  if (role === 'viewer') {
    viewers.add(ws);
    ws.send(JSON.stringify({ type: 'status', tvOnline: !!tv, now }));
    ws.on('close', () => viewers.delete(ws));
    return;
  }
  if (tv) tv.close();
  tv = ws;
  toViewers(JSON.stringify({ type: 'status', tvOnline: true, now }));
  sendTv({ type: 'limit', minutes: db.limitMinutes });
  sendTv({ type: 'camera', on: cameraWanted });

  ws.on('message', (data, isBinary) => {
    if (isBinary) return toViewers(data, true);       // camera frame (JPEG)
    let m; try { m = JSON.parse(data.toString()); } catch { return; }
    if (m.type === 'session') {                       // finished viewing session
      db.sessions.push({ pkg: m.pkg, label: m.label, start: m.start, end: m.end });
      db.sessions = db.sessions.slice(-20000); save();
    } else if (m.type === 'now') {
      now = m.pkg ? { pkg: m.pkg, label: m.label, since: m.since } : null;
      toViewers(JSON.stringify({ type: 'status', tvOnline: true, now }));
    } else if (m.type === 'camera-status') {
      if (!m.ok) cameraWanted = false;
      toViewers(JSON.stringify(m));
    }
  });
  ws.on('close', () => {
    if (tv === ws) { tv = null; now = null; toViewers(JSON.stringify({ type: 'status', tvOnline: false, now: null })); }
  });
});

server.listen(PORT, () => console.log(`relay listening on :${PORT}`));
