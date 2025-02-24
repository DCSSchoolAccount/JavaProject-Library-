/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

public class BodyDef {
    public BodyType type;
    public Object userData = null;
    public Vec2 position = new Vec2();
    public float angle = 0.0f;
    public Vec2 linearVelocity = new Vec2();
    public float angularVelocity = 0.0f;
    public float linearDamping = 0.0f;
    public float angularDamping = 0.0f;
    public boolean allowSleep = true;
    public boolean awake = true;
    public boolean fixedRotation = false;
    public boolean bullet = false;
    public boolean active = true;
    public float gravityScale = 1.0f;

    public BodyDef() {
        this.type = BodyType.STATIC;
    }
}

