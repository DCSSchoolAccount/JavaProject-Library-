/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.profile;

public abstract class BasicPerformanceTest {
    private final int numTests;
    private final int iters;
    protected final long[] times;

    public BasicPerformanceTest(int numTests, int iters) {
        this.numTests = numTests;
        this.iters = iters;
        this.times = new long[numTests];
        for (int i = 0; i < numTests; ++i) {
            this.times[i] = 0L;
        }
    }

    public double getTestTime(int testNum) {
        return (double)this.times[testNum] * 1.0 / 1000000.0;
    }

    public void go() {
        for (int i = 0; i < this.iters; ++i) {
            this.println((double)i * 100.0 / (double)this.iters + "%");
            int test = 0;
            while (test < this.numTests) {
                long prev = System.nanoTime();
                this.runTest(test);
                long after = System.nanoTime();
                int n = test++;
                this.times[n] = this.times[n] + (after - prev);
            }
        }
        int test = 0;
        while (test < this.numTests) {
            int n = test++;
            this.times[n] = this.times[n] / (long)this.iters;
        }
        this.printResults();
    }

    public void printResults() {
        this.printf("%-20s%20s%20s\n", "Test Name", "Milliseconds Avg", "FPS (optional)");
        for (int i = 0; i < this.numTests; ++i) {
            double milliseconds = (double)this.times[i] * 1.0 / 1000000.0;
            if (this.getFrames(i) != 0) {
                double fps = (double)this.getFrames(i) * 1000.0 / milliseconds;
                this.printf("%-20s%20.4f%20.4f\n", this.getTestName(i), milliseconds, fps);
                continue;
            }
            this.printf("%-20s%20.4f\n", this.getTestName(i), milliseconds);
        }
    }

    public abstract void runTest(int var1);

    public abstract String getTestName(int var1);

    public int getFrames(int testNum) {
        return 0;
    }

    public void println(String s) {
        System.out.println(s);
    }

    public void printf(String s, Object ... args) {
        System.out.printf(s, args);
    }
}

