This repository now includes PSEI innovation/support API stubs and models.

New endpoints (stubs)
- GET/POST /psei/ideas
- GET/POST /psei/patents
- GET /psei/schemes/search
- GET/POST /psei/protection/templates
- GET /psei/hub/mentors
- GET /psei/hub/investors
- GET /psei/hub/students

Use the OpenAPI file (docs/openapi.yaml) to generate clients or to import into Postman.

Notes about auth and signatures
- Endpoints are currently unauthenticated stubs. Recommended approach: JWT for API auth and simple role claims for users (creator, mentor, admin).
- Idea Vault evidence: recommend using asymmetric signatures (user signs payload with private key, server stores signature and public-key fingerprint). For higher trust, integrate with government eKYC or a trusted signer service.

