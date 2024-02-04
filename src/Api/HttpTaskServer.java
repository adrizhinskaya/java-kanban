package Api;

import Models.Epic;
import Models.Subtask;
import Models.Task;
import Services.Managers;
import Services.TaskManager;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final HttpServer server;
    private final Gson gson;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = Managers.getGson();
        this.server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/tasks", new PrioritizedTasksHandler());
        server.createContext("/tasks/history", new HistoryHandler());
        server.createContext("/tasks/task", new TasksHandler());
        server.createContext("/tasks/subtask", new SubtasksHandler());
        server.createContext("/tasks/epic", new EpicsHandler());
    }

    private class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                String path = httpExchange.getRequestURI().toString();
                String requestMethod = httpExchange.getRequestMethod();
                if (requestMethod.equals("GET") && Pattern.matches("^/tasks/history$", path)) {
                    String response = gson.toJson(taskManager.getHistory());
                    sendText(httpExchange, response);
                } else {
                    System.out.println("Принимается GET запрос. А получили - " + requestMethod);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpExchange.close();
            }
        }
    }

    private class PrioritizedTasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                String path = httpExchange.getRequestURI().toString();
                String requestMethod = httpExchange.getRequestMethod();
                if (requestMethod.equals("GET") && Pattern.matches("^/tasks$", path)) {
                    String response = gson.toJson(taskManager.getPrioritizedTasks());
                    sendText(httpExchange, response);
                } else {
                    System.out.println("Принимается GET запрос. А получили - " + requestMethod);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpExchange.close();
            }
        }
    }

    private class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                String path = httpExchange.getRequestURI().toString();
                String requestMethod = httpExchange.getRequestMethod();
                switch (requestMethod) {
                    case "GET": {
                        if (Pattern.matches("^/tasks/task$", path)) { // Пoлучение всех tasks
                            String response = gson.toJson(taskManager.getAllTasks());
                            sendText(httpExchange, response);
                            break;
                        }

                        if (Pattern.matches("^/tasks/task/\\?Id=\\d+$", path)) { // Получение по Id
                            String pathId = path.replaceFirst("/tasks/task/\\?Id=", "");
                            int taskId = parsePathId(pathId);
                            if (taskId != -1) {
                                String response = gson.toJson(taskManager.getTaskById(taskId));
                                sendText(httpExchange, response);
                            } else {
                                System.out.println("ID задачи " + taskId);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    }
                    case "DELETE": {
                        if (Pattern.matches("^/tasks/task$", path)) { // Удаление всех tasks
                            taskManager.removeAllTasks();
                            System.out.println("Удалены все задачи");
                            httpExchange.sendResponseHeaders(200, 0);
                            break;
                        }

                        if (Pattern.matches("^/tasks/task/\\?Id=\\d+$", path)) { // Удаление по Id
                            String pathId = path.replaceFirst("/tasks/task/\\?Id=", "");
                            int taskId = parsePathId(pathId);
                            if (taskId != -1) {
                                taskManager.removeTaskById(taskId);
                                System.out.println("Удалена задача с id: " + taskId);
                                httpExchange.sendResponseHeaders(200, 0);
                            } else {
                                System.out.println("ID задачи " + taskId);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    }
                    case "POST": {
                        if (Pattern.matches("^/tasks/task$", path)) {
                            String body = readText(httpExchange);
                            Task task = gson.fromJson(body, Task.class);
                            taskManager.updateTask(task);
                            System.out.println("Сериализована и создана/обновлена task");
                            httpExchange.sendResponseHeaders(201, 0);
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    }
                    default: {
                        System.out.println("Принимаются GET/DELETE/POST запросы. А получили - " + requestMethod);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpExchange.close();
            }
        }
    }

    private class SubtasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                String path = httpExchange.getRequestURI().toString();
                String requestMethod = httpExchange.getRequestMethod();
                switch (requestMethod) {
                    case "GET": {
                        if (Pattern.matches("^/tasks/subtask$", path)) {
                            String response = gson.toJson(taskManager.getAllSubtasks());
                            sendText(httpExchange, response);
                            break;
                        }
                        if (Pattern.matches("^/tasks/subtask/epic/\\?Id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/subtask/epic/\\?Id=", "");
                            int epicId = parsePathId(pathId);
                            if (epicId != -1) {
                                String response = gson.toJson(taskManager.getEpicById(epicId).getSubtasks());
                                sendText(httpExchange, response);
                            } else {
                                System.out.println("ID задачи " + epicId);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        }

                        if (Pattern.matches("^/tasks/subtask/\\?Id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/subtask/\\?Id=", "");
                            int taskId = parsePathId(pathId);
                            if (taskId != -1) {
                                String response = gson.toJson(taskManager.getSubtaskById(taskId));
                                sendText(httpExchange, response);
                            } else {
                                System.out.println("ID задачи " + taskId);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    }
                    case "DELETE": {
                        if (Pattern.matches("^/tasks/subtask$", path)) {
                            taskManager.removeAllSubtasks();
                            System.out.println("Удалены все подзадачи");
                            httpExchange.sendResponseHeaders(200, 0);
                            break;
                        }

                        if (Pattern.matches("^/tasks/subtask/\\?Id=\\d+$", path)) { // Удаление по Id
                            String pathId = path.replaceFirst("/tasks/subtask/\\?Id=", "");
                            int taskId = parsePathId(pathId);
                            if (taskId != -1) {
                                taskManager.removeSubtaskById(taskId);
                                System.out.println("Удалена подзадача с id: " + taskId);
                                httpExchange.sendResponseHeaders(200, 0);
                            } else {
                                System.out.println("ID задачи " + taskId);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    }
                    case "POST": {
                        if (Pattern.matches("^/tasks/subtask$", path)) {
                            String body = readText(httpExchange);
                            Subtask subtask = gson.fromJson(body, Subtask.class);
                            taskManager.updateSubtask(subtask);
                            System.out.println("Сериализована и создана/обновлена subtask");
                            httpExchange.sendResponseHeaders(201, 0);
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    }
                    default: {
                        System.out.println("Принимаются GET/DELETE/POST запросы. А получили - " + requestMethod);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpExchange.close();
            }
        }
    }

    private class EpicsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                String path = httpExchange.getRequestURI().toString();
                String requestMethod = httpExchange.getRequestMethod();
                switch (requestMethod) {
                    case "GET": {
                        if (Pattern.matches("^/tasks/epic$", path)) {
                            String response = gson.toJson(taskManager.getAllEpics());
                            sendText(httpExchange, response);
                            break;
                        }

                        if (Pattern.matches("^/tasks/epic/\\?Id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/epic/\\?Id=", "");
                            int taskId = parsePathId(pathId);
                            if (taskId != -1) {
                                String response = gson.toJson(taskManager.getEpicById(taskId));
                                sendText(httpExchange, response);
                            } else {
                                System.out.println("ID задачи " + taskId);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    }
                    case "DELETE": {
                        if (Pattern.matches("^/tasks/epic$", path)) {
                            taskManager.removeAllEpics();
                            System.out.println("Удалены все эпики");
                            httpExchange.sendResponseHeaders(200, 0);
                            break;
                        }

                        if (Pattern.matches("^/tasks/epic/\\?Id=\\d+$", path)) {
                            String pathId = path.replaceFirst("/tasks/epic/\\?Id=", "");
                            int taskId = parsePathId(pathId);
                            if (taskId != -1) {
                                taskManager.removeEpicById(taskId);
                                System.out.println("Удален эпик с id: " + taskId);
                                httpExchange.sendResponseHeaders(200, 0);
                            } else {
                                System.out.println("ID задачи " + taskId);
                                httpExchange.sendResponseHeaders(405, 0);
                            }
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    }
                    case "POST": {
                        if (Pattern.matches("^/tasks/epic$", path)) {
                            String body = readText(httpExchange);
                            Epic epic = gson.fromJson(body, Epic.class);
                            taskManager.updateEpic(epic);
                            System.out.println("Сериализован и создан/обновлен epic");
                            httpExchange.sendResponseHeaders(201, 0);
                        } else {
                            httpExchange.sendResponseHeaders(405, 0);
                        }
                        break;
                    }
                    default: {
                        System.out.println("Принимаются GET/DELETE/POST запросы. А получили - " + requestMethod);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpExchange.close();
            }
        }
    }

    private int parsePathId(String pathId) {
        try {
            return Integer.parseInt(pathId);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Открой в браузере http://localhost:" + PORT + "/");
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Остановили сервер на порту " + PORT);
    }

    protected String readText(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }
}
