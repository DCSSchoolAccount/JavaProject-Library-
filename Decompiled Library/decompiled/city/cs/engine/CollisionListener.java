/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.CollisionEvent;
import java.util.EventListener;

public interface CollisionListener
extends EventListener {
    public void collide(CollisionEvent var1);
}

