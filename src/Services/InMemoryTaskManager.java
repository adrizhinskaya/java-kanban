package Services;

import Models.Epic;
import Models.Subtask;
import Models.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected Integer id = 0;
    protected final HashMap<Integer, Task> taskMap;
    protected final HashMap<Integer, Epic> epicMap;
    protected final HashMap<Integer, Subtask> subtaskMap;
    Set<Task> tickets = new TreeSet<>(Task::compareByStartTime);
    protected final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.taskMap = new HashMap<>();
        this.epicMap = new HashMap<>();
        this.subtaskMap = new HashMap<>();
        this.historyManager = historyManager;
        getPrioritizedTasks();
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
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task getTaskById(Integer id) {
        if (!taskMap.containsKey(id) || id == null) {
            throw new NoSuchElementException("Элемента с id " + id + "не существует");
        }
        Task task = taskMap.get(id);
        historyManager.add(task);
        return task;
    }

    public Subtask getSubtaskById(Integer id) {
        if (!subtaskMap.containsKey(id) || id == null) {
            throw new NoSuchElementException("Элемента с id " + id + "не существует");
        }
        historyManager.add(subtaskMap.get(id));
        return subtaskMap.get(id);
    }

    @Override
    public Epic getEpicById(Integer id) {
        if (!epicMap.containsKey(id) || id == null) {
            throw new NoSuchElementException("Элемента с id " + id + "не существует");
        }
        historyManager.add(epicMap.get(id));
        return epicMap.get(id);
    }

    @Override
    public void createTask(Task task) {
        if (isTaskOverlaps(task)) {
            throw new IllegalArgumentException("Задача [" + task.getName() + "] пересекается по времени с уже " +
                    "существующей. Измените startTime задачи и попробуйте снова.");
        }
        Integer taskId = generateId();
        task.setId(taskId);
        taskMap.put(taskId, task);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (isTaskOverlaps(subtask)) {
            throw new IllegalArgumentException("Подзадача [" + subtask.getName() + "] пересекается по времени с уже " +
                    "существующей. Измените startTime подзадачи и попробуйте снова.");
        }
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
        Task oldTask = taskMap.get(task.getId());
        if (!task.getStartTime().isEqual(oldTask.getStartTime()) || !task.getDuration().equals(oldTask.getDuration())) {
            if (isTaskOverlaps(task)) {
                throw new IllegalArgumentException("Задача [" + task.getName() + "] пересекается по времени с уже " +
                        "существующей. Измените startTime задачи и попробуйте снова.");
            }
        }
        taskMap.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Task oldSubtask = subtaskMap.get(subtask.getId());
        if (!subtask.getStartTime().isEqual(oldSubtask.getStartTime())
                || !subtask.getDuration().equals(oldSubtask.getDuration())) {
            if (isTaskOverlaps(subtask)) {
                throw new IllegalArgumentException("Подзадача [" + subtask.getName() + "] пересекается по " +
                        "времени с уже существующей. Измените startTime подзадачи и попробуйте снова.");
            }
        }
        for (Subtask s : epicMap.get(subtask.getEpicId()).getSubtasks()) {
            if (s.getId() == subtask.getId()) {
                s.setName(subtask.getName());
                s.setStatus(subtask.getStatus());
                s.setDescription(subtask.getDescription());
                s.setStartTime(subtask.getStartTime());
                s.setDuration(subtask.getDuration());
            }
        }
        epicMap.get(subtask.getEpicId()).updateStatusAndTime();
        subtaskMap.put(subtask.getId(), subtask);
    }

    @Override
    public void updateEpic(Epic newEpic) {
        Epic epic = epicMap.get(newEpic.getId());
        epic.setName(newEpic.getName());
        epic.setDescription(newEpic.getDescription());
    }

    @Override
    public void removeTaskById(Integer id) {
        if (!taskMap.containsKey(id) || id == null) {
            throw new NoSuchElementException("Элемента с id " + id + "не существует");
        }
        taskMap.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeSubtaskById(Integer id) {
        if (!subtaskMap.containsKey(id) || id == null) {
            throw new NoSuchElementException("Элемента с id " + id + "не существует");
        }
        Subtask currentSubtask = subtaskMap.get(id);
        Epic currentEpic = epicMap.get(currentSubtask.getEpicId());
        currentEpic.getSubtasks().remove(currentSubtask);
        currentEpic.updateStatusAndTime();
        subtaskMap.remove(currentSubtask.getId());
        historyManager.remove(id);
    }

    @Override
    public void removeEpicById(Integer id) {
        if (!epicMap.containsKey(id) || id == null) {
            throw new NoSuchElementException("Элемента с id " + id + "не существует");
        }
        Epic currentEpic = epicMap.get(id);
        subtaskMap.values().removeIf(s -> s.getEpicId().equals(id));
        epicMap.remove(currentEpic.getId());

        for (Subtask s : currentEpic.getSubtasks()) {
            historyManager.remove(s.getId());
        }
        historyManager.remove(id);
    }

    @Override
    public void removeAllTasks() {
        taskMap.values().forEach(task -> historyManager.remove(task.getId()));
        taskMap.clear();
    }

    @Override
    public void removeAllSubtasks() {
        for (Epic epic : epicMap.values()) {
            epic.getSubtasks().forEach(subtask -> historyManager.remove(subtask.getId()));
            epic.getSubtasks().clear();
            epic.updateStatusAndTime();
        }
        subtaskMap.clear();
    }

    @Override
    public void removeAllEpics() {
        removeAllSubtasks();
        for (Epic epic : epicMap.values()) {
            historyManager.remove(epic.getId());
        }
        epicMap.clear();
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        tickets.addAll(taskMap.values());
        tickets.addAll(subtaskMap.values());
        return tickets;
    }

    public boolean isTaskOverlaps(Task task) {
        Set<Task> sortedTasks = getPrioritizedTasks();
        boolean match = sortedTasks.stream()
                .filter(iterTask -> isTimeOverlapping(task, iterTask))
                .anyMatch(task1 -> true);
        return match;
    }

    private boolean isTimeOverlapping(Task task1, Task task2) {
        boolean isTaskEqual = task1.getId() == task2.getId();
        if (isTaskEqual) {
            return false;
        }
        boolean startAfterStart = task1.getStartTime().isAfter(task2.getStartTime());
        boolean startEqualStart = task1.getStartTime().isEqual(task2.getStartTime());
        boolean startBeforeEnd = task1.getStartTime().isBefore(task2.getEndTime());
        boolean startEqualEnd = task1.getStartTime().isEqual(task2.getEndTime());

        boolean endAfterStart = task1.getEndTime().isAfter(task2.getStartTime());
        boolean endEqualStart = task1.getEndTime().isEqual(task2.getStartTime());
        boolean endBeforeEnd = task1.getEndTime().isBefore(task2.getEndTime());
        boolean endEqualEnd = task1.getEndTime().isEqual(task2.getEndTime());

        boolean isStartTimeOverlapping = (startAfterStart || startEqualStart) && (startBeforeEnd || startEqualEnd);
        boolean isEndTimeOverlapping = (endAfterStart || endEqualStart) && (endBeforeEnd || endEqualEnd);

        return isStartTimeOverlapping || isEndTimeOverlapping;
    }

    private Integer generateId() {
        return ++id;
    }
}
