import Models.Epic;
import Models.Status;
import Models.Subtask;
import Models.Task;
import Services.FileBackedTasksManager;
import Services.Managers;
import Services.TaskManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Main {
    static TaskManager fileBackedTasksManager = Managers.getFileBackedTaskManager();
    public static void main(String[] args) {

        Task task1 = new Task("Task1", "Описание Task1", Status.NEW);
        Task task2 = new Task("Task2", "Описание Task2", Status.NEW);
        fileBackedTasksManager.createTask(task1);
        fileBackedTasksManager.createTask(task2);

        Epic epic1 = new Epic("Epic1", "Описание Epic1");
        Epic epic2 = new Epic("Epic2", "Описание Epic2");
        fileBackedTasksManager.createEpic(epic1);
        fileBackedTasksManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask1", "Описание Subtask1", Status.DONE, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask2", "Описание Subtask2", Status.NEW, epic1.getId());
        Subtask subtask3 = new Subtask("Subtask3", "Описание Subtask3", Status.IN_PROGRESS, epic2.getId());
        fileBackedTasksManager.createSubtask(subtask1);
        fileBackedTasksManager.createSubtask(subtask2);
        fileBackedTasksManager.createSubtask(subtask3);

        fileBackedTasksManager.getTaskById(task2.getId());
        fileBackedTasksManager.getTaskById(task2.getId());
        fileBackedTasksManager.getEpicById(epic1.getId());
        fileBackedTasksManager.getEpicById(epic2.getId());
        fileBackedTasksManager.getTaskById(task1.getId());
        fileBackedTasksManager.getSubtaskById(subtask1.getId());
        fileBackedTasksManager.getSubtaskById(subtask2.getId());
        fileBackedTasksManager.getTaskById(task1.getId());
        fileBackedTasksManager.getSubtaskById(subtask3.getId());
        fileBackedTasksManager.getSubtaskById(subtask3.getId());
        fileBackedTasksManager.getSubtaskById(subtask3.getId());
        fileBackedTasksManager.getSubtaskById(subtask3.getId());
        fileBackedTasksManager.getSubtaskById(subtask3.getId());

        //fileBackedTasksManager.removeAllSubtasks();
        fileBackedTasksManager.removeAllTasks();
        //fileBackedTasksManager.removeAllEpics();

        TaskManager fileBackedTasksManager2 = Managers.getTaskManagerFromFile(
                new File("src\\Autosave\\autosave_data.csv"));

    }

    public static void showAllTasks() {
        printTasks(fileBackedTasksManager.getAllTasks());
        printSubtasks(fileBackedTasksManager.getAllSubtasks());
        printEpics(fileBackedTasksManager.getAllEpics());
    }

    public static void printTasks(HashMap<Integer, Task> taskHashMap) {
        System.out.println("\u001B[31m" + "TASKS" + "\u001B[0m");
        for (Task task : taskHashMap.values()) {
            System.out.println("Id - " + task.getId() + ", Name - " + task.getName() + ", Description - "
                    + task.getDescription() + ", Models.Status - " + task.getStatus());
        }
    }

    public static void printSubtasks(HashMap<Integer, Subtask> subtaskHashMap) {
        System.out.println("\u001B[31m" + "SUBTASKS" + "\u001B[0m");
        for (Subtask subtask : subtaskHashMap.values()) {
            System.out.println("Id - " + subtask.getId() + " Name - " + subtask.getName() + ", Description - "
                    + subtask.getDescription() + ", Models.Status - " + subtask.getStatus() + ", Models.Epic - "
                    + fileBackedTasksManager.getEpicById(subtask.getEpicId()).getName());
        }
    }

    public static void printEpics(HashMap<Integer, Epic> epicHashMap) {
        System.out.println("\u001B[31m" + "EPICS" + "\u001B[0m");
        for (Epic epic : epicHashMap.values()) {
            System.out.println("Id - " + epic.getId() + ", Name - " + epic.getName() + ", Description - "
                    + epic.getDescription() + ", Models.Status - " + epic.getStatus());
            System.out.println(" Models.Epic Subtasks:");
            for (Subtask subtask : epic.getSubtasks()) {
                System.out.println(" Id - " + subtask.getId() + ", Name - " + subtask.getName() + ", Description - "
                        + subtask.getDescription() + ", Models.Status - " + subtask.getStatus() + ", Models.Epic - "
                        + fileBackedTasksManager.getEpicById(subtask.getEpicId()).getName());
            }
            System.out.println();
        }
    }
}
