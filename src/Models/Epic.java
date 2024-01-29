package Models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private LocalDateTime endTime;
    private final ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW, LocalDateTime.of(2024, 1,1,0,0),
                Duration.ofMinutes(0));
        this.endTime = LocalDateTime.of(2024, 1,1,0,0);
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void addSubtask(Subtask subtask) {
        this.subtasks.add(subtask);
        updateStatusAndTime();
    }

    public void updateStatusAndTime() {
        updateStatus();
        updateDuration();
        updateStartTime();
        updateEndTime();
    }
    public void updateStatus() {
        super.setStatus(checkSubtasksAndChooseStatus());
    }

    public void updateDuration() {
        super.setDuration(checkSubtasksAndSumDuration());
    }

    public void updateStartTime() {
        super.setStartTime(checkSubtasksAndFindEarliestDateTime());
    }

    public void updateEndTime() {
        setEndTime(checkSubtasksAndFindLatestDateTime());
    }

    private Status checkSubtasksAndChooseStatus() {
        if (subtasks.isEmpty()) {
            return Status.NEW;
        }

        boolean isStatusesEqual = subtasksHaveSameStatuses();

        if (isStatusesEqual) {
            Status status = subtasks.get(0).getStatus();
            switch (status) {
                case NEW:
                    return Status.NEW;
                case DONE:
                    return Status.DONE;
                default:
                    return Status.IN_PROGRESS;
            }
        }
        return Status.IN_PROGRESS;
    }

    private boolean subtasksHaveSameStatuses() {
        Status status = subtasks.get(0).getStatus();
        for (int i = 1; i < subtasks.size(); i++) {
            if (!status.equals(subtasks.get(i).getStatus())) {
                return false;
            }
        }
        return true;
    }

    private Duration checkSubtasksAndSumDuration() {
        Duration subtasksDuration = Duration.ofMinutes(0);
        if (subtasks.isEmpty()) {
            return subtasksDuration;
        }
        for(Subtask s : subtasks) {
            subtasksDuration = subtasksDuration.plus(s.getDuration());
        }
        return subtasksDuration;
    }

    private LocalDateTime checkSubtasksAndFindEarliestDateTime() {
        if (subtasks.isEmpty()) {
            return super.getStartTime();
        }

        return subtasks.stream()
                .map(Subtask::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(super.getStartTime());
    }

    private LocalDateTime checkSubtasksAndFindLatestDateTime() {
        if (subtasks.isEmpty()) {
            return super.getEndTime();
        }

        return subtasks.stream()
                .map(Subtask::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(super.getEndTime());
    }
}
