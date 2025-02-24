/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.joints;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.Joint;

public class JointEdge {
    public Body other = null;
    public Joint joint = null;
    public JointEdge prev = null;
    public JointEdge next = null;
}

