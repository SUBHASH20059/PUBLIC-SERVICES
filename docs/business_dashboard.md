Business Dashboard: sample requests and notes

Authentication: All dashboard endpoints should require authentication (JWT). The current skeleton uses unauthenticated stubs; implement JWT guards and role checks before production.

Sample curl (placeholder):

1) Get dashboard summary (replace <TOKEN>):

curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/dashboard/summary

2) Get business profile:

curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/businesses/123

3) Upload a document (placeholder multipart flow):

curl -X POST -H "Authorization: Bearer <TOKEN>" -F "file=@incorporation.pdf" http://localhost:8080/businesses/123/documents

Notes:
- Document upload should store files in encrypted storage (S3 with server-side KMS or local encrypted filesystem for dev).
- All sensitive reads should insert an audit_logs row to record actorId, action, and timestamp.
