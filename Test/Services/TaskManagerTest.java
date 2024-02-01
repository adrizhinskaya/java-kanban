package Services;

import Models.Epic;
import Models.Status;
import Models.Subtask;
import Models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskManagerTest {
    TaskManager taskManager;
    LocalDateTime startTime1 = LocalDateTime.of(2024, 2, 2, 0, 0);
    LocalDateTime startTime2 = LocalDateTime.of(2024, 3, 3, 0, 0);
    Duration duration1 = Duration.ofMinutes(60);
    Duration duration2 = Duration.ofMinutes(120);

    @BeforeEach
    public void createTaskManager() {
        taskManager = Managers.getDefault();
    }

    public Task createTaskInTaskManagerAndReturn() {
        Task task = new Task("Task Name", "Task Description", Status.NEW, startTime1, duration1);
        taskManager.createTask(task);
        return task;
    }

    public Subtask createSubtaskInTaskManagerAndReturn(Integer epicId) {
        Subtask subtask = new Subtask("Subtask Name", "Subtask Description", Status.NEW, epicId,
                startTime2, duration2);
        taskManager.createSubtask(subtask);
        return subtask;
    }

    public Epic createEpicInTaskManagerAndReturn() {
        Epic epic = new Epic("Epic Name", "Epic Description");
        taskManager.createEpic(epic);
        return epic;
    }

    @Test
    void getAllTasks() {
        assertEquals(0, taskManager.getAllTasks().size(), "taskMap не пустая");
        Task task = createTaskInTaskManagerAndReturn();
        assertEquals(1, taskManager.getAllTasks().size(), "taskMap не заполняется");
    }

    @Test
    void getAllSubtasks() {
        assertEquals(0, taskManager.getAllSubtasks().size(), "subtaskMap не пустая");
        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());
        assertEquals(1, taskManager.getAllSubtasks().size(), "subtaskMap не заполняется");
    }

    @Test
    void getAllEpics() {
        assertEquals(0, taskManager.getAllEpics().size(), "epicMap не пустая");
        Epic epic = createEpicInTaskManagerAndReturn();
        assertEquals(1, taskManager.getAllEpics().size(), "epicMap не заполняется");
    }

    @Test
    void getTaskById() {
        final NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> {
                    taskManager.getTaskById(0);
                }, "Нет исключения на несуществующий id");
        assertEquals("Элемента с id " + 0 + "не существует", exception.getMessage());
        Task task = createTaskInTaskManagerAndReturn();
        assertEquals(task, taskManager.getTaskById(task.getId()), "Не возвращается существующая задача");
    }

    @Test
    void getSubtaskById() {
        final NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> {
                    taskManager.getSubtaskById(0);
                }, "Нет исключения на несуществующий id");

        assertEquals("Элемента с id " + 0 + "не существует", exception.getMessage());
        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Не возвращается существующая " +
                "подзадача");
    }

    @Test
    void getEpicById() {
        final NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> {
                    taskManager.getEpicById(0);
                }, "Нет исключения на несуществующий id");
        assertEquals("Элемента с id " + 0 + " не существует", exception.getMessage());

        Epic epic = createEpicInTaskManagerAndReturn();
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "Не возвращается существующий эпик");
    }

    @Test
    void createTask() {
        Task task = createTaskInTaskManagerAndReturn();
        assertEquals(1, task.getId(), "Некорректная работа присвоения ID");
        assertEquals(1, taskManager.getAllTasks().size(), "Задача не добавляется в taskMap");
        assertEquals(1, taskManager.getPrioritizedTasks().size(),
                "Задача не добавляется в prioritizedTasks");

        Task task2 = new Task("Task2 Name", "Task2 Description", Status.DONE, startTime1, duration1);
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    taskManager.createTask(task2);
                }, "Нет исключения на пересечение задач по времени");

        assertEquals("Задача [" + task2.getName() + "] пересекается по времени с уже " +
                "существующей. Измените startTime задачи и попробуйте снова.", exception.getMessage());
    }

    @Test
    void createSubtask() {
        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());
        assertEquals(2, subtask.getId(), "Некорректная работа присвоения ID");
        assertEquals(1, epic.getSubtasks().size(), "Подзадача не добавляется в subtasksList эпика");
        assertEquals(1, taskManager.getAllSubtasks().size(), "Подзадача не добавляется в subtaskMap");
        assertEquals(1, taskManager.getPrioritizedTasks().size(),
                "Подзадача не добавляется в prioritizedTasks");

        Subtask subtask2 = new Subtask("Subtask2 Name", "Subtask2 Description", Status.NEW, epic.getId(),
                startTime2.plus(duration2), duration2);
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    taskManager.createSubtask(subtask2);
                }, "Нет исключения на пересечение задач по времени");

        assertEquals("Подзадача [" + subtask2.getName() + "] пересекается по времени с уже " +
                "существующей. Измените startTime подзадачи и попробуйте снова.", exception.getMessage());
    }

    @Test
    void createEpic() {
        Epic epic = createEpicInTaskManagerAndReturn();
        assertEquals(1, epic.getId(), "Некорректная работа присвоения ID");
        assertEquals(1, taskManager.getAllEpics().size(), "Эпик не добавляется в epicMap");
    }

    @Test
    void updateTask() {
        Task task = createTaskInTaskManagerAndReturn();
        LocalDateTime newDate = LocalDateTime.of(2024, 2, 2, 0, 30);
        Duration newDuration = Duration.ofMinutes(60);
        Task newTask = new Task("NEW Task Name", "NEW Task Description", Status.IN_PROGRESS, newDate,
                newDuration);
        newTask.setId(task.getId());
        taskManager.updateTask(newTask);
        assertEquals(1, taskManager.getAllTasks().size(), "Некорректное добавление в taskMap");
        assertEquals(newTask, taskManager.getTaskById(task.getId()), "Некорректное обновление");


        LocalDateTime date2 = LocalDateTime.of(2024, 2, 2, 2, 0);
        Task task2 = new Task("Task2 Name", "Task2 Description", Status.IN_PROGRESS, date2,
                duration2);
        newTask.setId(2);
        taskManager.createTask(task2);

        LocalDateTime dateForEx = LocalDateTime.of(2024, 2, 2, 1, 10);
        Duration durationForEx = Duration.ofMinutes(60);
        Task exTask = new Task("Task1 Name", "Task1 Description", Status.DONE, dateForEx, durationForEx);
        exTask.setId(task.getId());
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    taskManager.updateTask(exTask);
                }, "Нет исключения на пересечение задач по времени");

        assertEquals("Задача [" + exTask.getName() + "] пересекается по времени с уже " +
                "существующей. Измените startTime задачи и попробуйте снова.", exception.getMessage());
    }

    @Test
    void updateSubtask() {
        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());
        Subtask newSubtask = new Subtask("NEW Subtask Name", "NEW Subask Description",
                Status.IN_PROGRESS, epic.getId(), startTime2, duration2);
        newSubtask.setId(subtask.getId());

        taskManager.updateSubtask(newSubtask);

        subtask = epic.getSubtasks().get(0);
        assertEquals(newSubtask.getName(), subtask.getName(), "Имя подзадачи не обновилось");
        assertEquals(newSubtask.getDescription(), subtask.getDescription(),
                "Описание подзадачи не обновилось");
        assertEquals(newSubtask.getStatus(), subtask.getStatus(), "Статус подзадачи не обновился");
        assertEquals(newSubtask.getStartTime(), subtask.getStartTime(), "startTime подзадачи не обновился");
        assertEquals(newSubtask.getDuration(), subtask.getDuration(), "duration подзадачи не обновился");
        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Статус эпика не обновился после обновления подзадачи");
        assertEquals(newSubtask, taskManager.getSubtaskById(subtask.getId()),
                "Подзадача не обновилась в мапе");

        LocalDateTime date2 = LocalDateTime.of(2024, 3, 3, 3, 0);
        Subtask subtask2 = new Subtask("Subtask2 Name", "Subtask2 Description", Status.IN_PROGRESS,
                epic.getId(), date2,
                duration2);
        subtask2.setId(2);
        taskManager.createTask(subtask2);

        LocalDateTime dateForEx = LocalDateTime.of(2024, 3, 3, 1, 30);
        Subtask exSubtask = new Subtask("Subtask Name", "Subtask Description", Status.DONE,
                epic.getId(), dateForEx, duration2);
        exSubtask.setId(subtask.getId());
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    taskManager.updateSubtask(exSubtask);
                }, "Нет исключения на пересечение задач по времени");

        assertEquals("Подзадача [" + exSubtask.getName() + "] пересекается по времени с уже " +
                "существующей. Измените startTime подзадачи и попробуйте снова.", exception.getMessage());
    }

    @Test
    void updateEpic() {
        Epic epic = createEpicInTaskManagerAndReturn();
        Epic newEpic = new Epic("NEW Epic Name", "New Epic Description");
        newEpic.setId(epic.getId());

        taskManager.updateEpic(newEpic);
        epic = taskManager.getEpicById(epic.getId());
        assertEquals("NEW Epic Name", epic.getName(), "Обновление имени некорректно");
        assertEquals("New Epic Description", epic.getDescription(), "Обновление описания некорректно");
    }

    @Test
    void removeTaskById() {
        final NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> {
                    taskManager.removeTaskById(0);
                }, "Нет исключения на несуществующий id");
        assertEquals("Элемента с id " + 0 + "не существует", exception.getMessage());
        Task task = createTaskInTaskManagerAndReturn();
        taskManager.removeTaskById(task.getId());
        assertEquals(0, taskManager.getAllTasks().size(), "Задача не удалена из taskMap");
        assertEquals(0, taskManager.getAllTasks().size(), "Задача не удалена из prioritizedTasks");
    }

    @Test
    void removeSubtaskById() {
        final NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> {
                    taskManager.removeSubtaskById(0);
                }, "Нет исключения на несуществующий id");
        assertEquals("Элемента с id " + 0 + "не существует", exception.getMessage());

        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());
        epic.setStatus(Status.IN_PROGRESS);
        taskManager.removeSubtaskById(subtask.getId());
        assertEquals(0, epic.getSubtasks().size(), "Подзадача не удалена из связанного эпика");
        assertEquals(Status.NEW, epic.getStatus(), "Не обновился статус эпика после удаления подзадачи");
        assertEquals(0, taskManager.getAllSubtasks().size(), "Подзадача не удалена из subtaskMap");
        assertEquals(0, taskManager.getPrioritizedTasks().size(),
                "Подзадача не удалена из prioritizedTasks");
    }

    @Test
    void removeEpicById() {
        final NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> {
                    taskManager.removeEpicById(0);
                }, "Нет исключения на несуществующий id");
        assertEquals("Элемента с id " + 0 + "не существует", exception.getMessage());
        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());
        taskManager.removeEpicById(epic.getId());
        assertEquals(0, taskManager.getAllSubtasks().size(),
                "Не удалены подзадачи эпика в subtaskMap");
        assertEquals(0, taskManager.getAllEpics().size(), "Эпик не удалён в epicMap");
    }

    @Test
    void removeAllTasks() {
        Task task = createTaskInTaskManagerAndReturn();
        taskManager.removeAllTasks();
        assertEquals(0, taskManager.getAllTasks().size(), "Задачи не были удалены из taskMap");
        assertEquals(0, taskManager.getPrioritizedTasks().size(),
                "Задачи не были удалены из prioritizedTasks");
    }

    @Test
    void removeAllSubtasks() {
        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());
        epic.setStatus(Status.IN_PROGRESS);
        taskManager.removeAllSubtasks();

        assertEquals(0, epic.getSubtasks().size(), "Не очищены списки подзадачь эпиков");
        assertEquals(Status.NEW, epic.getStatus(), "Не обновился статус эпиков");
        assertEquals(0, taskManager.getAllSubtasks().size(), "Не очищена subtaskMap");
        assertEquals(0, taskManager.getPrioritizedTasks().size(),
                "Подзадачи не были удалены из prioritizedTasks");
    }

    @Test
    void removeAllEpics() {
        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());
        taskManager.removeAllEpics();

        assertEquals(taskManager.getAllEpics().size(), 0, "Не очищена epicMap");
        assertEquals(taskManager.getAllSubtasks().size(), 0, "Не очищена subtaskMap");
    }

    @Test
    void getHistory() {
        assertEquals(taskManager.getHistory().size(), 0, "История заполнена");
        Task task = createTaskInTaskManagerAndReturn();
        Epic epic = createEpicInTaskManagerAndReturn();
        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());
        assertEquals(taskManager.getHistory().size(), 2, "История некорректно заполнена");
    }

    @Test
    void getPrioritizedTasks() {
        Task task = createTaskInTaskManagerAndReturn();
        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());
        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size(),
                "Некорректно заполнен список задач по приоритетности");
        assertEquals(task, prioritizedTasks.toArray()[0],
                "Некорректная сортировка списка задач по приоритетности");
        assertEquals(subtask, prioritizedTasks.toArray()[1],
                "Некорректная сортировка списка задач по приоритетности");
    }
}