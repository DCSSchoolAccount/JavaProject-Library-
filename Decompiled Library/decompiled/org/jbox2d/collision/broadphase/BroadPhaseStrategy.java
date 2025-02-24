/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.collision.broadphase;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.callbacks.TreeCallback;
import org.jbox2d.callbacks.TreeRayCastCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.common.Vec2;

public interface BroadPhaseStrategy {
    public int createProxy(AABB var1, Object var2);

    public void destroyProxy(int var1);

    public boolean moveProxy(int var1, AABB var2, Vec2 var3);

    public Object getUserData(int var1);

    public AABB getFatAABB(int var1);

    public void query(TreeCallback var1, AABB var2);

    public void raycast(TreeRayCastCallback var1, RayCastInput var2);

    public int computeHeight();

    public int getHeight();

    public int getMaxBalance();

    public float getAreaRatio();

    public int getInsertionCount();

    public void drawTree(DebugDraw var1);
}

