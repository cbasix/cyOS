package datastructs;

public class LinkedList {
    private Node first;

    private static class Node {
        Node next;
        Node previous;
        Object data;
    }

    public class Iterator {
        Node current = null;

        public void gotoStart(){
            current = null;
        }

        public boolean next(){
            if (current == null){
                if (first == null){
                    // list is empty
                    return false;
                }

                // go to first element since we were in front of it
                current = first;
                return true;
            }

            if (current.next == null){
                return false;
            }

            current = current.next;
            return true;
        }

        public Object _get(){
            if (current == null){
                return null;
            }
            return current.data;
        }

        public void _insert(Object data){
            // create and link new node
            Node n = new Node();
            n.data = data;

            if (first == null){
                // totally first insert
                first = n;

                n.next = null;
                n.previous = null;

            } else if (current == null){
                // before first element
                n.next = first.next;
                n.previous = null;

                first = n;

            }else {
                // normal in middle
                n.previous = current;
                n.next = current.next; // may be null on last element but that is what we want
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

        public Object _peekPrevious(){
            if (first == null || current == null){
                // before first element or empty list
                return null;
            }
            return current.previous.data;
        }

        public Object _peekNext(){
            if (first == null){
                // list is empty
                return null;
            }

            if (current == null){
                // we are before first element
                return first.data;
            }
            return current.next.data;
        }
    }

    public Iterator _iter(){
        return new Iterator();
    }
}
