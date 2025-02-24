/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

public abstract class Shape {
    private final org.jbox2d.collision.shapes.Shape b2shape;

    Shape(org.jbox2d.collision.shapes.Shape b2shape) {
        this.b2shape = b2shape;
    }

    org.jbox2d.collision.shapes.Shape getBox2DShape() {
        return this.b2shape;
    }

    abstract Vec2 extremalPoint(Transform var1, Vec2 var2);

    abstract java.awt.Shape awtShape(Transform var1);

    abstract Transform getTop(Transform var1, float var2);

    abstract Transform getBottom(Transform var1, float var2);
}

