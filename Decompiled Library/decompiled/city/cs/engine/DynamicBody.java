/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.Shape;
import city.cs.engine.World;
import java.awt.Color;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

public class DynamicBody
extends Body {
    private static final Color DEFAULT_FILL_COLOR = new Color(245, 222, 179);
    private static final Color DEFAULT_LINE_COLOR = new Color(139, 69, 19);

    public DynamicBody(World w) {
        super(w, BodyType.DYNAMIC);
        this.setFillColor(DEFAULT_FILL_COLOR);
        this.setLineColor(DEFAULT_LINE_COLOR);
    }

    public DynamicBody(World w, Shape s) {
        super(w, s, BodyType.DYNAMIC);
        this.setFillColor(DEFAULT_FILL_COLOR);
        this.setLineColor(DEFAULT_LINE_COLOR);
    }

    public void setLinearVelocity(Vec2 velocity) {
        this.b2body.setLinearVelocity(new Vec2(velocity));
        this.b2body.setAwake(true);
    }

    public void setAngularVelocity(float angularVelocity) {
        this.b2body.setAngularVelocity(angularVelocity);
        this.b2body.setAwake(true);
    }

    public void applyImpulse(Vec2 impulse) {
        this.b2body.applyLinearImpulse(impulse, this.b2body.getWorldCenter());
    }

    public void applyImpulse(Vec2 impulse, Vec2 point) {
        this.b2body.applyLinearImpulse(impulse, point);
    }

    public void applyForce(Vec2 force) {
        this.b2body.applyForce(force, this.b2body.getWorldCenter());
    }

    public void applyForce(Vec2 force, Vec2 point) {
        this.b2body.applyForce(force, point);
    }

    public void applyTorque(float torque) {
        this.b2body.applyTorque(torque);
    }

    public void setBullet(boolean b) {
        this.b2body.setBullet(b);
    }

    public boolean isBullet() {
        return this.b2body.isBullet();
    }

    public void setGravityScale(float g) {
        this.b2body.setGravityScale(g);
    }

    public float getGravityScale() {
        return this.b2body.getGravityScale();
    }

    @Override
    float defaultDensity() {
        return 1.0f;
    }
}

