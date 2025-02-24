/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.callbacks;

import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.Joint;

public interface DestructionListener {
    public void sayGoodbye(Joint var1);

    public void sayGoodbye(Fixture var1);
}

