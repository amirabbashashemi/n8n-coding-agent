package com.example.server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Dispatches incoming message operations to the configured service boundary.
 *
 * <p>The service contract is represented as alternating operation names and
 * values.  Keeping the dispatch method variadic also allows messages with an
 * optional number of fields to be handled without unsafe casts at call sites.
 */
public final class MessageHandler {
    private final Service service;

    /** Creates a handler backed by the default local service implementation. */
    public MessageHandler() {
        this((operation, arguments) -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("operation", operation);
            result.put("arguments", arguments);
            return result;
        });
    }

    /** Creates a handler backed by the supplied service. */
    public MessageHandler(Service service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    /**
     * Invokes a service with a sequence of name/value pairs.
     *
     * @param operation service operation name
     * @param arguments alternating argument names and values
     * @return the service response
     */
    public Object invokeService(String operation, Object... arguments) {
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("operation must not be blank");
        }
        if ((arguments.length & 1) != 0) {
            throw new IllegalArgumentException("service arguments must be name/value pairs");
        }
        Map<String, Object> namedArguments = new LinkedHashMap<>();
        for (int i = 0; i < arguments.length; i += 2) {
            Object name = arguments[i];
            if (!(name instanceof String key) || key.isBlank()) {
                throw new IllegalArgumentException("argument names must be non-blank strings");
            }
            namedArguments.put(key, arguments[i + 1]);
        }
        return service.invoke(operation, Map.copyOf(namedArguments));
    }

    /** Explicit eight-parameter overload retained for strongly typed callers. */
    public Object invokeService(String operation, Object name1, Object value1,
                                String name2, Object value2, String name3, Object value3,
                                String name4, Object value4) {
        return invokeService(operation, name1, value1, name2, value2, name3, value3, name4, value4);
    }

    /** Handles a message represented by an operation and name/value arguments. */
    public Object handle(String operation, Object... arguments) {
        return invokeService(operation, arguments);
    }

    /** Alias used by transports that call their callback {@code onMessage}. */
    public Object onMessage(String operation, Object... arguments) {
        return handle(operation, arguments);
    }

    @FunctionalInterface
    public interface Service {
        Object invoke(String operation, Map<String, Object> arguments);
    }
}
