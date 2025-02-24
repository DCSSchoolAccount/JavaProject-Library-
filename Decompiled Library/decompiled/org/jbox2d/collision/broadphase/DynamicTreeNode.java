/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.collision.broadphase;

import org.jbox2d.collision.AABB;

public class DynamicTreeNode {
    public final AABB aabb = new AABB();
    public Object userData;
    protected DynamicTreeNode parent;
    protected DynamicTreeNode child1;
    protected DynamicTreeNode child2;
    protected final int id;
    protected boolean leaf;
    protected int height;

    public final boolean isLeaf() {
        return this.child1 == null;
    }

    public Object getUserData() {
        return this.userData;
    }

    public void setUserData(Object argData) {
        this.userData = argData;
    }

    protected DynamicTreeNode(int id) {
        this.id = id;
    }
}

