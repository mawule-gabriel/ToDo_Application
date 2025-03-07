package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.InetSocketAddress;

public class Todo {

    // List to store the tasks
    private static final List<Task> tasks = new ArrayList<>();
    private static int nextId = 1;

    static class Task {
        int id;
        String content;
        boolean completed;

        Task(int id, String content) {
            this.id = id;
            this.content = content;
            this.completed = false;
        }
    }

    public static void main(String[] args) throws Exception {
        // Use port 5000 as default or use the PORT environment variable
        String port = System.getenv("PORT");
        int serverPort = (port != null && !port.isEmpty()) ? Integer.parseInt(port) : 5000;

        // Create HTTP server listening on the specified port
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/", new TodoHandler());
        // Handle task toggling
        server.createContext("/toggle", new ToggleTaskHandler());
        // Handle task deletion
        server.createContext("/delete", new DeleteTaskHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:" + serverPort + "/");
    }

    // Parse URL encoded parameters
    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> parameters = new HashMap<>();
        if (formData != null && !formData.isEmpty()) {
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
                parameters.put(key, value);
            }
        }
        return parameters;
    }

    // Extract form data from HTTP request
    private static String extractFormData(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder formData = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            formData.append(line);
        }
        return formData.toString();
    }

    // Handler for toggling task completion status
    static class ToggleTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String formData = extractFormData(exchange);
                Map<String, String> parameters = parseFormData(formData);

                if (parameters.containsKey("id")) {
                    int id = Integer.parseInt(parameters.get("id"));
                    for (Task task : tasks) {
                        if (task.id == id) {
                            task.completed = !task.completed;
                            break;
                        }
                    }
                }

                // Redirect back to the main page
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
            }
        }
    }

    // Handler for deleting tasks
    static class DeleteTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String formData = extractFormData(exchange);
                Map<String, String> parameters = parseFormData(formData);

                if (parameters.containsKey("id")) {
                    int id = Integer.parseInt(parameters.get("id"));
                    tasks.removeIf(task -> task.id == id);
                }

                // Redirect back to the main page
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
            }
        }
    }

    // Main handler to manage the requests
    static class TodoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Get the request method
            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) {
                // Process the form submission (add a new task)
                handlePostRequest(exchange);
            } else {
                // Handle GET request to show the To-Do list
                handleGetRequest(exchange);
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            // Read the task from the form submission
            String formData = extractFormData(exchange);
            Map<String, String> parameters = parseFormData(formData);

            if (parameters.containsKey("task") && !parameters.get("task").trim().isEmpty()) {
                String taskContent = parameters.get("task");
                // Add the task to the list
                tasks.add(new Task(nextId++, taskContent));
            }

            // Respond with the updated task list (reload the page)
            exchange.getResponseHeaders().set("Location", "/");
            // Redirect to the main page
            exchange.sendResponseHeaders(303, -1);
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            // HTML structure for the To-Do list page
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>");
            html.append("<html lang='en'>");
            html.append("<head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            html.append("<title>MY AWS LAB</title>");
            html.append("<style>");
            html.append("* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }");
            html.append("body { background-color: #f5f7fa; color: #333; line-height: 1.6; padding: 20px; }");
            html.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); padding: 30px; }");
            html.append("h1 { color: #2c3e50; text-align: center; margin-bottom: 20px; font-weight: 600; }");
            html.append(".input-group { display: flex; margin-bottom: 25px; }");
            html.append("input[type='text'] { flex-grow: 1; padding: 12px 16px; border: 1px solid #ddd; border-radius: 6px 0 0 6px; font-size: 16px; outline: none; transition: border 0.3s; }");
            html.append("input[type='text']:focus { border-color: #3498db; box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2); }");
            html.append(".add-btn { background: #3498db; color: white; border: none; padding: 12px 20px; border-radius: 0 6px 6px 0; cursor: pointer; font-size: 16px; transition: background 0.3s; }");
            html.append(".add-btn:hover { background: #2980b9; }");
            html.append(".task-list { list-style-type: none; }");
            html.append(".task-item { display: flex; align-items: center; padding: 16px; border-bottom: 1px solid #eee; animation: fadeIn 0.3s; }");
            html.append(".task-item:last-child { border-bottom: none; }");
            html.append(".task-checkbox { margin-right: 12px; height: 20px; width: 20px; }");
            html.append(".task-content { flex-grow: 1; font-size: 16px; transition: color 0.3s, text-decoration 0.3s; }");
            html.append(".task-completed { color: #95a5a6; text-decoration: line-through; }");
            html.append(".task-actions { display: flex; gap: 8px; }");
            html.append(".task-toggle-btn, .task-delete-btn { background: none; border: none; cursor: pointer; padding: 4px; border-radius: 4px; transition: background 0.2s; }");
            html.append(".task-toggle-btn { color: #27ae60; }");
            html.append(".task-delete-btn { color: #e74c3c; }");
            html.append(".task-toggle-btn:hover, .task-delete-btn:hover { background: #f5f5f5; }");
            html.append(".empty-state { text-align: center; margin-top: 30px; color: #95a5a6; font-style: italic; }");
            html.append("@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }");
            html.append("</style>");
            html.append("</head>");
            html.append("<body>");
            html.append("<div class='container'>");
            html.append("<h1>My To-Do List</h1>");

            // Form for adding new tasks
            html.append("<form method='POST' action='/'>");
            html.append("<div class='input-group'>");
            html.append("<input type='text' name='task' placeholder='What needs to be done?' required autofocus>");
            html.append("<button type='submit' class='add-btn'>Add</button>");
            html.append("</div>");
            html.append("</form>");

            // Task list
            html.append("<ul class='task-list'>");

            // Display each task in the list
            if (tasks.isEmpty()) {
                html.append("<div class='empty-state'>Your to-do list is empty. Add a task to get started!</div>");
            } else {
                for (Task task : tasks) {
                    html.append("<li class='task-item'>");

                    // Task content
                    html.append("<span class='task-content ").append(task.completed ? "task-completed" : "").append("'>");
                    html.append(task.content);
                    html.append("</span>");

                    // Task actions
                    html.append("<div class='task-actions'>");

                    // Toggle completion form
                    html.append("<form method='POST' action='/toggle' style='display:inline;'>");
                    html.append("<input type='hidden' name='id' value='").append(task.id).append("'>");
                    html.append("<button type='submit' class='task-toggle-btn' title='").append(task.completed ? "Mark as incomplete" : "Mark as complete").append("'>");
                    html.append(task.completed ? "↩️" : "✅");
                    html.append("</button>");
                    html.append("</form>");

                    // Delete task form
                    html.append("<form method='POST' action='/delete' style='display:inline;'>");
                    html.append("<input type='hidden' name='id' value='").append(task.id).append("'>");
                    html.append("<button type='submit' class='task-delete-btn' title='Delete task'>🗑️</button>");
                    html.append("</form>");

                    html.append("</div>");
                    html.append("</li>");
                }
            }

            html.append("</ul>");
            html.append("</div>");

            // Respond with the HTML content
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] responseBytes = html.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }
}