package Models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    Epic epic;
    LocalDateTime localDateTimeNow = LocalDateTime.now();
    LocalDateTime startTime1 = LocalDateTime.of(2024,2,2,0,0);
    LocalDateTime startTime2 = LocalDateTime.of(2024,3,3,0,0);
    Duration duration1 = Duration.ofMinutes(60);
    Duration duration2 = Duration.ofMinutes(120);
    LocalDateTime endTime1 = startTime1.plus(duration1);
    LocalDateTime endTime2 = startTime2.plus(duration2);
    @BeforeEach
    public void createEpic() {
        epic =  new Epic("Epic Name", "Epic Description");
    }
    public Subtask createSubtask1(Status status) {
        return new Subtask("Subtask1 Name", "Subtask1 Description", status, epic.getId(), startTime1,
                duration1);
    }
    public Subtask createSubtask2(Status status) {
        return new Subtask("Subtask2 Name", "Subtask2 Description", status, epic.getId(), startTime2,
                duration2);
    }
    @Test
    public void shouldHaveNewStatusNullTimesAnd0DurationForEmptyList() {
        assertEquals(Status.NEW, epic.getStatus());
        assertEquals(localDateTimeNow.toLocalDate(), epic.getStartTime().toLocalDate());
        assertEquals(localDateTimeNow.toLocalDate(), epic.getStartTime().toLocalDate());
        assertEquals(Duration.ofMinutes(0), epic.getDuration());
        assertEquals(Duration.ofMinutes(0), epic.getDuration());
    }

    @Test
    public void shouldUpdateTimesForSubtask() {
        epic.addSubtask(createSubtask1(Status.NEW));
        assertEquals(startTime1, epic.getStartTime());
        assertEquals(endTime1, epic.getEndTime());
        assertEquals(duration1, epic.getDuration());
    }

    @Test
    public void shouldUpdateTimesForAllSubtasks() {
        epic.addSubtask(createSubtask1(Status.NEW));
        epic.addSubtask(createSubtask2(Status.NEW));
        assertEquals(startTime1, epic.getStartTime());
        assertEquals(endTime2, epic.getEndTime());
        assertEquals(duration1.plus(duration2), epic.getDuration());
    }

    @Test
    public void shouldHaveNewStatusForAllNewSubtasks() {
        epic.addSubtask(createSubtask1(Status.NEW));
        epic.addSubtask(createSubtask2(Status.NEW));
        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    public void shouldHaveDoneStatusForAllDoneSubtasks() {
        epic.addSubtask(createSubtask1(Status.DONE));
        epic.addSubtask(createSubtask2(Status.DONE));
        assertEquals(epic.getStatus(), Status.DONE);
    }

    @Test
    public void shouldHaveInProgressStatusForNewAndDoneSubtasks() {
        epic.addSubtask(createSubtask1(Status.NEW));
        epic.addSubtask(createSubtask2(Status.DONE));
        assertEquals(epic.getStatus(), Status.IN_PROGRESS);
    }

    @Test
    public void shouldHaveInProgressStatusForAllInProgressSubtasks() {
        epic.addSubtask(createSubtask1(Status.IN_PROGRESS));
        epic.addSubtask(createSubtask2(Status.IN_PROGRESS));
        assertEquals(epic.getStatus(), Status.IN_PROGRESS);
    }
}