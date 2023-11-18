import java.util.HashMap;

public class Main {
    static TaskManager inMemoryTaskManager = Managers.getDefault();
    public static void main(String[] args) {

        Task task1 = new Task("Task1", "Описание Task1", Status.NEW);
        Task task2 = new Task("Task2", "Описание Task2", Status.NEW);
        inMemoryTaskManager.createTask(task1);
        inMemoryTaskManager.createTask(task2);

        Epic epic1 = new Epic("Epic1", "Описание Epic1");
        Epic epic2 = new Epic("Epic2", "Описание Epic2");
        inMemoryTaskManager.createEpic(epic1);
        inMemoryTaskManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask1", "Описание Subtask1", Status.DONE, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask2", "Описание Subtask2", Status.NEW, epic1.getId());
        Subtask subtask3 = new Subtask("Subtask3", "Описание Subtask3", Status.IN_PROGRESS, epic2.getId());
        inMemoryTaskManager.createSubtask(subtask1);
        inMemoryTaskManager.createSubtask(subtask2);
        inMemoryTaskManager.createSubtask(subtask3);

        inMemoryTaskManager.getTaskById(task1.getId());
        inMemoryTaskManager.getTaskById(task2.getId());
        inMemoryTaskManager.getEpicById(epic1.getId());
        inMemoryTaskManager.getEpicById(epic2.getId());
        inMemoryTaskManager.getSubtaskById(subtask1.getId());
        inMemoryTaskManager.getSubtaskById(subtask2.getId());
        inMemoryTaskManager.getSubtaskById(subtask3.getId());
        inMemoryTaskManager.getSubtaskById(subtask3.getId());
        inMemoryTaskManager.getSubtaskById(subtask3.getId());
        inMemoryTaskManager.getSubtaskById(subtask3.getId());
        inMemoryTaskManager.getSubtaskById(subtask3.getId());

        for(Task t : inMemoryTaskManager.getHistory()) {
            System.out.println(t.getName());
        }


//        showAllTasks();
//        System.out.println("________________________");
//
//        task1.setStatus(Status.IN_PROGRESS);
//        task2.setStatus(Status.DONE);
//        subtask1.setStatus(Status.IN_PROGRESS);
//        subtask2.setStatus(Status.DONE);
//        subtask3.setStatus(Status.DONE);
//
//        inMemoryTaskManager.updateTask(task1);
//        inMemoryTaskManager.updateTask(task2);
//        inMemoryTaskManager.updateSubtask(subtask1);
//        inMemoryTaskManager.updateSubtask(subtask2);
//        inMemoryTaskManager.updateSubtask(subtask3);
//
//        showAllTasks();
//        System.out.println("________________________");
//
//        inMemoryTaskManager.removeTaskById(task1.getId());
//        inMemoryTaskManager.removeSubtaskById(subtask1.getId());
//        inMemoryTaskManager.removeEpicById(epic2.getId());
//
//        showAllTasks();
//        System.out.println("________________________");
    }

    public static void showAllTasks() {
        printTasks(inMemoryTaskManager.getAllTasks());
        printSubtasks(inMemoryTaskManager.getAllSubtasks());
        printEpics(inMemoryTaskManager.getAllEpics());
    }

    public static void printTasks(HashMap<Integer, Task> taskHashMap) {
        System.out.println("\u001B[31m" + "TASKS" + "\u001B[0m");
        for (Task task : taskHashMap.values()) {
            System.out.println("Id - " + task.getId() + ", Name - " + task.getName() + ", Description - "
                    + task.getDescription() + ", Status - " + task.getStatus());
        }
    }

    public static void printSubtasks(HashMap<Integer, Subtask> subtaskHashMap) {
        System.out.println("\u001B[31m" + "SUBTASKS" + "\u001B[0m");
        for (Subtask subtask : subtaskHashMap.values()) {
            System.out.println("Id - " + subtask.getId() + " Name - " + subtask.getName() + ", Description - "
                    + subtask.getDescription() + ", Status - " + subtask.getStatus() + ", Epic - "
                    + inMemoryTaskManager.getEpicById(subtask.getEpicId()).getName());
        }
    }

    public static void printEpics(HashMap<Integer, Epic> epicHashMap) {
        System.out.println("\u001B[31m" + "EPICS" + "\u001B[0m");
        for (Epic epic : epicHashMap.values()) {
            System.out.println("Id - " + epic.getId() + ", Name - " + epic.getName() + ", Description - "
                    + epic.getDescription() + ", Status - " + epic.getStatus());
            System.out.println(" Epic Subtasks:");
            for (Subtask subtask : epic.getSubtasks()) {
                System.out.println(" Id - " + subtask.getId() + ", Name - " + subtask.getName() + ", Description - "
                        + subtask.getDescription() + ", Status - " + subtask.getStatus() + ", Epic - "
                        + inMemoryTaskManager.getEpicById(subtask.getEpicId()).getName());
            }
            System.out.println();
        }
    }
}
