/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics;

import org.jbox2d.collision.AABB;
import org.jbox2d.dynamics.Fixture;

public class FixtureProxy {
    final AABB aabb = new AABB();
    Fixture fixture;
    int childIndex;
    int proxyId;
}

