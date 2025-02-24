/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.joints;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;

public class RopeJointDef
extends JointDef {
    public final Vec2 localAnchorA = new Vec2();
    public final Vec2 localAnchorB = new Vec2();
    public float maxLength;

    public RopeJointDef() {
        this.type = JointType.ROPE;
        this.localAnchorA.set(-1.0f, 0.0f);
        this.localAnchorB.set(1.0f, 0.0f);
    }
}

