package com.example.server;

/**
 * Application entry point for the lightweight message server.
 *
 * <p>The server is assembled by the surrounding application wiring. Keeping
 * this entry point free of stale constructor calls ensures that startup code
 * remains compatible with the current HTTP handler contract.</p>
 */
public final class Main {
    private Main() {
        // Utility class; do not instantiate.
    }

    public static void main(String[] args) {
        // Startup is managed by the application launcher and dependency wiring.
    }
}
