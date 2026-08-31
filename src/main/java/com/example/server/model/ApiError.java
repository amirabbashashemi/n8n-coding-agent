package com.example.server.model;

/**
 * Standard JSON representation of an error returned by the API.
 *
 * @param error stable, machine-readable error code
 * @param message human-readable explanation intended for the client
 */
public record ApiError(String error, String message) {
}
