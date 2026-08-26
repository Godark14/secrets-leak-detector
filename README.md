# Secrets Leak Detector

![Build](https://github.com/Godark14/secrets-leak-detector/workflows/Build/badge.svg)

An IntelliJ Platform plugin that scans your files and flags hardcoded secrets — API keys, tokens, and private keys — directly in the editor, before they end up in a commit.

## What it detects

- AWS access keys and secret keys
- GitHub personal access tokens
- Slack tokens
- Google Cloud API keys and service account private keys
- Stripe live secret/restricted keys
- JSON Web Tokens (JWT)
- Database connection strings with a plaintext password (PostgreSQL, MySQL, MongoDB, Redis)
- RSA/EC/OpenSSH private keys
- Generic API keys and access tokens (using keyword matching + entropy analysis to reduce false positives)

The inspection runs on **all text files**, not just source code — so secrets in `.env`, `.yml`, `.properties`, and config files are caught too.

## Quick fix

Each detected secret comes with a one-click "Replace with placeholder" quick fix (Alt+Enter) that swaps the value for `REDACTED_SECRET` directly in the editor.

## Reducing false positives

To keep noise low, the inspection skips:
- Common placeholder values (`YOUR_API_KEY_HERE`, `changeme`, `example`, etc.)
- Repeated-character strings (`xxxxxxxx`, `00000000`)
- Low-entropy values that look more like dictionary words than random secrets
- Files under `node_modules/`, `build/`, `.git/`, `.gradle/`, `.idea/`, `dist/`, and `out/`
- Files ignored by your project's VCS (e.g. matched by `.gitignore`)

## Installation

- Using the IDE built-in plugin system:

  Settings/Preferences > Plugins > Marketplace > Search for "Secrets Leak Detector" > Install

- Manually:

  Download the [latest release](https://github.com/Godark14/secrets-leak-detector/releases) and install it via Settings/Preferences > Plugins > Install plugin from disk...

## Contributing

Issues and pull requests are welcome. This project is built on the [IntelliJ Platform Plugin Template][template].

## License

[MIT](./LICENSE)

[template]: https://github.com/JetBrains/intellij-platform-plugin-template