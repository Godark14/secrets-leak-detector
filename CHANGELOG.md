<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# secrets-leak-detector Changelog

## [Unreleased]
### Added
- Initial secret detection inspection (`SecretLeakInspection`), applied to all text files
- Detection for AWS access/secret keys, GitHub tokens, Slack tokens, RSA/EC/OpenSSH private keys, and generic API keys/access tokens
- False-positive reduction: placeholder value filtering, repeated-character detection, and Shannon entropy analysis on generic API key matches
- Path-based exclusions (`node_modules`, `build`, `.gradle`, `.git`, `.idea`, `dist`, `out`) and VCS-ignored file exclusion
- Unit test suite covering detection and false-positive filtering

### Removed
- Template scaffold sample code (tool window, startup activity, sample project service)