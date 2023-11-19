package Services;

import java.util.HashMap;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(new HashMap<>(), new HashMap<>(), new HashMap<>(), getDefaultHistory());
    }

    static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
