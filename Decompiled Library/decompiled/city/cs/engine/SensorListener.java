/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.SensorEvent;
import java.util.EventListener;

public interface SensorListener
extends EventListener {
    public void beginContact(SensorEvent var1);

    public void endContact(SensorEvent var1);
}

