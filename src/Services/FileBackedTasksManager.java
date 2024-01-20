package Services;

import Models.*;

import javax.management.modelmbean.InvalidTargetObjectTypeException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTasksManager extends InMemoryTaskManager {
    String filePath = "src\\Autosave\\";
    boolean isFileCreated = false;

    FileBackedTasksManager(HistoryManager historyManager, String filePath) {
        super(historyManager);
        this.filePath += filePath;
    }

    public void createFile() {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Files.createFile(Paths.get(filePath));
                isFileCreated = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() { // будет сохранять текущее состояние менеджера в указанный файл.
        if(!isFileCreated) {
            createFile();
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(filePath)))) {
            bufferedWriter.write("id,type,name,status,description,epic\n");
            for (Task task : Stream.concat(getAllTasks().values().stream(), Stream.concat(getAllEpics().values().stream(), getAllSubtasks().values().stream())).collect(Collectors.toList())) {
                bufferedWriter.write(toString(task) + "\n");
            }
//            for (Task task : getAllTasks().values()) {
//                bufferedWriter.write(toString(task) + "\n");
//            }
//            for (Task task : getAllSubtasks().values()) {
//                bufferedWriter.write(toString(task) + "\n");
//            }
//            for (Task task : getAllEpics().values()) {
//                bufferedWriter.write(toString(task) + "\n");
//            }
            bufferedWriter.newLine();
            bufferedWriter.write(historyToString(historyManager));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString(Task task) {
        String taskString = String.format("%s,%s,%s,%s,%s,", task.getId(), task.getClass().getSimpleName().toUpperCase(),
                task.getName(), task.getStatus(), task.getDescription());
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            taskString += subtask.getEpicId();
        }
        return taskString;
    }

    public static String historyToString(HistoryManager historyManager) {
        List<Task> tl = historyManager.getHistory();
        if(!historyManager.getHistory().isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Task task : historyManager.getHistory()) {
                stringBuilder.append(task.getId()).append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            return stringBuilder.toString();
        }
        return "";
    }

    public static void putTaskToMap(Task task, FileBackedTasksManager fileBackedTasksManager) {
        if (task instanceof Epic) {
            fileBackedTasksManager.epicMap.put(task.getId(), (Epic) task);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            fileBackedTasksManager.epicMap.get(subtask.getEpicId()).addSubtask(subtask);
            fileBackedTasksManager.subtaskMap.put(subtask.getId(), subtask);
        } else if (task instanceof Task) {
            fileBackedTasksManager.taskMap.put(task.getId(), task);
        }
    }

    public static void fillHistoryManager(List<Integer> historyIDs, FileBackedTasksManager fileBackedTasksManager,
                                          HistoryManager historyManager) {
        historyIDs.stream()
                .flatMap(id -> Stream.of(
                        fileBackedTasksManager.taskMap.get(id),
                        fileBackedTasksManager.subtaskMap.get(id),
                        fileBackedTasksManager.epicMap.get(id)))
                .filter(Objects::nonNull)
                .forEach(historyManager::add);

//        for (Integer taskId : historyIDs) {
//            if (fileBackedTasksManager.taskMap.containsKey(taskId)) {
//                historyManager.add(fileBackedTasksManager.taskMap.get(taskId));
//            } else if (fileBackedTasksManager.subtaskMap.containsKey(taskId)) {
//                historyManager.add(fileBackedTasksManager.taskMap.get(taskId));
//            } else if (fileBackedTasksManager.epicMap.containsKey(taskId)) {
//                historyManager.add(fileBackedTasksManager.epicMap.get(taskId));
//            }
//        }
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(historyManager, file.getName());

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isBlank()) {
                    putTaskToMap(fromString(line), fileBackedTasksManager);
                } else {
                    while ((line = bufferedReader.readLine()) != null) {
                        fillHistoryManager(historyFromString(line), fileBackedTasksManager, historyManager);
                    }
                }
            }
            return fileBackedTasksManager;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidTargetObjectTypeException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Integer> historyFromString(String value) {
        String[] taskChars = value.split(",");
        List<Integer> historyList = new ArrayList<>();
        for (String taskId : taskChars) {
            historyList.add(Integer.parseInt(taskId));
        }
        return historyList;
    }

    public static Task fromString(String value) throws
            InvalidTargetObjectTypeException {
        String[] taskChars = value.split(",");
        TasksTypes taskType = TasksTypes.valueOf(taskChars[1]);
        switch (taskType) {
            case TASK:
                Task task = new Task(taskChars[2], taskChars[4], Status.valueOf(taskChars[3]));
                task.setId(Integer.parseInt(taskChars[0]));
                return task;
            case SUBTASK:
                Subtask subtask = new Subtask(taskChars[2], taskChars[4], Status.valueOf(taskChars[3]), Integer.parseInt(taskChars[5]));
                subtask.setId(Integer.parseInt(taskChars[0]));
                return subtask;
            case EPIC:
                Epic epic = new Epic(taskChars[2], taskChars[4]);
                epic.setId(Integer.parseInt(taskChars[0]));
                return epic;
            default:
                throw new InvalidTargetObjectTypeException(value);
        }
    }

    @Override
    public HashMap<Integer, Task> getAllTasks() {
        return super.getAllTasks();
    }

    @Override
    public HashMap<Integer, Subtask> getAllSubtasks() {
        return super.getAllSubtasks();
    }

    @Override
    public HashMap<Integer, Epic> getAllEpics() {
        return super.getAllEpics();
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    @Override
    Integer generateId() {
        return super.generateId();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeTaskById(Integer id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeSubtaskById(Integer id) {
        super.removeSubtaskById(id);
        save();
    }

    @Override
    public void removeEpicById(Integer id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }
}
