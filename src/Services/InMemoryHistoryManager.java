package Services;

import Models.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    final byte VIEWED_TASKS_CAPACITY = 10;
    private final List<Task> viewedTasks = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (viewedTasks.size() >= VIEWED_TASKS_CAPACITY) {
            viewedTasks.remove(0);
        }
        viewedTasks.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return viewedTasks;
    }
}
