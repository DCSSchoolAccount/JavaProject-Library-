/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.Fixture;
import city.cs.engine.Shape;
import java.awt.BasicStroke;
import org.jbox2d.dynamics.FixtureDef;

public class SolidFixture
extends Fixture {
    private static final BasicStroke stroke = new BasicStroke();

    public SolidFixture(Body body, Shape shape) {
        this(body, shape, body.defaultDensity());
    }

    public SolidFixture(Body body, Shape shape, float density) {
        super(body, shape, SolidFixture.makeFixtureDef(density));
    }

    private static FixtureDef makeFixtureDef(float density) {
        FixtureDef def = new FixtureDef();
        def.density = density;
        def.friction = 0.2f;
        def.restitution = 0.0f;
        return def;
    }

    @Override
    boolean isVisible() {
        return true;
    }

    @Override
    BasicStroke getOutlineStyle() {
        return stroke;
    }

    public void setFriction(float friction) {
        this.b2fixture.setFriction(friction);
    }

    public float getFriction() {
        return this.b2fixture.getFriction();
    }

    public void setRestitution(float restitution) {
        this.b2fixture.setRestitution(restitution);
    }

    public float getRestitution() {
        return this.b2fixture.getRestitution();
    }
}

