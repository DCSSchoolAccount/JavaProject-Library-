/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

public class SimulationSettings {
    public static final int DEFAULT_FRAME_RATE = 24;
    public static final int DEFAULT_SIMULATION_RATE = 60;
    public static final int DEFAULT_VELOCITY_ITERATIONS = 8;
    public static final int DEFAULT_POSITION_ITERATIONS = 3;
    private final int fpsAverageCount = 100;
    private final long[] nanos;
    private final long nanoStart;
    private long frameCount;
    private float averagedFPS;
    private float totalFPS;
    private int velocityIterations = 8;
    private int positionIterations = 3;
    private int timeStepMilli;
    private final float simTimeStep;

    public SimulationSettings(int framesPerSecond) {
        this.timeStepMilli = SimulationSettings.timeStepMilliFromFrameRate(framesPerSecond);
        this.frameCount = 0L;
        this.nanos = new long[100];
        long nanosPerFrameGuess = (long)(1.0E9 / (double)this.getFrameRate());
        this.nanos[99] = System.nanoTime();
        for (int i = 98; i >= 0; --i) {
            this.nanos[i] = this.nanos[i + 1] - nanosPerFrameGuess;
        }
        this.nanoStart = System.nanoTime();
        this.simTimeStep = 0.016666668f;
    }

    void step() {
        ++this.frameCount;
        long now = System.nanoTime();
        int index = (int)(this.frameCount % 100L);
        this.averagedFPS = (float)(1.0E11 / (double)(now - this.nanos[index]));
        this.totalFPS = (float)((double)this.frameCount * 1.0E9 / (double)(now - this.nanoStart));
        this.nanos[index] = now;
    }

    private static int timeStepMilliFromFrameRate(int frameRate) {
        float delay = 1000.0f / (float)frameRate;
        int milliQuantum = 1;
        int q = (int)(delay / (float)milliQuantum) * milliQuantum;
        if (delay - (float)q >= (float)milliQuantum / 2.0f) {
            q += milliQuantum;
        }
        return q;
    }

    public float getSimTimeStep() {
        return this.simTimeStep;
    }

    public int getFrameRate() {
        return Math.round(1000.0f / (float)this.timeStepMilli);
    }

    public long getFrameCount() {
        return this.frameCount;
    }

    public void setTargetFrameRate(int framesPerSecond) {
        this.timeStepMilli = SimulationSettings.timeStepMilliFromFrameRate(framesPerSecond);
    }

    public float getTimeStep() {
        return (float)this.timeStepMilli / 1000.0f;
    }

    public int getTimeStepMilli() {
        return this.timeStepMilli;
    }

    public int getVelocityIterations() {
        return this.velocityIterations;
    }

    public void setVelocityIterations(int n) {
        this.velocityIterations = n;
    }

    public int getPositionIterations() {
        return this.positionIterations;
    }

    public void setPositionIterations(int n) {
        this.positionIterations = n;
    }

    public int getFpsAverageCount() {
        return 100;
    }

    public float getAveragedFPS() {
        return this.averagedFPS;
    }

    public float getTotalFPS() {
        return this.totalFPS;
    }
}

