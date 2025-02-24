/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.DestructionEvent;
import java.util.EventListener;

public interface DestructionListener
extends EventListener {
    public void destroy(DestructionEvent var1);
}

