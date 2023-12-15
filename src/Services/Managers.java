package Services;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
