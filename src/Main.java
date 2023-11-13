import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    static Manager manager = new Manager();

    public static void main(String[] args) {

        Task task1 = new Task("Task1", "Описание Task1", Status.NEW);
        Task task2 = new Task("Task2", "Описание Task2", Status.NEW);
        Epic epic1 = new Epic("Epic1", "Описание Epic1");
        Epic epic2 = new Epic("Epic2", "Описание Epic2");
        Subtask subtask1 = new Subtask("Subtask1", "Описание Subtask1", Status.NEW, epic1);
        Subtask subtask2 = new Subtask("Subtask2", "Описание Subtask2", Status.NEW, epic1);
        Subtask subtask3 = new Subtask("Subtask3", "Описание Subtask3", Status.NEW, epic2);

        manager.createTask(task1);
        manager.createTask(task2);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);
        manager.createSubtask(subtask3);
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        showAllTasks();
        System.out.println("________________________");

        task1.setStatus(Status.IN_PROGRESS);
        task2.setStatus(Status.DONE);
        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask3.setStatus(Status.DONE);

        showAllTasks();
        System.out.println("________________________");

        manager.removeTaskById(task1.getId());
        manager.removeEpicById(epic1.getId());

        showAllTasks();
        System.out.println("________________________");
    }

    public static void showAllTasks() {
        printTasks(manager.getAllTasks());
        printSubtasks(manager.getAllSubtasks());
        printEpics(manager.getAllEpics());
    }

    public static void printTasks(HashMap<Integer, Task> taskHashMap) {
        System.out.println("\u001B[31m" + "TASKS" + "\u001B[0m");
        for (Task task : taskHashMap.values()) {
            System.out.println("Id - " + task.getId() + ", Name - " + task.getName() + ", Description - " + task.getDescription() + ", Status - " + task.getStatus());
        }
    }

    public static void printSubtasks(HashMap<Integer, Subtask> subtaskHashMap) {
        System.out.println("\u001B[31m" + "SUBTASKS" + "\u001B[0m");
        for (Subtask subtask : subtaskHashMap.values()) {
            System.out.println("Id - " + subtask.getId() + " Name - " + subtask.getName() + ", Description - " + subtask.getDescription() + ", Status - " + subtask.getStatus() + ", Epic - " + subtask.getEpic().getName());
        }
    }

    public static void printEpics(HashMap<Integer, Epic> epicHashMap) {
        System.out.println("\u001B[31m" + "EPICS" + "\u001B[0m");
        for (Epic epic : epicHashMap.values()) {
            System.out.println("Id - " + epic.getId() + ", Name - " + epic.getName() + ", Description - " + epic.getDescription() + ", Status - " + epic.getStatus());
            System.out.println(" Epic Subtasks:");
            for (Subtask subtask : epic.getSubtasks()) {
                System.out.println(" Id - " + subtask.getId() + ", Name - " + subtask.getName() + ", Description - " + subtask.getDescription() + ", Status - " + subtask.getStatus() + ", Epic - " + subtask.getEpic().getName());
            }
            System.out.println();
        }
    }
}
