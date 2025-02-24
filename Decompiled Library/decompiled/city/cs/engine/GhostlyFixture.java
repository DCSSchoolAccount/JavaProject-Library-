/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.Fixture;
import city.cs.engine.Shape;
import java.awt.BasicStroke;
import org.jbox2d.dynamics.FixtureDef;

public class GhostlyFixture
extends Fixture {
    private static final float[] DASH1 = new float[]{4.0f};
    private static final BasicStroke stroke = new BasicStroke(1.0f, 0, 0, 1.0f, DASH1, 0.0f);

    public GhostlyFixture(Body body, Shape shape) {
        this(body, shape, body.defaultDensity());
    }

    public GhostlyFixture(Body body, Shape shape, float density) {
        super(body, shape, GhostlyFixture.makeFixtureDef(density));
    }

    private static FixtureDef makeFixtureDef(float density) {
        FixtureDef def = new FixtureDef();
        def.density = density;
        def.filter.categoryBits = 2;
        def.filter.maskBits = 0;
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
}

