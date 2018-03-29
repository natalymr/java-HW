package ru.spbau.mit.dictionary;

public class ListNode {
    private String key;
    private String value;
    private ListNode next;

    ListNode(String key, String value) {
        this.key = key;
        this.value = value;
        this.next = null;
    }

    public boolean contains(String key) {
        if (this == null) {
            return false;
        }

        ListNode current = this;
        while (current != null) {
            if (current.key == key) {
                return true;
            }
            current = current.next;
        }

        return false;
    }

    // данная функция находит элемент в списке по ключу, меняет value на новое и возвращает ранее хранимое значение
    public String findAndReplace(String key, String newValue) {
        ListNode current = this;
        String returnedValue = null;

        while (current != null) {
            if (current.key == key) {

                returnedValue = current.value;
                current.value = newValue;
                break;
            }
            current = current.next;
        }

        return returnedValue;
    }

    // данная функция находит элемент в списке по ключу и возвращает хранимое значение
    // если такого элемента нет, возвращается null
    public String find(String key) {
        ListNode current = this;
        String returnedValue = null;

        while (current != null) {
            if (current.key == key) {

                returnedValue = current.value;
                break;
            }
            current = current.next;
        }

        return returnedValue;
    }


    public void add(String key, String value) {
        ListNode current = this;
        if (current == null) {
            System.out.println("add");
            current = new ListNode(key, value);
        }

        while (current.next != null) {
            current = current.next;
        }
        current.next = new ListNode(key, value);
    }

    // данная функция находит элемент в списке по ключу, удаляет его и возвращает ранее хранимое значение
    public String findAndRemove(String key) {
        String returnedValue = null;

        // случай, когда оказалось, что нужно удалить первый элемент
        // рассмотрели вне этой функции
        ListNode currentNode = this;
        ListNode nextNode = currentNode.next;

        while (nextNode.key != key) {
            currentNode = nextNode;
            nextNode = nextNode.next;
        }
        returnedValue = nextNode.value;
        currentNode.next = nextNode.next;

        return returnedValue;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public ListNode getNext() {
        return next;
    }

    public String[] getKeysOfAllList() {
        // узнаем размер текущего списка
        int size = 0;
        ListNode current = this;
        while (current != null) {
            size++;
            current = current.next;
        }

        String[] result = new String[size];
        current = this;
        for (int i = 0; i < size; i++) {
            result[i] = current.key;
            current = current.next;
        }

        return result;
    }

    public String[] getValuesOfAllList() {
        // узнаем размер текущего списка
        int size = 0;
        ListNode current = this;
        while (current != null) {
            size++;
            current = current.next;
        }

        String[] result = new String[size];
        current = this;
        for (int i = 0; i < size; i++) {
            result[i] = current.value;
            current = current.next;
        }

        return result;
    }
}
