package benchmarks.testcases;

import benchmarks.testcases.IncThread;

public class CyclicDemo {
    public int count = 0;
    public int leader;

    public CyclicDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        CyclicDemo shared = new CyclicDemo();
        IncThread[] threads = new IncThread[4];

        int i;
        for(i = 0; i < 4; ++i) {
            threads[i] = new IncThread(i, shared);
            threads[i].start();
        }

        for(i = 1; i < 4; ++i) {
            threads[i].join();
        }

        synchronized(shared) {
            assert shared.count == 4;

        }
    }
}