import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpContext;

public class SimpleApiServer {
    private static List<String> messages = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        HttpContext context = server.createContext("/api/messages", new MessageHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000...");
    }

    static class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "";
            switch (exchange.getRequestMethod()) {
                case "GET":
                    response = String.join("\n", messages);
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.close();
                    break;
                case "POST":
                    String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines()
                                      .collect(java.util.stream.Collectors.joining("\n"));
                    if (body.isEmpty()) {
                        exchange.sendResponseHeaders(400, -1); // Bad Request
                    } else {
                        messages.add(body);
                        exchange.sendResponseHeaders(201, -1); // Created
                    }
                    exchange.close();
                    break;
                case "DELETE":
                    messages.clear();
                    exchange.sendResponseHeaders(204, -1); // No Content
                    exchange.close();
                    break;
                default:
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                    exchange.close();
            }
        }
    }
}