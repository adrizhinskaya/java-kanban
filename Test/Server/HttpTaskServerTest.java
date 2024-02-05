package Server;

import Models.Epic;
import Models.Status;
import Models.Subtask;
import Models.Task;
import Server.HttpTaskServer;
import Services.InMemory.InMemoryHistoryManager;
import Services.InMemory.InMemoryTaskManager;
import Services.Managers;
import Services.InMemory.TaskManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpTaskServerTest {
    private HttpTaskServer taskServer;
    private TaskManager taskManager;
    private final Gson gson = Managers.getGson();

    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void init() throws IOException {
        taskManager = new InMemoryTaskManager(new InMemoryHistoryManager());
        taskServer = new HttpTaskServer(taskManager);

        task = new Task("taskName", "taskDescription", Status.IN_PROGRESS,
                LocalDateTime.of(2024, 2,2,0,0),
                Duration.ofMinutes(60));
        taskManager.createTask(task);

        epic = new Epic("epicName", "epicDescription");
        taskManager.createEpic(epic);

        subtask = new Subtask("subtaskName", "subtaskDescription", Status.IN_PROGRESS, epic.getId(),
                LocalDateTime.of(2024, 3,3,0,0),
                Duration.ofMinutes(120));
        taskManager.createSubtask(subtask);

        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void getPrioritizedTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<List<Task>>() {}.getType();
        List<Task> actual = gson.fromJson(response.body(), taskType);

        assertNotNull(actual, "PrioritizedTasks не возвращаются");
        assertEquals(2, actual.size(), "Неверное количество PrioritizedTasks");
        assertEquals(task, actual.get(0), "PrioritizedTasks не совпадают");
    }

    @Test
    void getHistory() throws IOException, InterruptedException {
        taskManager.getTaskById(task.getId());
        taskManager.getSubtaskById(subtask.getId());
        taskManager.getEpicById(epic.getId());
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<List<Task>>() {}.getType();
        List<Task> actual = gson.fromJson(response.body(), taskType);

        assertNotNull(actual, "History не возвращается");
        assertEquals(3, actual.size(), "Неверное количество элементов History");
        assertEquals(task, actual.get(0), "History элементы не совпадают");
    }

    @Test
    void getTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<HashMap<Integer, Task>>() {}.getType();
        HashMap<Integer, Task> actual = gson.fromJson(response.body(), taskType);

        assertNotNull(actual, "Tasks не возвращаются");
        assertEquals(1, actual.size(), "Неверное количество tasks");
        assertEquals(task, actual.get(task.getId()), "Tasks не совпадают");
    }

    @Test
    void getSubtasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<HashMap<Integer, Subtask>>() {}.getType();
        HashMap<Integer, Subtask> actual = gson.fromJson(response.body(), taskType);

        assertNotNull(actual, "Subtasks не возвращаются");
        assertEquals(1, actual.size(), "Неверное количество subtasks");
        assertEquals(subtask, actual.get(subtask.getId()), "Subtasks не совпадают");
    }

    @Test
    void getEpicSubtasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/subtask/epic/?Id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<List<Task>>() {}.getType();
        List<Task> actual = gson.fromJson(response.body(), taskType);

        assertNotNull(actual, "Подзадачи не возвращаются");
        assertEquals(1, actual.size(), "Неверное количество подзадач эпика");
    }

    @Test
    void getEpics() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<HashMap<Integer, Epic>>() {}.getType();
        HashMap<Integer, Epic> actual = gson.fromJson(response.body(), taskType);

        assertNotNull(actual, "Epics не возвращаются");
        assertEquals(1, actual.size(), "Неверное количество epics");
        assertEquals(epic, actual.get(epic.getId()), "Epics не совпадают");
    }

    @Test
    void getTaskById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task/?Id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<Task>() {}.getType();
        Task actual = gson.fromJson(response.body(), taskType);

        assertNotNull(actual, "Tasks не возвращаются");
        assertEquals(task, actual, "Tasks не совпадают");
    }

    @Test
    void getSubtaskById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/subtask/?Id=3");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<Subtask>() {}.getType();
        Subtask actual = gson.fromJson(response.body(), taskType);

        assertNotNull(actual, "Subtasks не возвращаются");
        assertEquals(subtask, actual, "Subtasks не совпадают");
    }

    @Test
    void getEpicById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/epic/?Id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<Epic>() {}.getType();
        Epic actual = gson.fromJson(response.body(), taskType);

        assertNotNull(actual, "Epics не возвращаются");
        assertEquals(epic, actual, "Epics не совпадают");
    }

    @Test
    void deleteTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HashMap<Integer, Task> actual = taskManager.getAllTasks();
        assertEquals(0, actual.size(), "tasksMap не пустой");
    }

    @Test
    void deleteSubtasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HashMap<Integer, Subtask> actual = taskManager.getAllSubtasks();
        assertEquals(0, actual.size(), "subtasksMap не пустой");
    }

    @Test
    void deleteEpics() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HashMap<Integer, Epic> actual = taskManager.getAllEpics();
        assertEquals(0, actual.size(), "epicMap не пустой");
    }

    @Test
    void deleteTaskById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task/?Id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HashMap<Integer, Task> actual = taskManager.getAllTasks();
        assertEquals(0, actual.size(), "tasksMap не пустой");
    }

    @Test
    void deleteSubtaskById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/subtask/?Id=3");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HashMap<Integer, Subtask> actual = taskManager.getAllSubtasks();
        assertEquals(0, actual.size(), "subtasksMap не пустой");
    }

    @Test
    void deleteEpicById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/epic/?Id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HashMap<Integer, Epic> actual = taskManager.getAllEpics();
        assertEquals(0, actual.size(), "epicMap не пустой");
    }

    @Test
    void createOrUpdateTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/task");

        Task newTask = new Task("NEWTaskName", "NEWTaskDescription", Status.NEW,
                LocalDateTime.of(2024, 2,2,0,0),
                Duration.ofMinutes(60));
        newTask.setId(task.getId());
        String jsonTask = gson.toJson(newTask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        HashMap<Integer, Task> actual = taskManager.getAllTasks();
        assertEquals(1, actual.size(), "tasksMap некорректно обновилась");
        assertEquals(newTask, actual.get(task.getId()), "Tasks не совпадают. Некорректная работа updateTask()");


        Task task2 = new Task("testTaskName", "testTaskDescription", Status.NEW,
                LocalDateTime.of(2024, 4,4,0,0),
                Duration.ofMinutes(60));
        task2.setId(4);
        String jsonTask2 = gson.toJson(task2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask2))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode());

        HashMap<Integer, Task> actual2 = taskManager.getAllTasks();
        assertEquals(2, actual2.size(), "tasksMap пуста");
        assertEquals(task2, actual2.get(task2.getId()), "Tasks не совпадают. Некорректная работа createTask()");
    }

    @Test
    void createOrUpdateSubtask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/subtask");

        Subtask newSubtask = new Subtask("NEWSubtaskName", "NEWSubtaskDescription", Status.NEW, epic.getId(),
                LocalDateTime.of(2024, 3,3,0,0),
                Duration.ofMinutes(120));
        newSubtask.setId(subtask.getId());
        String jsonTask = gson.toJson(newSubtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        HashMap<Integer, Subtask> actual = taskManager.getAllSubtasks();
        assertEquals(1, actual.size(), "subtasksMap некорректно обновилась");
        assertEquals(newSubtask, actual.get(subtask.getId()), "Subtasks не совпадают. Некорректная работа updateSubtask()");


        Subtask subtask2 = new Subtask("testTaskName", "testTaskDescription", Status.NEW, epic.getId(),
                LocalDateTime.of(2024, 4,4,0,0),
                Duration.ofMinutes(60));
        subtask2.setId(4);
        String jsonTask2 = gson.toJson(subtask2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask2))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode());

        HashMap<Integer, Subtask> actual2 = taskManager.getAllSubtasks();
        assertEquals(2, actual2.size(), "subtasksMap пуста");
        assertEquals(subtask2, actual2.get(subtask2.getId()), "Subtasks не совпадают. Некорректная работа createSubtask()");
    }

    @Test
    void createOrUpdateEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/epic");

        Epic newEpic = new Epic("NEWEpicName", "NEWEpicDescription");
        newEpic.setId(epic.getId());
        String jsonTask = gson.toJson(newEpic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        HashMap<Integer, Epic> actual = taskManager.getAllEpics();
        assertEquals(1, actual.size(), "epicMap некорректно обновилась");


        Epic epic2 = new Epic("testTaskName", "testTaskDescription");
        epic2.setId(4);
        String jsonTask2 = gson.toJson(epic2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask2))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode());

        HashMap<Integer, Epic> actual2 = taskManager.getAllEpics();
        assertEquals(2, actual2.size(), "epicMap пуста");
        assertEquals(epic2, actual2.get(epic2.getId()), "Epics не совпадают. Некорректная работа createEpic()");
    }
}