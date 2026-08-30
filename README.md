# API Documentation

## Overview
This document provides detailed descriptions for each API method available in the Simple API Server.

## Endpoints

### 1. POST /api/messages
- **Description**: This endpoint allows clients to send a new message to the server.
- **Request Body**: A JSON object containing the message.  
  Example:
  ```json
  { "message": "Hello, world!" }
  ```
- **Responses**:
  - **201 Created**: The message was successfully created.
  - **400 Bad Request**: Returned if the request body is empty.

### 2. GET /api/messages
- **Description**: This endpoint retrieves all stored messages from the server.
- **Responses**:
  - **200 OK**: A JSON array containing all messages.  
  Example:
  ```json
  ["Hello, world!", "Another message"]
  ```

### 3. DELETE /api/messages
- **Description**: This endpoint allows clients to delete all stored messages.
- **Responses**:
  - **204 No Content**: All messages were successfully deleted.

## Usage
To interact with these endpoints, use a REST client or command line tools like curl.
