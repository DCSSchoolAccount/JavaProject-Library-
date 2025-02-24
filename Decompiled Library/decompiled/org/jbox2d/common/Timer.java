/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.common;

public class Timer {
    private long resetNanos;

    public Timer() {
        this.reset();
    }

    public void reset() {
        this.resetNanos = System.nanoTime();
    }

    public float getMilliseconds() {
        return (float)((System.nanoTime() - this.resetNanos) / 1000L) * 1.0f / 1000.0f;
    }
}

