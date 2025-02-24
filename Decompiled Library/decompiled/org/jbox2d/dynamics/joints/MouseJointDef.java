/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.joints;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;

public class MouseJointDef
extends JointDef {
    public final Vec2 target = new Vec2();
    public float maxForce;
    public float frequencyHz;
    public float dampingRatio;

    public MouseJointDef() {
        this.type = JointType.MOUSE;
        this.target.set(0.0f, 0.0f);
        this.maxForce = 0.0f;
        this.frequencyHz = 5.0f;
        this.dampingRatio = 0.7f;
    }
}

