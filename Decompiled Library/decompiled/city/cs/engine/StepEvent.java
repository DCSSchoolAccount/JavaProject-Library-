/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import java.util.EventObject;

public class StepEvent
extends EventObject {
    private final float step;

    StepEvent(Object source, float step) {
        super(source);
        this.step = step;
    }

    public float getStep() {
        return this.step;
    }
}

