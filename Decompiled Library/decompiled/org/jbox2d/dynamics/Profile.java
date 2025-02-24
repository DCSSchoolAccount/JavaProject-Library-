/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics;

import java.util.List;

public class Profile {
    public float step;
    public float collide;
    public float solve;
    public float solveInit;
    public float solveVelocity;
    public float solvePosition;
    public float broadphase;
    public float solveTOI;

    public void toDebugStrings(List<String> strings) {
        strings.add("Profile:");
        strings.add(" step: " + this.step);
        strings.add("  collide: " + this.collide);
        strings.add("  solve: " + this.solve);
        strings.add("   solveInit: " + this.solveInit);
        strings.add("   solveVelocity: " + this.solveVelocity);
        strings.add("   solvePosition: " + this.solvePosition);
        strings.add("   broadphase: " + this.broadphase);
        strings.add("  solveTOI: " + this.solveTOI);
    }
}

