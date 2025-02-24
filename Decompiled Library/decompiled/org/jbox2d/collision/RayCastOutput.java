/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.collision;

import org.jbox2d.common.Vec2;

public class RayCastOutput {
    public final Vec2 normal = new Vec2();
    public float fraction = 0.0f;

    public void set(RayCastOutput rco) {
        this.normal.set(rco.normal);
        this.fraction = rco.fraction;
    }
}

