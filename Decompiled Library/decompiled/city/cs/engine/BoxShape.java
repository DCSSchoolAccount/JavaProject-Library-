/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.PolygonShape;
import org.jbox2d.common.Vec2;

public class BoxShape
extends PolygonShape {
    public BoxShape(float halfWidth, float halfHeight) {
        super(BoxShape.box(halfWidth, halfHeight));
    }

    public BoxShape(float halfWidth, float halfHeight, Vec2 centre) {
        super(BoxShape.box(halfWidth, halfHeight, centre, 0.0f));
    }

    public BoxShape(float halfWidth, float halfHeight, Vec2 centre, float angle) {
        super(BoxShape.box(halfWidth, halfHeight, centre, angle));
    }

    private static org.jbox2d.collision.shapes.PolygonShape box(float halfWidth, float halfHeight) {
        org.jbox2d.collision.shapes.PolygonShape polyShape = new org.jbox2d.collision.shapes.PolygonShape();
        polyShape.setAsBox(halfWidth, halfHeight);
        return polyShape;
    }

    private static org.jbox2d.collision.shapes.PolygonShape box(float halfWidth, float halfHeight, Vec2 centre, float angle) {
        org.jbox2d.collision.shapes.PolygonShape polyShape = new org.jbox2d.collision.shapes.PolygonShape();
        polyShape.setAsBox(halfWidth, halfHeight, centre, angle);
        return polyShape;
    }
}

