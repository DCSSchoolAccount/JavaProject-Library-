/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.callbacks.TreeRayCastCallback;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.broadphase.BroadPhase;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureProxy;

class WorldRayCastWrapper
implements TreeRayCastCallback {
    private final RayCastOutput output = new RayCastOutput();
    private final Vec2 temp = new Vec2();
    private final Vec2 point = new Vec2();
    BroadPhase broadPhase;
    RayCastCallback callback;

    WorldRayCastWrapper() {
    }

    @Override
    public float raycastCallback(RayCastInput input, int nodeId) {
        Object userData = this.broadPhase.getUserData(nodeId);
        FixtureProxy proxy = (FixtureProxy)userData;
        Fixture fixture = proxy.fixture;
        int index = proxy.childIndex;
        boolean hit = fixture.raycast(this.output, input, index);
        if (hit) {
            float fraction = this.output.fraction;
            this.temp.set(input.p2).mulLocal(fraction);
            this.point.set(input.p1).mulLocal(1.0f - fraction).addLocal(this.temp);
            return this.callback.reportFixture(fixture, this.point, this.output.normal, fraction);
        }
        return input.maxFraction;
    }
}

