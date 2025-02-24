/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.callbacks.TreeCallback;
import org.jbox2d.collision.broadphase.BroadPhase;
import org.jbox2d.dynamics.FixtureProxy;

class WorldQueryWrapper
implements TreeCallback {
    BroadPhase broadPhase;
    QueryCallback callback;

    WorldQueryWrapper() {
    }

    @Override
    public boolean treeCallback(int nodeId) {
        FixtureProxy proxy = (FixtureProxy)this.broadPhase.getUserData(nodeId);
        return this.callback.reportFixture(proxy.fixture);
    }
}

