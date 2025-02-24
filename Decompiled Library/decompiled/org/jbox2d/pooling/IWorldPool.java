/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.pooling;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.Distance;
import org.jbox2d.collision.TimeOfImpact;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.Mat33;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.pooling.IDynamicStack;

public interface IWorldPool {
    public IDynamicStack<Contact> getPolyContactStack();

    public IDynamicStack<Contact> getCircleContactStack();

    public IDynamicStack<Contact> getPolyCircleContactStack();

    public IDynamicStack<Contact> getEdgeCircleContactStack();

    public IDynamicStack<Contact> getEdgePolyContactStack();

    public IDynamicStack<Contact> getChainCircleContactStack();

    public IDynamicStack<Contact> getChainPolyContactStack();

    public Vec2 popVec2();

    public Vec2[] popVec2(int var1);

    public void pushVec2(int var1);

    public Vec3 popVec3();

    public Vec3[] popVec3(int var1);

    public void pushVec3(int var1);

    public Mat22 popMat22();

    public Mat22[] popMat22(int var1);

    public void pushMat22(int var1);

    public Mat33 popMat33();

    public void pushMat33(int var1);

    public AABB popAABB();

    public AABB[] popAABB(int var1);

    public void pushAABB(int var1);

    public Rot popRot();

    public void pushRot(int var1);

    public Collision getCollision();

    public TimeOfImpact getTimeOfImpact();

    public Distance getDistance();

    public float[] getFloatArray(int var1);

    public int[] getIntArray(int var1);

    public Vec2[] getVec2Array(int var1);
}

