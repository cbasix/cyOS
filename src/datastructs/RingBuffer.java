package datastructs;

public class RingBuffer {
    private Object[] data;
    private int start = 0, end = 0, length;

    public RingBuffer(int length){
        this.length = length + 1;
        this.data = new Object[this.length];
    }

    public void push(Object o){
        data[end] = o;

        end++;
        end %= length;

        // start has to be one after -> buffer is full overwrite oldest one
        if (start == end){
            start++;
        }
    }

    public Object get(){
        Object o = data[start];
        if (count() > 0) {
            start++;
            start %= length;
        } else {
            return null;
        }
        return o;
    }

    // todo check & fix: thows out of range exception.
    public Object peekPushed(int i){
        if (i < count()) {
            return data[(end-1-i) % length];
        } else {
            return null;
        }
    }

    public int count(){
        return ((end + length)-start) % length;
    }
}
