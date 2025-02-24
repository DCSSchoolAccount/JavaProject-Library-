/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.collision.shapes;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

public abstract class Shape {
    public final ShapeType m_type;
    public float m_radius;

    public Shape(ShapeType type) {
        this.m_type = type;
    }

    public ShapeType getType() {
        return this.m_type;
    }

    public float getRadius() {
        return this.m_radius;
    }

    public void setRadius(float radius) {
        this.m_radius = radius;
    }

    public abstract int getChildCount();

    public abstract boolean testPoint(Transform var1, Vec2 var2);

    public abstract boolean raycast(RayCastOutput var1, RayCastInput var2, Transform var3, int var4);

    public abstract void computeAABB(AABB var1, Transform var2, int var3);

    public abstract void computeMass(MassData var1, float var2);

    public abstract Shape clone();
}

