import Services.HttpTaskManager;
import Api.KVServer;
import Models.Epic;
import Models.Status;
import Models.Subtask;
import Models.Task;
import Services.Managers;
import Services.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        new KVServer().start();
        TaskManager httpTaskManager = Managers.getDefault();

        Task task1 = new Task("Task1", "Описание Task1", Status.NEW,
                LocalDateTime.of(2021, 1, 1, 0, 0),
                Duration.ofMinutes(60));
        Task task2 = new Task("Task2", "Описание Task2", Status.NEW,
                LocalDateTime.of(2022, 2, 2, 0, 0),
                Duration.ofMinutes(120));
        httpTaskManager.createTask(task1);
        httpTaskManager.createTask(task2);

        Epic epic1 = new Epic("Epic1", "Описание Epic1");
        Epic epic2 = new Epic("Epic2", "Описание Epic2");
        httpTaskManager.createEpic(epic1);
        httpTaskManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask1", "Описание Subtask1", Status.DONE, epic1.getId(),
                LocalDateTime.of(1991, 1, 1, 0, 0), Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask("Subtask2", "Описание Subtask2", Status.NEW, epic1.getId(),
                LocalDateTime.of(1992, 2, 2, 0, 0), Duration.ofMinutes(120));
        Subtask subtask3 = new Subtask("Subtask3", "Описание Subtask3", Status.IN_PROGRESS, epic2.getId(),
                LocalDateTime.of(1993, 3, 3, 0, 0), Duration.ofMinutes(180));
        httpTaskManager.createSubtask(subtask1);
        httpTaskManager.createSubtask(subtask2);
        httpTaskManager.createSubtask(subtask3);

        //httpTaskManager.removeEpicById(epic1.getId());

//        Set<Task> tasks = httpTaskManager.getPrioritizedTasks();
//        for(Task t : tasks) {
//            System.out.println(t.getName() + " " + t.getStartTime());
//        }

        httpTaskManager.getTaskById(task2.getId());
        httpTaskManager.getEpicById(epic1.getId());
        httpTaskManager.getEpicById(epic2.getId());
        httpTaskManager.getTaskById(task1.getId());
        httpTaskManager.getSubtaskById(subtask1.getId());
        httpTaskManager.getSubtaskById(subtask2.getId());
        httpTaskManager.getTaskById(task1.getId());
        httpTaskManager.getSubtaskById(subtask3.getId());
        httpTaskManager.getSubtaskById(subtask3.getId());
        httpTaskManager.getSubtaskById(subtask3.getId());
        httpTaskManager.getSubtaskById(subtask3.getId());
        httpTaskManager.getSubtaskById(subtask3.getId());

////        //httpTaskManager.removeAllSubtasks();
////        httpTaskManager.removeAllTasks();
////        //httpTaskManager.removeAllEpics();
////
        TaskManager httpTaskManager2 = HttpTaskManager.load();
        System.out.println("hkjy");

    }

//    public static void showAllTasks() {
//        printTasks(httpTaskManager.getAllTasks());
//        printSubtasks(httpTaskManager.getAllSubtasks());
//        printEpics(httpTaskManager.getAllEpics());
//    }

//    public static void printTasks(HashMap<Integer, Task> taskHashMap) {
//        System.out.println("\u001B[31m" + "TASKS" + "\u001B[0m");
//        for (Task task : taskHashMap.values()) {
//            System.out.println("Id - " + task.getId() + ", Name - " + task.getName() + ", Description - "
//                    + task.getDescription() + ", Models.Status - " + task.getStatus());
//        }
//    }
//
//    public static void printSubtasks(HashMap<Integer, Subtask> subtaskHashMap) {
//        System.out.println("\u001B[31m" + "SUBTASKS" + "\u001B[0m");
//        for (Subtask subtask : subtaskHashMap.values()) {
//            System.out.println("Id - " + subtask.getId() + " Name - " + subtask.getName() + ", Description - "
//                    + subtask.getDescription() + ", Models.Status - " + subtask.getStatus() + ", Models.Epic - "
//                    + httpTaskManager.getEpicById(subtask.getEpicId()).getName());
//        }
//    }
//
//    public static void printEpics(HashMap<Integer, Epic> epicHashMap) {
//        System.out.println("\u001B[31m" + "EPICS" + "\u001B[0m");
//        for (Epic epic : epicHashMap.values()) {
//            System.out.println("Id - " + epic.getId() + ", Name - " + epic.getName() + ", Description - "
//                    + epic.getDescription() + ", Models.Status - " + epic.getStatus());
//            System.out.println(" Models.Epic Subtasks:");
//            for (Subtask subtask : epic.getSubtasks()) {
//                System.out.println(" Id - " + subtask.getId() + ", Name - " + subtask.getName() + ", Description - "
//                        + subtask.getDescription() + ", Models.Status - " + subtask.getStatus() + ", Models.Epic - "
//                        + httpTaskManager.getEpicById(subtask.getEpicId()).getName());
//            }
//            System.out.println();
//        }
//    }
}
