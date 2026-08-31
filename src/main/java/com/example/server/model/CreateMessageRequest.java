package com.example.server.model;

/**
 * Request payload used to create a message.
 *
 * <p>The HTTP handler is responsible for validating that each value is
 * present and contains non-whitespace text before passing it to the service.
 * A no-argument constructor and JavaBean accessors keep this DTO compatible
 * with Jackson's default deserialization.</p>
 */
public class CreateMessageRequest {
    private String author;
    private String content;
    private String category;

    public CreateMessageRequest() {
        // Required by Jackson for property-based deserialization.
    }

    public CreateMessageRequest(String author, String content, String category) {
        this.author = author;
        this.content = content;
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
