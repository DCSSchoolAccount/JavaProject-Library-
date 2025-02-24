/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.SolidFixture;
import java.util.EventObject;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.Contact;

public class CollisionEvent
extends EventObject {
    private final SolidFixture reporter;
    private final SolidFixture other;
    private Vec2 position;
    private Vec2 velocity;
    private Vec2 normal;
    private float friction;
    private float restitution;

    private CollisionEvent(SolidFixture reporter, SolidFixture other) {
        super(reporter.getBody());
        this.reporter = reporter;
        this.other = other;
    }

    CollisionEvent(SolidFixture reporter, SolidFixture other, Contact contact) {
        this(reporter, other);
        this.velocity = other.getBody().getLinearVelocity().sub(reporter.getBody().getLinearVelocity());
        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);
        this.position = worldManifold.points[0].clone();
        this.normal = worldManifold.normal.clone();
        this.friction = contact.getFriction();
        this.restitution = contact.getRestitution();
    }

    @Override
    public String toString() {
        String s = super.toString();
        s = s + "[" + this.getReportingBody() + "<-" + this.getOtherBody() + "]";
        return s;
    }

    public Body getOtherBody() {
        return this.getOtherFixture().getBody();
    }

    public Body getReportingBody() {
        return this.getReportingFixture().getBody();
    }

    public SolidFixture getOtherFixture() {
        return this.other;
    }

    public SolidFixture getReportingFixture() {
        return this.reporter;
    }

    public Vec2 getPosition() {
        return this.position;
    }

    public Vec2 getVelocity() {
        return this.velocity;
    }

    public Vec2 getNormal() {
        return this.normal;
    }

    public float getFriction() {
        return this.friction;
    }

    public float getRestitution() {
        return this.restitution;
    }

    CollisionEvent inverse() {
        CollisionEvent ce = new CollisionEvent(this.other, this.reporter);
        ce.position = this.position;
        ce.velocity = this.velocity.negate();
        ce.normal = this.normal.negate();
        ce.friction = this.friction;
        ce.restitution = this.restitution;
        return ce;
    }

    void dispatch() {
        this.getReportingBody().collide(this);
    }
}

