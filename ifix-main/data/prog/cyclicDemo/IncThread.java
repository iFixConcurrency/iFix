package cyclicDemo;

class IncThread extends Thread {
    private int id;
    private CyclicDemo shared;

    public IncThread(int id, CyclicDemo shared) {
        this.id = id;
        this.shared = shared;
    }

    public void run() {
        CyclicDemo var1 = this.shared;
        synchronized(this.shared) {
            ++this.shared.count;
        }

        this.shared.leader = this.id;
    }
}