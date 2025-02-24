/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.DynamicBody;
import city.cs.engine.Shape;
import city.cs.engine.SolidFixture;
import city.cs.engine.StepEvent;
import city.cs.engine.StepListener;
import city.cs.engine.World;
import org.jbox2d.common.Vec2;

public class Walker
extends DynamicBody {
    private final Legs legs;
    private boolean walking;

    public Walker(World world) {
        super(world);
        this.b2body.setFixedRotation(true);
        this.legs = new Legs();
        this.walking = false;
    }

    public Walker(World world, Shape shape) {
        this(world);
        SolidFixture fixture = new SolidFixture((Body)this, shape, 5.0f);
        fixture.setFriction(1.0f);
    }

    public void jump(float speed) {
        Vec2 v = this.getLinearVelocity();
        if (Math.abs(v.y) < 0.01f) {
            this.setLinearVelocity(new Vec2(v.x, speed));
        }
    }

    public void startWalking(float speed) {
        if (!this.walking) {
            this.getWorld().addStepListener(this.legs);
            this.walking = true;
        }
        this.legs.setSpeed(speed);
    }

    public void stopWalking() {
        if (this.walking) {
            this.getWorld().removeStepListener(this.legs);
            this.walking = false;
        }
    }

    @Override
    public void destroy() {
        this.stopWalking();
        super.destroy();
    }

    private class Legs
    implements StepListener {
        private float speed = 0.0f;

        public void setSpeed(float speed) {
            this.speed = speed;
        }

        @Override
        public void preStep(StepEvent e) {
            Vec2 v = Walker.this.getLinearVelocity();
            Walker.this.setLinearVelocity(new Vec2(this.speed, v.y));
        }

        @Override
        public void postStep(StepEvent e) {
        }
    }
}

