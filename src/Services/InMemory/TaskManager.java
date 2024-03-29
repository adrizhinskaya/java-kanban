package Services.InMemory;

import Models.Epic;
import Models.Subtask;
import Models.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface TaskManager {

    List<Task> getHistory();

    public Set<Task> getPrioritizedTasks();

    HashMap<Integer, Task> getAllTasks();

    HashMap<Integer, Subtask> getAllSubtasks();

    HashMap<Integer, Epic> getAllEpics();

    Task getTaskById(Integer id);

    Subtask getSubtaskById(Integer id);

    Epic getEpicById(Integer id);

    void createTask(Task task);

    void createSubtask(Subtask subtask);

    void createEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void removeTaskById(Integer id);

    void removeSubtaskById(Integer id);

    void removeEpicById(Integer id);

    void removeAllTasks();

    void removeAllSubtasks();

    void removeAllEpics();
}
