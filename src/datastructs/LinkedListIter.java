package datastructs;

public class LinkedListIter {
    public static class Node {
        Node next;
        Node previous;
        Object data;
    }

    private Node first;
    private Node current;


    public boolean gotoFirst(){
        current = first;
        return current != null;
    }
    public boolean next(){
        if (current.next == null){
            return false;
        }
        current = current.next;
        return true;
    }

    public Object _get(){
        return current.data;
    }

    public static final boolean BEFORE_CURRENT = true;
    public static final boolean AFTER_CURRENT = false;

    public void _insert(Object data){
        _insert(data, AFTER_CURRENT);
    }

    public void _insert(Object data, boolean insertBefore){
        // create and link new node
        Node n = new Node();
        n.data = data;

        if (current == null){
            // totally first insert
            first = n;

            n.next = null;
            n.previous = null;

        } else {
            if (insertBefore) {
                n.previous = current.previous;
                n.next = current;
            } else {
                n.previous = current;
                n.next = current.next;
            }
        }
        // allways jump to the new inserted one
        current = n;

        // update incoming references on both sides
        if (n.previous != null){
            n.previous.next = n;
        }

        if (n.next != null){
            n.next.previous = n;
        }


    }

    public void removeCurrent(){
        // update both incomming references
        if (current.previous != null){
           current.previous.next = current.next;
        }
        if (current.next != null){
           current.next.previous = current.previous;
        }

        // give away first title ;)
        if (current == first){
            first = current.next;
        }

        // set current to the previous one or the next one if there is no previous
        if (current.previous != null){
            current = current.previous;
        } else {
            current = current.next;
        }

    }

    public Object peekPrevious(){
        return current.previous.data;
    }

    public Object peekNext(){
        return current.next.data;
    }

}
