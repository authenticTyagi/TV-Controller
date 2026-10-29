# Privacy and consent

- The system observes children in the owner's own home. Tell the children what is monitored (apps, durations) and that a camera exists.
- Camera: on only when the parent presses Start; live relay, no storage, no audio. Show an on-TV indicator while streaming (T-12).
- Data held on the relay: app names, start/end times, daily limit. Messages and images are passed through, not stored.
- Access: one shared viewer password and one TV token. Use long random values, HTTPS only. Rotate if leaked.
- Public repo: contains no secrets. Keep it that way (CLAUDE.md rule 1).
- If the relay is ever shared with anyone else, add per-user accounts first (BACKLOG).
