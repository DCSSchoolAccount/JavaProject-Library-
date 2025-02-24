/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.callbacks;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

public interface RayCastCallback {
    public float reportFixture(Fixture var1, Vec2 var2, Vec2 var3, float var4);
}

