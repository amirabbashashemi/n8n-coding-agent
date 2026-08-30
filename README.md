# API Documentation

## Overview
This document provides an overview of the API endpoints available in the application. Each endpoint is described with its purpose, request type, expected parameters, and response.

### Endpoints

#### POST /api/messages
- **Purpose**: Send a new message to the server.
- **Request Body**: Must contain a JSON object with the message. If the body is empty, a 400 Bad Request response is returned.
  - Example: `{ "message": "Hello, world!" }`
- **Response**: 
  - **201 Created**: If the message is successfully stored.
  - **400 Bad Request**: If the request body is empty.

#### GET /api/messages
- **Purpose**: Retrieve all messages stored on the server.
- **Response**: 
  - **200 OK**: Returns a JSON array of messages.

#### DELETE /api/messages
- **Purpose**: Clear all stored messages.
- **Response**: 
  - **204 No Content**: Indicates that messages were successfully cleared and there is no content to return.

## Notes
Ensure that the server is running before trying to access these endpoints. Use an appropriate tool like Postman or curl to test the API calls.