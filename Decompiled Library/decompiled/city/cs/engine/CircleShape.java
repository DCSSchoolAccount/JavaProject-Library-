/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

public class CircleShape
extends Shape {
    private org.jbox2d.collision.shapes.CircleShape b2circle;

    private CircleShape(org.jbox2d.collision.shapes.CircleShape shape) {
        super(shape);
        this.b2circle = shape;
    }

    public CircleShape(float radius, float x, float y) {
        this(CircleShape.circle(radius, x, y));
    }

    public CircleShape(float radius, Vec2 centre) {
        this(CircleShape.circle(radius, centre.x, centre.y));
    }

    public CircleShape(float radius) {
        this(CircleShape.circle(radius, 0.0f, 0.0f));
    }

    private static org.jbox2d.collision.shapes.CircleShape circle(float radius, float x, float y) {
        org.jbox2d.collision.shapes.CircleShape circleShape = new org.jbox2d.collision.shapes.CircleShape();
        circleShape.m_radius = radius;
        circleShape.m_p.set(new Vec2(x, y));
        return circleShape;
    }

    private Vec2 getCentre() {
        return this.b2circle.m_p;
    }

    private float getRadius() {
        return this.b2circle.getRadius();
    }

    @Override
    Vec2 extremalPoint(Transform transform, Vec2 direction) {
        Vec2 centre = Transform.mul(transform, this.getCentre());
        float r = this.getRadius();
        return centre.add(direction.mul(r));
    }

    @Override
    java.awt.Shape awtShape(Transform transform) {
        Vec2 centre = Transform.mul(transform, this.getCentre());
        float r = this.getRadius();
        float x = centre.x - r;
        float y = centre.y - r;
        return new Ellipse2D.Float(x, y, 2.0f * r, 2.0f * r);
    }

    java.awt.Shape awtRadius(Transform transform) {
        Vec2 c = this.getCentre();
        float r = this.getRadius();
        Vec2 centre = Transform.mul(transform, this.getCentre());
        Vec2 axis = Transform.mul(transform, c.add(new Vec2(r, 0.0f)));
        return new Line2D.Float(centre.x, centre.y, axis.x, axis.y);
    }

    @Override
    Transform getTop(Transform transform, float x) {
        Vec2 centre = Transform.mul(transform, this.getCentre());
        float dx = x - centre.x;
        float r = this.getRadius();
        if (dx < -r || dx > r) {
            return null;
        }
        float dy = (float)Math.sqrt(r * r - dx * dx);
        float theta = -((float)Math.atan2(dx, dy));
        float y = centre.y + dy;
        return new Transform(new Vec2(x, y), new Rot(theta));
    }

    @Override
    Transform getBottom(Transform transform, float x) {
        Vec2 centre = Transform.mul(transform, this.getCentre());
        float dx = x - centre.x;
        float r = this.getRadius();
        if (dx < -r || dx > r) {
            return null;
        }
        float dy = (float)Math.sqrt(r * r - dx * dx);
        float theta = -((float)Math.atan2(-dx, dy));
        float y = centre.y - dy;
        return new Transform(new Vec2(x, y), new Rot(theta));
    }
}

