/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.Shape;
import city.cs.engine.World;
import java.awt.Color;
import org.jbox2d.dynamics.BodyType;

public class StaticBody
extends Body {
    private static final Color DEFAULT_FILL_COLOR = Color.GRAY;
    private static final Color DEFAULT_LINE_COLOR = Color.BLACK;

    public StaticBody(World w) {
        super(w, BodyType.STATIC);
        this.setFillColor(DEFAULT_FILL_COLOR);
        this.setLineColor(DEFAULT_LINE_COLOR);
    }

    public StaticBody(World w, Shape s) {
        super(w, s, BodyType.STATIC);
        this.setFillColor(DEFAULT_FILL_COLOR);
        this.setLineColor(DEFAULT_LINE_COLOR);
    }

    @Override
    float defaultDensity() {
        return 0.0f;
    }
}

