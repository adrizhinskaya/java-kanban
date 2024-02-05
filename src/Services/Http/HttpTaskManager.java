package Services.Http;

import Server.KVTaskClient;
import Models.Epic;
import Models.Subtask;
import Models.Task;
import Services.File.FileBackedTasksManager;
import Services.InMemory.HistoryManager;
import Services.Managers;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class HttpTaskManager extends FileBackedTasksManager {
    private static KVTaskClient kvTaskClient;
    private static Gson gson;

    public HttpTaskManager(HistoryManager historyManager) {
        super(historyManager);
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

    public static HttpTaskManager load() throws IOException, InterruptedException {
        HttpTaskManager taskManager = new HttpTaskManager(Managers.getDefaultHistory());
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
            FileBackedTasksManager.setIdGenerator(taskManager);
            FileBackedTasksManager.fillPrioritizedTasks(taskManager);
            List<Task> historyList = gson.fromJson(jsonHistory, historyListType);
            historyList.forEach(taskManager.historyManager::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return taskManager;
    }
}
