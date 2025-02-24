/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.joints;

import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;

public class GearJointDef
extends JointDef {
    public Joint joint1;
    public Joint joint2;
    public float ratio;

    public GearJointDef() {
        this.type = JointType.GEAR;
        this.joint1 = null;
        this.joint2 = null;
    }
}

