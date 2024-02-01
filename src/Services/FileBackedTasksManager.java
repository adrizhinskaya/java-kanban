package Services;

import Exceptions.ManagerSaveException;
import Models.*;

import javax.management.modelmbean.InvalidTargetObjectTypeException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private String filePath = "src\\Autosave\\";
    private boolean isFileCreated = false;

    FileBackedTasksManager(HistoryManager historyManager, String filePath) {
        super(historyManager);
        this.filePath += filePath;
    }

    private void createFile() {
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

    private void save() {
        if (!isFileCreated) {
            createFile();
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(filePath)))) {
            bufferedWriter.write("id,type,name,status,description,startTime,duration,endTime,epic");
            for (Task task : Stream.concat(getAllTasks().values().stream(),
                            Stream.concat(getAllEpics().values().stream(),
                                    getAllSubtasks().values().stream()))
                    .collect(Collectors.toList())) {
                bufferedWriter.write("\n" + toString(task));
            }
            bufferedWriter.newLine();
            bufferedWriter.write(historyToString(historyManager));

        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    private String toString(Task task) {
        int id = task.getId();
        String type = task.getClass().getSimpleName().toUpperCase();
        String name = task.getName();
        Status status = task.getStatus();
        String description = task.getDescription();
        LocalDateTime startTime = task.getStartTime();
        Long duration = task.getDuration().toMinutes();
        LocalDateTime endTime = task.getEndTime();
        String taskString = String.format("%s,%s,%s,%s,%s,%s,%s,%s,", id, type, name, status, description, startTime,
                duration, endTime);
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            taskString += subtask.getEpicId();
        }
        return taskString;
    }

    private static String historyToString(HistoryManager historyManager) {
        List<Task> tl = historyManager.getHistory();
        if (!historyManager.getHistory().isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Task task : historyManager.getHistory()) {
                stringBuilder.append(task.getId()).append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            return "\n" + stringBuilder.toString();
        }
        return "";
    }

    public static FileBackedTasksManager loadFromFile(HistoryManager historyManager, File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(historyManager, file.getName());

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            bufferedReader.readLine();
            String line = bufferedReader.readLine();
            if (line == null) {
                return fileBackedTasksManager;
            }
            while (line != null) {
                if (!line.isBlank()) {
                    putTaskToMap(fromString(line), fileBackedTasksManager);
                    line = bufferedReader.readLine();
                    continue;
                }
                distributeSubtasksToEpics(fileBackedTasksManager);
                setIdGenerator(fileBackedTasksManager);
                fillHistoryManager(historyFromString(bufferedReader.readLine()), fileBackedTasksManager, historyManager);
                fillPrioritizedTasks(fileBackedTasksManager);
                return fileBackedTasksManager;
            }
            distributeSubtasksToEpics(fileBackedTasksManager);
            fillPrioritizedTasks(fileBackedTasksManager);
            setIdGenerator(fileBackedTasksManager);
            return fileBackedTasksManager;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidTargetObjectTypeException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setIdGenerator(FileBackedTasksManager fileBackedTasksManager) {
        List<Task> taskList = Stream.concat(fileBackedTasksManager.taskMap.values().stream(),
                Stream.concat(fileBackedTasksManager.epicMap.values().stream(),
                        fileBackedTasksManager.subtaskMap.values().stream())).collect(Collectors.toList());

        fileBackedTasksManager.id = taskList.stream()
                .max(Comparator.comparingInt(Task::getId))
                .get().getId();
    }

    private static void distributeSubtasksToEpics(FileBackedTasksManager fileBackedTasksManager) {
        for (Subtask s : fileBackedTasksManager.subtaskMap.values()) {
            fileBackedTasksManager.epicMap.get(s.getEpicId()).addSubtask(s);
        }
    }

    private static Task fromString(String value) throws
            InvalidTargetObjectTypeException {
        String[] taskChars = value.split(",");
        TasksTypes taskType = TasksTypes.valueOf(taskChars[1]);
        int id = Integer.parseInt(taskChars[0]);
        String name = taskChars[2];
        String description = taskChars[4];
        Status status = Status.valueOf(taskChars[3]);
        LocalDateTime startTime = LocalDateTime.parse(taskChars[5]);
        Duration duration = Duration.ofMinutes(Long.parseLong(taskChars[6]));
        switch (taskType) {
            case TASK:
                Task task = new Task(name, description, status, startTime, duration);
                task.setId(id);
                return task;
            case SUBTASK:
                Integer epicId = Integer.parseInt(taskChars[8]);
                Subtask subtask = new Subtask(name, description, status, epicId, startTime, duration);
                subtask.setId(id);
                return subtask;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                return epic;
            default:
                throw new InvalidTargetObjectTypeException(value);
        }
    }

    private static List<Integer> historyFromString(String value) {
        String[] taskChars = value.split(",");
        List<Integer> historyList = new ArrayList<>();
        for (String taskId : taskChars) {
            historyList.add(Integer.parseInt(taskId));
        }
        return historyList;
    }

    private static void putTaskToMap(Task task, FileBackedTasksManager fileBackedTasksManager) {
        if (task instanceof Epic) {
            fileBackedTasksManager.epicMap.put(task.getId(), (Epic) task);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            fileBackedTasksManager.subtaskMap.put(subtask.getId(), subtask);
        } else if (task instanceof Task) {
            fileBackedTasksManager.taskMap.put(task.getId(), task);
        }
    }

    private static void fillHistoryManager(List<Integer> historyIDs, FileBackedTasksManager fileBackedTasksManager,
                                           HistoryManager historyManager) {
        historyIDs.stream()
                .flatMap(id -> Stream.of(
                        fileBackedTasksManager.taskMap.get(id),
                        fileBackedTasksManager.subtaskMap.get(id),
                        fileBackedTasksManager.epicMap.get(id)))
                .filter(Objects::nonNull)
                .forEach(historyManager::add);
    }

    private static void fillPrioritizedTasks(FileBackedTasksManager fileBackedTasksManager) {
        fileBackedTasksManager.prioritizedTasks.addAll(
                Stream.of(fileBackedTasksManager.taskMap.values(), fileBackedTasksManager.subtaskMap.values())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())
        );
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
