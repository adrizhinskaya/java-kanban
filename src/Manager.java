import java.util.HashMap;

public class Manager {
    private Integer id = 0;
    HashMap<Integer, Task> taskMap = new HashMap<>();
    HashMap<Integer, Epic> epicMap = new HashMap<>();
    HashMap<Integer, Subtask> subtaskMap = new HashMap<>();

    HashMap<Integer, Task> getAllTasks() {
        return taskMap;
    }

    HashMap<Integer, Subtask> getAllSubtasks() {
        return subtaskMap;
    }

    HashMap<Integer, Epic> getAllEpics() {
        return epicMap;
    }

    Task getTaskById(int id) {
        for (int key : taskMap.keySet()) {
            if (key == id) {
                return taskMap.get(key);
            }
        }
        return null;
    }

    Subtask getSubtaskById(int id) {
        for (int key : subtaskMap.keySet()) {
            if (key == id) {
                return subtaskMap.get(key);
            }
        }
        return null;
    }

    Epic getEpicById(int id) {
        for (int key : epicMap.keySet()) {
            if (key == id) {
                return epicMap.get(key);
            }
        }
        return null;
    }

    void createTask(Task task) {
        Integer taskId = ++id;
        task.setId(taskId);
        taskMap.put(taskId, task);
    }

    void createSubtask(Subtask subtask) {
        Integer subtaskId = ++id;
        subtask.setId(subtaskId);
        subtaskMap.put(subtaskId, subtask);
    }

    void createEpic(Epic epic) {
        Integer epicId = ++id;
        epic.setId(epicId);
        epicMap.put(epicId, epic);
    }

    void updateTask(Task task) {
        taskMap.put(task.getId(), task);
    }

    void updateSubtask(Subtask subtask) {
        subtaskMap.put(subtask.getId(), subtask);
    }

    void updateEpic(Epic epic) {
        epicMap.put(epic.getId(), epic);
    }

    void removeTaskById(int id) {
        taskMap.remove(getTaskById(id).getId());
    }

    void removeSubtaskById(int id) {
        subtaskMap.remove(getSubtaskById(id).getId());
    }

    void removeEpicById(int id) {
        epicMap.remove(getEpicById(id).getId());
    }

    void removeAllTasks() {
        taskMap.clear();
    }

    void removeAllSubtasks() {
        subtaskMap.clear();
    }

    void removeAllEpics() {
        epicMap.clear();
    }
}
