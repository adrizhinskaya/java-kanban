import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(String name, String description, ArrayList<Subtask> subtasks) {
        super(name, description);
        for (Subtask s : subtasks) {
            s.setEpic(this);
        }
        this.subtasks = subtasks;
        Status epicStatus = checkSubtasksAndChooseStatus();
        setStatus(epicStatus);
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public void setStatus(Status status) {
        super.setStatus(checkSubtasksAndChooseStatus());
    }

    private Status checkSubtasksAndChooseStatus() {
        if (subtasks.isEmpty()) {
            return Status.NEW;
        }

        boolean isStatucesEqual = containsEqualValues();

        if (isStatucesEqual) {
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

    private boolean containsEqualValues() {
        boolean isStatucesEqual = true;

        for (int i = 0; i <= subtasks.size() - 2; i++) {
            if (subtasks.get(i).getStatus().equals(subtasks.get(i + 1).getStatus())) {
                isStatucesEqual = true;
            } else {
                isStatucesEqual = false;
            }
        }
        return isStatucesEqual;
    }
}
