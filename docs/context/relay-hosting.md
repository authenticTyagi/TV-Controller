# Relay hosting

Need: always-on process holding a WebSocket, HTTPS/WSS, small persistent volume. Findings from a web search on 2026-10-29
(vendor blogs, so verify on each pricing page):

| Option | Cost | Always on? | Note |
|---|---|---|---|
| Northflank free | $0 | yes (per one source) | card verification; chosen first try |
| Koyeb free | $0 | sources disagree | card verification |
| Render free | $0 | no, sleeps after 15 min idle | would drop the TV connection |
| Railway | one-time $5 credit | while credit lasts | no permanent free plan |
| Small VPS | ~$4-5/mo (not searched) | yes | self-managed |
| Vercel | — | no | cannot hold persistent connections |
Oracle Always Free and Fly.io free terms were not confirmed.

## Northflank progress (as of end of 2026-10-29)
- Account created by the user via GitHub sign-up; team "authenticTyagi's team" (Free), project "Default".
- "Create service" form open: name `controller`, Combined (build and deploy a Git repo). Banner says no version-control
  platform linked yet. Form notes a card is required at the end.
- Blocked on: user approving the GitHub OAuth link (restrict to `TV-Controller`) and adding a card.

## Remaining steps
1. Link GitHub (user). 2. Source: repo `TV-Controller`, branch `main`. 3. Build: Dockerfile, build context `/server`,
Dockerfile location `/server/Dockerfile` (check the field when the form is open). 4. Port 8080, HTTP, public.
5. Env vars `TV_TOKEN`, `VIEWER_PASSWORD` (user-chosen, long random). 6. Volume mounted at `/data`.
7. Copy the public URL; TV app uses `wss://<host>`; dashboard is `https://<host>`.
8. Check the service stays up and the TV connection survives > 1 hour.
