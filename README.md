## Simple API Server

This server provides a simple HTTP API to manage messages.

### Endpoints

- **POST /api/messages**: Add a new message. Returns 201 Created if successful, 400 Bad Request if the message is empty.
- **GET /api/messages**: Retrieve all messages. Returns 200 OK with messages separated by new lines.
- **DELETE /api/messages**: Clear all stored messages. Returns 204 No Content.