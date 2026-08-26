<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# secrets-leak-detector Changelog

## [Unreleased]
### Added
- Initial secret detection inspection (`SecretLeakInspection`), applied to all text files
- Detection for AWS access/secret keys, GitHub tokens, Slack tokens, RSA/EC/OpenSSH private keys, and generic API keys/access tokens
- Detection for Google Cloud API keys, Google service account private keys, Stripe live secret keys, JWTs, and database connection strings with plaintext passwords
- "Replace with placeholder" quick fix to redact a detected secret in one click
- False-positive reduction: placeholder value filtering, repeated-character detection, and Shannon entropy analysis on generic API key matches
- Path-based exclusions (`node_modules`, `build`, `.gradle`, `.git`, `.idea`, `dist`, `out`) and VCS-ignored file exclusion
- Plugin icon (light and dark theme variants)
- Unit test suite covering detection, quick fix, and false-positive filtering

### Removed
- Template scaffold sample code (tool window, startup activity, sample project service)