package Services;

import Models.Epic;
import Models.Status;
import Models.Subtask;
import Models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackedTasksManagerTest {
    TaskManager _fileTaskManager;
    LocalDateTime localDateTimeNow = LocalDateTime.now();
    LocalDateTime startTime1 = LocalDateTime.of(2024, 2, 2, 0, 0);
    LocalDateTime startTime2 = LocalDateTime.of(2024, 3, 3, 0, 0);
    Duration duration1 = Duration.ofMinutes(60);
    Duration duration2 = Duration.ofMinutes(120);
    LocalDateTime endTime2 = startTime2.plus(duration2);

    @BeforeEach
    public void createFileBackTaskManager() {
        _fileTaskManager = new FileBackedTasksManager(new InMemoryHistoryManager(), "autosave_data.csv");
    }

    public TaskManager createBackedTaskManagerFromFile() {
        return Managers.getTaskManagerFromFile(new File("src\\Autosave\\autosave_data.csv"));
    }

    public Task createTaskInTaskManagerAndReturn() {
        Task task = new Task("Task Name", "Task Description", Status.NEW, startTime1, duration1);
        _fileTaskManager.createTask(task);
        return task;
    }

    public Subtask createSubtaskInTaskManagerAndReturn(Integer epicId) {
        Subtask subtask = new Subtask("Subtask Name", "Subtask Description", Status.NEW, epicId,
                startTime2, duration2);
        _fileTaskManager.createSubtask(subtask);
        return subtask;
    }

    public Epic createEpicInTaskManagerAndReturn() {
        Epic epic = new Epic("Epic Name", "Epic Description");
        _fileTaskManager.createEpic(epic);
        return epic;
    }

    @Test
    public void shouldReturnEmptyHistoryAndNoDataAfterDeleting() {
        _fileTaskManager.removeAllTasks();
        TaskManager resultBTM = createBackedTaskManagerFromFile();
        assertEquals(0, resultBTM.getAllTasks().size());
        assertEquals(0, resultBTM.getAllSubtasks().size());
        assertEquals(0, resultBTM.getAllEpics().size());
        assertEquals(0, resultBTM.getHistory().size());
    }

    @Test
    public void shouldReturnEmptyHistoryAndDataAfterCreating() {
        Task task = createTaskInTaskManagerAndReturn();
        Epic epic = createEpicInTaskManagerAndReturn();
        Subtask subtask = createSubtaskInTaskManagerAndReturn(epic.getId());

        TaskManager resultBTM = createBackedTaskManagerFromFile();
        assertEquals(1, resultBTM.getAllTasks().size());
        assertEquals(1, resultBTM.getAllSubtasks().size());
        assertEquals(1, resultBTM.getAllEpics().size());
        assertEquals(0, resultBTM.getHistory().size());
        assertEquals(1, resultBTM.getEpicById(epic.getId()).getSubtasks().size());
        assertEquals(startTime2, resultBTM.getEpicById(epic.getId()).getStartTime());
        assertEquals(endTime2, resultBTM.getEpicById(epic.getId()).getEndTime());
    }

    @Test
    public void shouldReturnHistoryAndDataAfterCreating() {
        Task task = createTaskInTaskManagerAndReturn();
        Epic epic = createEpicInTaskManagerAndReturn();
        _fileTaskManager.getTaskById(task.getId());
        _fileTaskManager.getEpicById(epic.getId());

        TaskManager resultBTM = createBackedTaskManagerFromFile();
        assertEquals(1, resultBTM.getAllTasks().size());
        assertEquals(0, resultBTM.getAllSubtasks().size());
        assertEquals(1, resultBTM.getAllEpics().size());
        assertEquals(2, resultBTM.getHistory().size());

        assertEquals(localDateTimeNow.toLocalDate(), resultBTM.getEpicById(epic.getId()).getStartTime().toLocalDate());
        assertEquals(localDateTimeNow.toLocalDate(), resultBTM.getEpicById(epic.getId()).getEndTime().toLocalDate());
    }
}