package Api;

import Models.Epic;
import Models.Subtask;
import Models.Task;
import Services.FileBackedTasksManager;
import Services.Managers;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskManager extends FileBackedTasksManager {
    private static KVTaskClient kvTaskClient;
    private static Gson gson;

    public HttpTaskManager() {
        super(Managers.getDefaultHistory(), "autosave_data.csv");
        try {
            kvTaskClient = new KVTaskClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        gson = Managers.getGson();
    }

    @Override
    protected void save() {
        try {
            kvTaskClient.put("taskMap", gson.toJson(getAllTasks()));
            kvTaskClient.put("subtaskMap", gson.toJson(getAllSubtasks()));
            kvTaskClient.put("epicMap", gson.toJson(getAllEpics()));
            kvTaskClient.put("history", gson.toJson(getHistory()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }

    public static HttpTaskManager load() throws IOException, InterruptedException {
        HttpTaskManager taskManager = new HttpTaskManager();
        try {
            String jsonTaskMap = kvTaskClient.load("taskMap");
            String jsonSubtaskMap = kvTaskClient.load("subtaskMap");
            String jsonEpicMap = kvTaskClient.load("epicMap");
            String jsonHistory = kvTaskClient.load("history");
            Type taskMapType = new TypeToken<HashMap<Integer, Task>>() {
            }.getType();
            Type subtaskMapType = new TypeToken<HashMap<Integer, Subtask>>() {
            }.getType();
            Type epicMapType = new TypeToken<HashMap<Integer, Epic>>() {
            }.getType();
            Type historyListType = new TypeToken<List<Task>>() {
            }.getType();


            taskManager.taskMap = gson.fromJson(jsonTaskMap, taskMapType);
            taskManager.subtaskMap = gson.fromJson(jsonSubtaskMap, subtaskMapType);
            taskManager.epicMap = gson.fromJson(jsonEpicMap, epicMapType);
            setIdGenerator(taskManager);
            fillPrioritizedTasks(taskManager);
            List<Task> historyList = gson.fromJson(jsonHistory, historyListType);
            historyList.forEach(taskManager.historyManager::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return taskManager;
    }

    private static void setIdGenerator(HttpTaskManager taskManager) {
        List<Task> taskList = Stream.concat(taskManager.taskMap.values().stream(),
                Stream.concat(taskManager.epicMap.values().stream(),
                        taskManager.subtaskMap.values().stream())).collect(Collectors.toList());

        taskManager.id = taskList.stream()
                .max(Comparator.comparingInt(Task::getId))
                .get().getId();
    }

    private static void fillPrioritizedTasks(HttpTaskManager taskManager) {
        taskManager.prioritizedTasks.addAll(
                Stream.of(taskManager.taskMap.values(), taskManager.subtaskMap.values())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())
        );
    }
}
