package Services;

import Models.Status;
import Models.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    HistoryManager historyManager;
    static Task task1;
    static Task task2;
    static Task task3;


    @BeforeAll
    public static void createTasks() {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime startTime2 = LocalDateTime.of(2024, 2, 2, 0, 0);
        LocalDateTime startTime3 = LocalDateTime.of(2024, 3, 3, 0, 0);
        Duration duration1 = Duration.ofMinutes(60);
        Duration duration2 = Duration.ofMinutes(120);
        Duration duration3 = Duration.ofMinutes(180);
        task1 = new Task("Task1 Name", "Task1 Description", Status.NEW, startTime1, duration1);
        task2 = new Task("Task2 Name", "Task2 Description", Status.NEW, startTime2, duration2);
        task3 = new Task("Task3 Name", "Task3 Description", Status.NEW, startTime3, duration3);
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
    }

    @BeforeEach
    public void createTaskManager() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void shouldReturnEmptyListForNoActions() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая");
        assertEquals(0, history.size(), "История не пустая");
    }

    @Test
    public void shouldFillHistoryAfterAdding() {
        historyManager.add(task1);
        List<Task> historyWith1Elem = historyManager.getHistory();
        assertEquals(1, historyWith1Elem.size(), "Просмотр не добавлен в историю");
        assertEquals(task1, historyWith1Elem.get(0), "Некорректная работа добавления просмотров в историю");

        historyManager.add(task2);
        List<Task> historyWith2Elem = historyManager.getHistory();
        assertEquals(2, historyWith2Elem.size(), "Просмотр не добавлен в историю");
        assertEquals(task2, historyWith2Elem.get(1), "Некорректная работа добавления просмотров в историю");
    }

    @Test
    public void shouldReplaceEqualTaskInHistoryAfterAdding() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверный размер истории после добавления дубликата");
        assertEquals(task2, history.get(0), "Некорректное расположение элементов в истории после добавления " +
                "дубликата");
    }

    @Test
    public void shouldRemoveFirstArgumentInHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверный размер истории после удаления элемента");
        assertEquals(task2, history.get(0), "Некорректное расположение элементов в истории после удаления " +
                "элемента");
    }

    @Test
    public void shouldRemoveLastArgumentInHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверный размер истории после удаления элемента");
        assertEquals(task2, history.get(1), "Некорректное расположение элементов в истории после удаления " +
                "элемента");
    }

    @Test
    public void shouldRemoveMiddleArgumentInHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Неверный размер истории после удаления элемента");
        assertEquals(task3, history.get(1), "Некорректное расположение элементов в истории после удаления " +
                "элемента");
    }
}