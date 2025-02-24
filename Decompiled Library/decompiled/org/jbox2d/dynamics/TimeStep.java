/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics;

public class TimeStep {
    public float dt;
    public float inv_dt;
    public float dtRatio;
    public int velocityIterations;
    public int positionIterations;
    public boolean warmStarting;
}

