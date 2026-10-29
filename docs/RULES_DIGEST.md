# Rules digest (summary of ../CLAUDE.md — CLAUDE.md wins)

1. Repo is public: no real tokens, passwords, URLs, TV IP, card data.
2. No account creation, card entry or OAuth grants by Claude; ask the user. Ask before outward actions.
3. Say only what has been tested. Relay = tested; app, dashboard-in-browser, camera = untested.
4. Protocol change = server + TV app + dashboard + ARCHITECTURE.md together.
5. Camera: live only, never stored, no audio, parent-triggered, children informed, on-TV indicator.
6. Relay deps: express + ws only, unless a decision is logged.
7. Android: targetSdk 28, minSdk 24; re-test on the TV before changing.
8. Update TASKS, DECISIONS and context/session-state.md whenever behaviour changes or a session ends.
9. Match existing code style, sparse comments.
10. Windows shell: quote the project path (contains a space); use the Write tool for multi-line files.
