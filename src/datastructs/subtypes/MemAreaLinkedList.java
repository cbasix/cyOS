package datastructs.subtypes;

import datastructs.LinkedList;
import kernel.memory.MemArea;

public class MemAreaLinkedList extends LinkedList {
    public class MemAreaIterator extends LinkedList.Iterator {
        public MemArea get() {
            return (MemArea) super._get();
        }

        public void insert(MemArea o) {
            super._insert(o);
        }

        public MemArea peekPrevious() {
            return (MemArea) super._peekPrevious();
        }

        public MemArea peekNext() {
            return (MemArea) super._peekNext();
        }
    }

    public MemAreaIterator iter() {
        return new MemAreaIterator();
    }
}
