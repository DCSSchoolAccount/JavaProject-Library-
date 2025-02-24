/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.StepEvent;
import java.util.EventListener;

public interface StepListener
extends EventListener {
    public void preStep(StepEvent var1);

    public void postStep(StepEvent var1);
}

