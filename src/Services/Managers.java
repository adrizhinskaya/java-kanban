package Services;

import Api.HttpTaskManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

public class Managers {
    public static TaskManager getDefault() {
        return new HttpTaskManager();
    }

    public static TaskManager getFileBackedTaskManager() {
        return new FileBackedTasksManager(getDefaultHistory(), "autosave_data.csv");
    }

    public static TaskManager getTaskManagerFromFile(File file) {
        return FileBackedTasksManager.loadFromFile(getDefaultHistory(), file);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.create();
    }
}
