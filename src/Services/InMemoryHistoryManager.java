package Services;

import Models.Node;
import Models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList<Task> viewedTasks = new CustomLinkedList<>();
    Map<Integer, Node<Task>> viewedTasksMap = new HashMap<>();

    @Override
    public void add(Task task) {
        if (viewedTasksMap.containsKey(task.getId())) {
            remove(task.getId());
        }
        viewedTasks.linkLast(task);
        viewedTasksMap.put(task.getId(), viewedTasks.tail);
    }

    @Override
    public void remove(int id) {
        Node<Task> nodeToDelete = viewedTasksMap.get(id);
        if (nodeToDelete != null) {
            viewedTasks.removeNode(nodeToDelete);
        }
    }

    @Override
    public List<Task> getHistory() {
        return viewedTasks.getTasks();
    }

    public static class CustomLinkedList<T> {
        private Node<T> head;
        private Node<T> tail;
        private int size = 0;

        void linkLast(T task) {
            final Node<T> oldTail = tail;
            final Node<T> newNode = new Node<>(oldTail, task, null);
            tail = newNode;
            if (oldTail == null) {
                head = newNode;
            } else {
                oldTail.next = newNode;
            }
            size++;
        }

        void removeFirst() {
            Node<T> nextHead = head.next;
            if (nextHead != null) {
                nextHead.prev = null;
                head.next = null;
                head = nextHead;
            } else {
                head = null;
                tail = null;
            }
            size--;
        }

        void removeLast() {
            Node<T> prevTail = tail.prev;
            prevTail.next = null;
            tail.prev = null;
            tail = prevTail;
            size--;
        }

        public void removeNode(Node<T> node) {
            if (head.equals(node)) {
                removeFirst();
            } else if (tail.equals(node)) {
                removeLast();
            } else {
                Node<T> prevNode = node.prev;
                Node<T> nextNode = node.next;
                prevNode.next = nextNode;
                nextNode.prev = prevNode;
                node.prev = null;
                node.next = null;
                size--;
            }
        }

        List<T> getTasks() {
            ArrayList<T> tasksList = new ArrayList<>();
            if (head == null) {
                return tasksList;
            }
            Node<T> currentNode = head;
            while (currentNode != null) {
                tasksList.add(currentNode.data);
                currentNode = currentNode.next;
            }

            return tasksList;
        }

        int size() {
            return this.size;
        }
    }
}
