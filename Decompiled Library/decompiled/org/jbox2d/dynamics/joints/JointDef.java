/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.joints;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.JointType;

public class JointDef {
    public JointType type = JointType.UNKNOWN;
    public Object userData = null;
    public Body bodyA = null;
    public Body bodyB = null;
    public boolean collideConnected = false;
}

