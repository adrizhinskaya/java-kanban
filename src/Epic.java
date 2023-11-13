import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public Epic(String name, String description, ArrayList<Subtask> subtasks) {
        super(name, description, Status.NEW);
        this.subtasks = subtasks;
        updateStatus();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        this.subtasks.add(subtask);
        updateStatus();
    }

    public void updateStatus() {
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
        Status status = subtasks.get(0).getStatus();
        for (int i = 1; i < subtasks.size(); i++) {
            if (!status.equals(subtasks.get(i).getStatus())) {
                return false;
            }
        }
        return true;
    }
}
