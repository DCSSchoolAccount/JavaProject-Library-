/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.collision;

import org.jbox2d.collision.ManifoldPoint;
import org.jbox2d.common.Vec2;

public class Manifold {
    public final ManifoldPoint[] points = new ManifoldPoint[2];
    public final Vec2 localNormal;
    public final Vec2 localPoint;
    public ManifoldType type;
    public int pointCount;

    public Manifold() {
        for (int i = 0; i < 2; ++i) {
            this.points[i] = new ManifoldPoint();
        }
        this.localNormal = new Vec2();
        this.localPoint = new Vec2();
        this.pointCount = 0;
    }

    public Manifold(Manifold other) {
        this.localNormal = other.localNormal.clone();
        this.localPoint = other.localPoint.clone();
        this.pointCount = other.pointCount;
        this.type = other.type;
        for (int i = 0; i < 2; ++i) {
            this.points[i] = new ManifoldPoint(other.points[i]);
        }
    }

    public void set(Manifold cp) {
        for (int i = 0; i < cp.pointCount; ++i) {
            this.points[i].set(cp.points[i]);
        }
        this.type = cp.type;
        this.localNormal.set(cp.localNormal);
        this.localPoint.set(cp.localPoint);
        this.pointCount = cp.pointCount;
    }

    public static enum ManifoldType {
        CIRCLES,
        FACE_A,
        FACE_B;

    }
}

