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
        epicMap.get(subtask.getEpicId()).addSubtask(subtask);
        subtaskMap.put(subtaskId, subtask);
    }

    void createEpic(Epic epic) {
        Integer epicId = generateId();
        epic.setId(epicId);
        epicMap.put(epicId, epic);
    }

    void updateTask(Task task) { // не понимаю зачем нам этот метод если в мапе храняться объекты (изменяются обект - изменяется он же и в мапе)
        taskMap.put(task.getId(), task);
    }

    void updateSubtask(Subtask subtask) {
        epicMap.get(subtask.getEpicId()).updateStatus(); // так как у нас нет привязки к событию subtask.setStatus- статус эпика не обновится, поэтому все изменения подзадась должны проходить через этот метод
        subtaskMap.put(subtask.getId(), subtask); // в subtaskMap тоже хранятся объекты, непонятно зачем их менять напрямую
    }

    void updateEpic(Epic epic) {
        epicMap.put(epic.getId(), epic);
    }

    void removeTaskById(Integer id) {
        taskMap.remove(getTaskById(id).getId());
    }

    void removeSubtaskById(Integer id) {
        Subtask currentSubtask = getSubtaskById(id); // находим подзадачу
        Epic currentEpic = getEpicById(currentSubtask.getEpicId()); // находим эпик этой подзадачи в epicMap
        currentEpic.getSubtasks().remove(currentSubtask);// удаляем в эту подзадачу из списка подзада эпика
        currentEpic.updateStatus(); // обновляем статус эпика
        subtaskMap.remove(currentSubtask.getId()); // удаляем подзадачу из subtaskMap
    }

    void removeEpicById(Integer id) {
        Epic currentEpic = getEpicById(id);
        for (Subtask s : subtaskMap.values()) {
            if(s.getEpicId().equals(id)) {
                subtaskMap.remove(s.getId());
            }
        }
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
