package Services;

import java.io.File;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static TaskManager getFileBackedTaskManager() {
        return new FileBackedTasksManager(getDefaultHistory(), "autosave_data.csv");
    }

    public static TaskManager getTaskManagerFromFile(File file) {
        return FileBackedTasksManager.loadFromFile(getDefaultHistory(), file);
    }

    static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
