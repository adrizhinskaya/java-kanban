package Services;

import Models.Epic;
import Models.Subtask;
import Models.Task;

import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private Integer id = 0;
    private final HashMap<Integer, Task> taskMap;
    private final HashMap<Integer, Epic> epicMap;
    private final HashMap<Integer, Subtask> subtaskMap;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.taskMap = new HashMap<>();
        this.epicMap = new HashMap<>();
        this.subtaskMap = new HashMap<>();
        this.historyManager = historyManager;
    }

    @Override
    public HashMap<Integer, Task> getAllTasks() {
        return taskMap;
    }

    @Override
    public HashMap<Integer, Subtask> getAllSubtasks() {
        return subtaskMap;
    }

    @Override
    public HashMap<Integer, Epic> getAllEpics() {
        return epicMap;
    }

    @Override
    public Task getTaskById(Integer id) {
        historyManager.add(taskMap.get(id));
        return taskMap.get(id);
    }

    public Subtask getSubtaskById(Integer id) {
        historyManager.add(subtaskMap.get(id));
        return subtaskMap.get(id);
    }

    @Override
    public Epic getEpicById(Integer id) {
        historyManager.add(epicMap.get(id));
        return epicMap.get(id);
    }

    @Override
    public void createTask(Task task) {
        Integer taskId = generateId();
        task.setId(taskId);
        taskMap.put(taskId, task);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        Integer subtaskId = generateId();
        subtask.setId(subtaskId);
        epicMap.get(subtask.getEpicId()).addSubtask(subtask);
        subtaskMap.put(subtaskId, subtask);
    }

    @Override
    public void createEpic(Epic epic) {
        Integer epicId = generateId();
        epic.setId(epicId);
        epicMap.put(epicId, epic);
    }

    @Override
    public void updateTask(Task task) {
        taskMap.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        for (Subtask s : epicMap.get(subtask.getEpicId()).getSubtasks()) {
            if (s.getId() == subtask.getId()) {
                s = subtask;
            }
        }
        getEpicById(subtask.getEpicId()).updateStatus();
        subtaskMap.put(subtask.getId(), subtask);
    }

    @Override
    public void updateEpic(Epic epic) {
        getEpicById(epic.getId()).setName(epic.getName());
        getEpicById(epic.getId()).setDescription(epic.getDescription());
    }

    @Override
    public void removeTaskById(Integer id) {
        taskMap.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeSubtaskById(Integer id) {
        Subtask currentSubtask = subtaskMap.get(id);
        Epic currentEpic = epicMap.get(currentSubtask.getEpicId());
        currentEpic.getSubtasks().remove(currentSubtask);
        currentEpic.updateStatus();
        subtaskMap.remove(currentSubtask.getId());
        historyManager.remove(id);
    }

    @Override
    public void removeEpicById(Integer id) {
        Epic currentEpic = epicMap.get(id);
        subtaskMap.values().removeIf(s -> s.getEpicId().equals(id)); // ChatGPT и IntelliSense помогли заменить этой
        // строчкой код ниже (удаление элементов хэш-таблицы в процессе итерации).
        // Это верное решение с точки зрения логики работы?

//        Epic currentEpic = epicMap.get(id);
//        for (Subtask s : subtaskMap.values()) {
//            if (s.getEpicId().equals(id)) {
//                subtaskMap.remove(s.getId());
//            }
//        }
        epicMap.remove(currentEpic.getId());

        for (Subtask s : currentEpic.getSubtasks()){
            historyManager.remove(s.getId());
        }
        historyManager.remove(id);
    }

    @Override
    public void removeAllTasks() {
        taskMap.values().forEach(task -> removeTaskById(task.getId()));
        for(Task t : taskMap.values()) {
            removeTaskById(t.getId());
        }
    }

    @Override
    public void removeAllSubtasks() {
        for (Epic epic : epicMap.values()) {
            epic.getSubtasks().forEach(subtask -> historyManager.remove(subtask.getId()));
            epic.getSubtasks().clear();
        }
        subtaskMap.clear();
    }

    @Override
    public void removeAllEpics() {
        removeAllSubtasks();
        for(Epic epic : epicMap.values()) {
            historyManager.remove(epic.getId());
        }
        epicMap.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    Integer generateId() {
        return ++id;
    }
}
