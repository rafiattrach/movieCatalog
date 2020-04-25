package network.net;

public class RW {
    /* A simple read/write lock so that multiple threads can read from the database
     but only one can add to it at a single period of time (synchronization) */

    private int countReaders = 0;

    public synchronized void startRead() throws InterruptedException {
        while (countReaders < 0) wait();
        countReaders++;
    }

    public synchronized void endRead() {
        countReaders--;
        if (countReaders == 0)
            notify();
    }

    public synchronized void startWrite() throws InterruptedException {
        while (countReaders != 0)
            wait();
        countReaders = -1;
    }

    public synchronized void endWrite() {
        countReaders = 0;
        notifyAll();
    }
}
