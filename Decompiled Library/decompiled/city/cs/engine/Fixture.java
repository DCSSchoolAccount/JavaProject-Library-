/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.Shape;
import java.awt.BasicStroke;
import java.awt.geom.Area;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.FixtureDef;

public abstract class Fixture {
    private Shape shape;
    org.jbox2d.dynamics.Fixture b2fixture;
    private Body body;

    Fixture(Body body, Shape shape, FixtureDef def) {
        this.shape = shape;
        def.shape = shape.getBox2DShape();
        this.b2fixture = body.getBox2DBody().createFixture(def);
        this.body = body;
        this.b2fixture.setUserData(this);
    }

    static Fixture getGameFixture(org.jbox2d.dynamics.Fixture s) {
        if (s == null) {
            return null;
        }
        Object userData = s.getUserData();
        if (userData != null && userData instanceof Fixture) {
            return (Fixture)userData;
        }
        return null;
    }

    public void destroy() {
        this.body.getWorld().removeCollisions(this);
        this.b2fixture.setUserData(null);
        this.body.getBox2DBody().destroyFixture(this.b2fixture);
        this.b2fixture.destroy();
        this.b2fixture = null;
        this.body = null;
        this.shape = null;
    }

    public void setDensity(float density) {
        this.b2fixture.setDensity(density);
    }

    public float getDensity() {
        return this.b2fixture.getDensity();
    }

    public Body getBody() {
        return this.body;
    }

    public boolean contains(Vec2 p) {
        return this.b2fixture.testPoint(p);
    }

    Shape getShape() {
        return this.shape;
    }

    org.jbox2d.dynamics.Fixture getBox2DFixture() {
        return this.b2fixture;
    }

    Vec2 extremalPoint(Vec2 direction) {
        return this.shape.extremalPoint(this.getTransform(), direction);
    }

    java.awt.Shape awtShape() {
        return this.shape.awtShape(this.getTransform());
    }

    public boolean intersects(Fixture f) {
        Area testArea = (Area)f.awtShape();
        testArea.intersect((Area)this.awtShape());
        return !testArea.isEmpty();
    }

    public boolean intersects(Vec2 centre, float halfWidth, float halfHeight) {
        return this.awtShape().intersects(centre.x - halfWidth, centre.y - halfHeight, 2.0f * halfWidth, 2.0f * halfHeight);
    }

    Transform getTop(float x) {
        return this.shape.getTop(this.getTransform(), x);
    }

    Transform getBottom(float x) {
        return this.shape.getBottom(this.getTransform(), x);
    }

    private Transform getTransform() {
        return this.b2fixture.getBody().getTransform();
    }

    abstract boolean isVisible();

    abstract BasicStroke getOutlineStyle();
}

