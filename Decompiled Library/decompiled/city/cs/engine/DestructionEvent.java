/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import java.util.EventObject;

public class DestructionEvent
extends EventObject {
    public DestructionEvent(Body source) {
        super(source);
    }

    @Override
    public Body getSource() {
        return (Body)this.source;
    }
}

