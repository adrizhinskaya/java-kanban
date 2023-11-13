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

    Task getTaskById(Integer id) {
        return taskMap.get(id);
    }

    Subtask getSubtaskById(Integer id) {
        return subtaskMap.get(id);
    }

    Epic getEpicById(Integer id) {
        return epicMap.get(id);
    }

    void createTask(Task task) {
        Integer taskId = generateId();
        task.setId(taskId);
        taskMap.put(taskId, task);
    }

    void createSubtask(Subtask subtask) {
        Integer subtaskId = generateId();
        subtask.setId(subtaskId);
        subtaskMap.put(subtaskId, subtask);
    }

    void createEpic(Epic epic) {
        Integer epicId = generateId();
        epic.setId(epicId);
        epicMap.put(epicId, epic);
    }

    void updateTask(Task task) {
        taskMap.put(task.getId(), task);
    }

    void updateSubtask(Subtask subtask) {
        subtask.getEpic().updateStatus();
        subtaskMap.put(subtask.getId(), subtask);
    }

    void updateEpic(Epic epic) {
        epicMap.put(epic.getId(), epic);
    }

    void removeTaskById(Integer id) {
        taskMap.remove(getTaskById(id).getId());
    }

    void removeSubtaskById(Integer id) {
        Subtask currentSubtask = getSubtaskById(id);
        currentSubtask.getEpic().updateStatus();
        subtaskMap.remove(currentSubtask.getId());
    }

    void removeEpicById(Integer id) {
        Epic currentEpic = getEpicById(id);
        currentEpic.getSubtasks().clear();
        epicMap.remove(currentEpic.getId());
    }

    void removeAllTasks() {
        taskMap.clear();
    }

    void removeAllSubtasks() {
        for(Epic epic : epicMap.values()) {
            epic.getSubtasks().clear();
            epic.updateStatus();
        }
        subtaskMap.clear();
    }

    void removeAllEpics() {
        removeAllSubtasks();
        epicMap.clear();
    }

    Integer generateId() {
        return ++id;
    }
}
