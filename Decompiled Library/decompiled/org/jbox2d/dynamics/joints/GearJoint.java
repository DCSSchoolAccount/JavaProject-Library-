/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.joints;

import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.SolverData;
import org.jbox2d.dynamics.joints.GearJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.PrismaticJoint;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.pooling.IWorldPool;

public class GearJoint
extends Joint {
    private final Joint m_joint1;
    private final Joint m_joint2;
    private final JointType m_typeA;
    private final JointType m_typeB;
    private final Body m_bodyC;
    private final Body m_bodyD;
    private final Vec2 m_localAnchorA = new Vec2();
    private final Vec2 m_localAnchorB = new Vec2();
    private final Vec2 m_localAnchorC = new Vec2();
    private final Vec2 m_localAnchorD = new Vec2();
    private final Vec2 m_localAxisC = new Vec2();
    private final Vec2 m_localAxisD = new Vec2();
    private float m_referenceAngleA;
    private float m_referenceAngleB;
    private float m_constant;
    private float m_ratio;
    private float m_impulse;
    private int m_indexA;
    private int m_indexB;
    private int m_indexC;
    private int m_indexD;
    private final Vec2 m_lcA = new Vec2();
    private final Vec2 m_lcB = new Vec2();
    private final Vec2 m_lcC = new Vec2();
    private final Vec2 m_lcD = new Vec2();
    private float m_mA;
    private float m_mB;
    private float m_mC;
    private float m_mD;
    private float m_iA;
    private float m_iB;
    private float m_iC;
    private float m_iD;
    private final Vec2 m_JvAC = new Vec2();
    private final Vec2 m_JvBD = new Vec2();
    private float m_JwA;
    private float m_JwB;
    private float m_JwC;
    private float m_JwD;
    private float m_mass;

    protected GearJoint(IWorldPool argWorldPool, GearJointDef def) {
        super(argWorldPool, def);
        float coordinateB;
        float coordinateA;
        this.m_joint1 = def.joint1;
        this.m_joint2 = def.joint2;
        this.m_typeA = this.m_joint1.getType();
        this.m_typeB = this.m_joint2.getType();
        assert (this.m_typeA == JointType.REVOLUTE || this.m_typeA == JointType.PRISMATIC);
        assert (this.m_typeB == JointType.REVOLUTE || this.m_typeB == JointType.PRISMATIC);
        this.m_bodyC = this.m_joint1.getBodyA();
        this.m_bodyA = this.m_joint1.getBodyB();
        Transform xfA = this.m_bodyA.m_xf;
        float aA = this.m_bodyA.m_sweep.a;
        Transform xfC = this.m_bodyC.m_xf;
        float aC = this.m_bodyC.m_sweep.a;
        if (this.m_typeA == JointType.REVOLUTE) {
            RevoluteJoint revolute = (RevoluteJoint)def.joint1;
            this.m_localAnchorC.set(revolute.m_localAnchorA);
            this.m_localAnchorA.set(revolute.m_localAnchorB);
            this.m_referenceAngleA = revolute.m_referenceAngle;
            this.m_localAxisC.setZero();
            coordinateA = aA - aC - this.m_referenceAngleA;
        } else {
            Vec2 pA = this.pool.popVec2();
            Vec2 temp = this.pool.popVec2();
            PrismaticJoint prismatic = (PrismaticJoint)def.joint1;
            this.m_localAnchorC.set(prismatic.m_localAnchorA);
            this.m_localAnchorA.set(prismatic.m_localAnchorB);
            this.m_referenceAngleA = prismatic.m_referenceAngle;
            this.m_localAxisC.set(prismatic.m_localXAxisA);
            Vec2 pC = this.m_localAnchorC;
            Rot.mulToOutUnsafe(xfA.q, this.m_localAnchorA, temp);
            temp.addLocal(xfA.p).subLocal(xfC.p);
            Rot.mulTransUnsafe(xfC.q, temp, pA);
            coordinateA = Vec2.dot(pA.subLocal(pC), this.m_localAxisC);
            this.pool.pushVec2(2);
        }
        this.m_bodyD = this.m_joint2.getBodyA();
        this.m_bodyB = this.m_joint2.getBodyB();
        Transform xfB = this.m_bodyB.m_xf;
        float aB = this.m_bodyB.m_sweep.a;
        Transform xfD = this.m_bodyD.m_xf;
        float aD = this.m_bodyD.m_sweep.a;
        if (this.m_typeB == JointType.REVOLUTE) {
            RevoluteJoint revolute = (RevoluteJoint)def.joint2;
            this.m_localAnchorD.set(revolute.m_localAnchorA);
            this.m_localAnchorB.set(revolute.m_localAnchorB);
            this.m_referenceAngleB = revolute.m_referenceAngle;
            this.m_localAxisD.setZero();
            coordinateB = aB - aD - this.m_referenceAngleB;
        } else {
            Vec2 pB = this.pool.popVec2();
            Vec2 temp = this.pool.popVec2();
            PrismaticJoint prismatic = (PrismaticJoint)def.joint2;
            this.m_localAnchorD.set(prismatic.m_localAnchorA);
            this.m_localAnchorB.set(prismatic.m_localAnchorB);
            this.m_referenceAngleB = prismatic.m_referenceAngle;
            this.m_localAxisD.set(prismatic.m_localXAxisA);
            Vec2 pD = this.m_localAnchorD;
            Rot.mulToOutUnsafe(xfB.q, this.m_localAnchorB, temp);
            temp.addLocal(xfB.p).subLocal(xfD.p);
            Rot.mulTransUnsafe(xfD.q, temp, pB);
            coordinateB = Vec2.dot(pB.subLocal(pD), this.m_localAxisD);
            this.pool.pushVec2(2);
        }
        this.m_ratio = def.ratio;
        this.m_constant = coordinateA + this.m_ratio * coordinateB;
        this.m_impulse = 0.0f;
    }

    @Override
    public void getAnchorA(Vec2 argOut) {
        this.m_bodyA.getWorldPointToOut(this.m_localAnchorA, argOut);
    }

    @Override
    public void getAnchorB(Vec2 argOut) {
        this.m_bodyB.getWorldPointToOut(this.m_localAnchorB, argOut);
    }

    @Override
    public void getReactionForce(float inv_dt, Vec2 argOut) {
        argOut.set(this.m_JvAC).mulLocal(this.m_impulse);
        argOut.mulLocal(inv_dt);
    }

    @Override
    public float getReactionTorque(float inv_dt) {
        float L = this.m_impulse * this.m_JwA;
        return inv_dt * L;
    }

    public void setRatio(float argRatio) {
        this.m_ratio = argRatio;
    }

    public float getRatio() {
        return this.m_ratio;
    }

    @Override
    public void initVelocityConstraints(SolverData data) {
        this.m_indexA = this.m_bodyA.m_islandIndex;
        this.m_indexB = this.m_bodyB.m_islandIndex;
        this.m_indexC = this.m_bodyC.m_islandIndex;
        this.m_indexD = this.m_bodyD.m_islandIndex;
        this.m_lcA.set(this.m_bodyA.m_sweep.localCenter);
        this.m_lcB.set(this.m_bodyB.m_sweep.localCenter);
        this.m_lcC.set(this.m_bodyC.m_sweep.localCenter);
        this.m_lcD.set(this.m_bodyD.m_sweep.localCenter);
        this.m_mA = this.m_bodyA.m_invMass;
        this.m_mB = this.m_bodyB.m_invMass;
        this.m_mC = this.m_bodyC.m_invMass;
        this.m_mD = this.m_bodyD.m_invMass;
        this.m_iA = this.m_bodyA.m_invI;
        this.m_iB = this.m_bodyB.m_invI;
        this.m_iC = this.m_bodyC.m_invI;
        this.m_iD = this.m_bodyD.m_invI;
        float aA = data.positions[this.m_indexA].a;
        Vec2 vA = data.velocities[this.m_indexA].v;
        float wA = data.velocities[this.m_indexA].w;
        float aB = data.positions[this.m_indexB].a;
        Vec2 vB = data.velocities[this.m_indexB].v;
        float wB = data.velocities[this.m_indexB].w;
        float aC = data.positions[this.m_indexC].a;
        Vec2 vC = data.velocities[this.m_indexC].v;
        float wC = data.velocities[this.m_indexC].w;
        float aD = data.positions[this.m_indexD].a;
        Vec2 vD = data.velocities[this.m_indexD].v;
        float wD = data.velocities[this.m_indexD].w;
        Rot qA = this.pool.popRot();
        Rot qB = this.pool.popRot();
        Rot qC = this.pool.popRot();
        Rot qD = this.pool.popRot();
        qA.set(aA);
        qB.set(aB);
        qC.set(aC);
        qD.set(aD);
        this.m_mass = 0.0f;
        Vec2 temp = this.pool.popVec2();
        if (this.m_typeA == JointType.REVOLUTE) {
            this.m_JvAC.setZero();
            this.m_JwA = 1.0f;
            this.m_JwC = 1.0f;
            this.m_mass += this.m_iA + this.m_iC;
        } else {
            Vec2 rC = this.pool.popVec2();
            Vec2 rA = this.pool.popVec2();
            Rot.mulToOutUnsafe(qC, this.m_localAxisC, this.m_JvAC);
            Rot.mulToOutUnsafe(qC, temp.set(this.m_localAnchorC).subLocal(this.m_lcC), rC);
            Rot.mulToOutUnsafe(qA, temp.set(this.m_localAnchorA).subLocal(this.m_lcA), rA);
            this.m_JwC = Vec2.cross(rC, this.m_JvAC);
            this.m_JwA = Vec2.cross(rA, this.m_JvAC);
            this.m_mass += this.m_mC + this.m_mA + this.m_iC * this.m_JwC * this.m_JwC + this.m_iA * this.m_JwA * this.m_JwA;
            this.pool.pushVec2(2);
        }
        if (this.m_typeB == JointType.REVOLUTE) {
            this.m_JvBD.setZero();
            this.m_JwB = this.m_ratio;
            this.m_JwD = this.m_ratio;
            this.m_mass += this.m_ratio * this.m_ratio * (this.m_iB + this.m_iD);
        } else {
            Vec2 u = this.pool.popVec2();
            Vec2 rD = this.pool.popVec2();
            Vec2 rB = this.pool.popVec2();
            Rot.mulToOutUnsafe(qD, this.m_localAxisD, u);
            Rot.mulToOutUnsafe(qD, temp.set(this.m_localAnchorD).subLocal(this.m_lcD), rD);
            Rot.mulToOutUnsafe(qB, temp.set(this.m_localAnchorB).subLocal(this.m_lcB), rB);
            this.m_JvBD.set(u).mulLocal(this.m_ratio);
            this.m_JwD = this.m_ratio * Vec2.cross(rD, u);
            this.m_JwB = this.m_ratio * Vec2.cross(rB, u);
            this.m_mass += this.m_ratio * this.m_ratio * (this.m_mD + this.m_mB) + this.m_iD * this.m_JwD * this.m_JwD + this.m_iB * this.m_JwB * this.m_JwB;
            this.pool.pushVec2(3);
        }
        float f = this.m_mass = this.m_mass > 0.0f ? 1.0f / this.m_mass : 0.0f;
        if (data.step.warmStarting) {
            vA.x += this.m_mA * this.m_impulse * this.m_JvAC.x;
            vA.y += this.m_mA * this.m_impulse * this.m_JvAC.y;
            wA += this.m_iA * this.m_impulse * this.m_JwA;
            vB.x += this.m_mB * this.m_impulse * this.m_JvBD.x;
            vB.y += this.m_mB * this.m_impulse * this.m_JvBD.y;
            wB += this.m_iB * this.m_impulse * this.m_JwB;
            vC.x -= this.m_mC * this.m_impulse * this.m_JvAC.x;
            vC.y -= this.m_mC * this.m_impulse * this.m_JvAC.y;
            wC -= this.m_iC * this.m_impulse * this.m_JwC;
            vD.x -= this.m_mD * this.m_impulse * this.m_JvBD.x;
            vD.y -= this.m_mD * this.m_impulse * this.m_JvBD.y;
            wD -= this.m_iD * this.m_impulse * this.m_JwD;
        } else {
            this.m_impulse = 0.0f;
        }
        this.pool.pushVec2(1);
        this.pool.pushRot(4);
        data.velocities[this.m_indexA].w = wA;
        data.velocities[this.m_indexB].w = wB;
        data.velocities[this.m_indexC].w = wC;
        data.velocities[this.m_indexD].w = wD;
    }

    @Override
    public void solveVelocityConstraints(SolverData data) {
        Vec2 vA = data.velocities[this.m_indexA].v;
        float wA = data.velocities[this.m_indexA].w;
        Vec2 vB = data.velocities[this.m_indexB].v;
        float wB = data.velocities[this.m_indexB].w;
        Vec2 vC = data.velocities[this.m_indexC].v;
        float wC = data.velocities[this.m_indexC].w;
        Vec2 vD = data.velocities[this.m_indexD].v;
        float wD = data.velocities[this.m_indexD].w;
        Vec2 temp1 = this.pool.popVec2();
        Vec2 temp2 = this.pool.popVec2();
        float Cdot = Vec2.dot(this.m_JvAC, temp1.set(vA).subLocal(vC)) + Vec2.dot(this.m_JvBD, temp2.set(vB).subLocal(vD));
        this.pool.pushVec2(2);
        float impulse = -this.m_mass * (Cdot += this.m_JwA * wA - this.m_JwC * wC + (this.m_JwB * wB - this.m_JwD * wD));
        this.m_impulse += impulse;
        vA.x += this.m_mA * impulse * this.m_JvAC.x;
        vA.y += this.m_mA * impulse * this.m_JvAC.y;
        wA += this.m_iA * impulse * this.m_JwA;
        vB.x += this.m_mB * impulse * this.m_JvBD.x;
        vB.y += this.m_mB * impulse * this.m_JvBD.y;
        wB += this.m_iB * impulse * this.m_JwB;
        vC.x -= this.m_mC * impulse * this.m_JvAC.x;
        vC.y -= this.m_mC * impulse * this.m_JvAC.y;
        vD.x -= this.m_mD * impulse * this.m_JvBD.x;
        vD.y -= this.m_mD * impulse * this.m_JvBD.y;
        data.velocities[this.m_indexA].w = wA;
        data.velocities[this.m_indexB].w = wB;
        data.velocities[this.m_indexC].w = wC -= this.m_iC * impulse * this.m_JwC;
        data.velocities[this.m_indexD].w = wD -= this.m_iD * impulse * this.m_JwD;
    }

    public Joint getJoint1() {
        return this.m_joint1;
    }

    public Joint getJoint2() {
        return this.m_joint2;
    }

    @Override
    public boolean solvePositionConstraints(SolverData data) {
        float coordinateB;
        float JwD;
        float JwB;
        float coordinateA;
        float JwC;
        float JwA;
        Vec2 cA = data.positions[this.m_indexA].c;
        float aA = data.positions[this.m_indexA].a;
        Vec2 cB = data.positions[this.m_indexB].c;
        float aB = data.positions[this.m_indexB].a;
        Vec2 cC = data.positions[this.m_indexC].c;
        float aC = data.positions[this.m_indexC].a;
        Vec2 cD = data.positions[this.m_indexD].c;
        float aD = data.positions[this.m_indexD].a;
        Rot qA = this.pool.popRot();
        Rot qB = this.pool.popRot();
        Rot qC = this.pool.popRot();
        Rot qD = this.pool.popRot();
        qA.set(aA);
        qB.set(aB);
        qC.set(aC);
        qD.set(aD);
        float linearError = 0.0f;
        Vec2 temp = this.pool.popVec2();
        Vec2 JvAC = this.pool.popVec2();
        Vec2 JvBD = this.pool.popVec2();
        float mass = 0.0f;
        if (this.m_typeA == JointType.REVOLUTE) {
            JvAC.setZero();
            JwA = 1.0f;
            JwC = 1.0f;
            mass += this.m_iA + this.m_iC;
            coordinateA = aA - aC - this.m_referenceAngleA;
        } else {
            Vec2 rC = this.pool.popVec2();
            Vec2 rA = this.pool.popVec2();
            Vec2 pC = this.pool.popVec2();
            Vec2 pA = this.pool.popVec2();
            Rot.mulToOutUnsafe(qC, this.m_localAxisC, JvAC);
            Rot.mulToOutUnsafe(qC, temp.set(this.m_localAnchorC).subLocal(this.m_lcC), rC);
            Rot.mulToOutUnsafe(qA, temp.set(this.m_localAnchorA).subLocal(this.m_lcA), rA);
            JwC = Vec2.cross(rC, JvAC);
            JwA = Vec2.cross(rA, JvAC);
            mass += this.m_mC + this.m_mA + this.m_iC * JwC * JwC + this.m_iA * JwA * JwA;
            pC.set(this.m_localAnchorC).subLocal(this.m_lcC);
            Rot.mulTransUnsafe(qC, temp.set(rA).addLocal(cA).subLocal(cC), pA);
            coordinateA = Vec2.dot(pA.subLocal(pC), this.m_localAxisC);
            this.pool.pushVec2(4);
        }
        if (this.m_typeB == JointType.REVOLUTE) {
            JvBD.setZero();
            JwB = this.m_ratio;
            JwD = this.m_ratio;
            mass += this.m_ratio * this.m_ratio * (this.m_iB + this.m_iD);
            coordinateB = aB - aD - this.m_referenceAngleB;
        } else {
            Vec2 u = this.pool.popVec2();
            Vec2 rD = this.pool.popVec2();
            Vec2 rB = this.pool.popVec2();
            Vec2 pD = this.pool.popVec2();
            Vec2 pB = this.pool.popVec2();
            Rot.mulToOutUnsafe(qD, this.m_localAxisD, u);
            Rot.mulToOutUnsafe(qD, temp.set(this.m_localAnchorD).subLocal(this.m_lcD), rD);
            Rot.mulToOutUnsafe(qB, temp.set(this.m_localAnchorB).subLocal(this.m_lcB), rB);
            JvBD.set(u).mulLocal(this.m_ratio);
            JwD = Vec2.cross(rD, u);
            JwB = Vec2.cross(rB, u);
            mass += this.m_ratio * this.m_ratio * (this.m_mD + this.m_mB) + this.m_iD * JwD * JwD + this.m_iB * JwB * JwB;
            pD.set(this.m_localAnchorD).subLocal(this.m_lcD);
            Rot.mulTransUnsafe(qD, temp.set(rB).addLocal(cB).subLocal(cD), pB);
            coordinateB = Vec2.dot(pB.subLocal(pD), this.m_localAxisD);
            this.pool.pushVec2(5);
        }
        float C = coordinateA + this.m_ratio * coordinateB - this.m_constant;
        float impulse = 0.0f;
        if (mass > 0.0f) {
            impulse = -C / mass;
        }
        this.pool.pushVec2(3);
        this.pool.pushRot(4);
        cA.x += this.m_mA * impulse * JvAC.x;
        cA.y += this.m_mA * impulse * JvAC.y;
        aA += this.m_iA * impulse * JwA;
        cB.x += this.m_mB * impulse * JvBD.x;
        cB.y += this.m_mB * impulse * JvBD.y;
        aB += this.m_iB * impulse * JwB;
        cC.x -= this.m_mC * impulse * JvAC.x;
        cC.y -= this.m_mC * impulse * JvAC.y;
        cD.x -= this.m_mD * impulse * JvBD.x;
        cD.y -= this.m_mD * impulse * JvBD.y;
        data.positions[this.m_indexA].a = aA;
        data.positions[this.m_indexB].a = aB;
        data.positions[this.m_indexC].a = aC -= this.m_iC * impulse * JwC;
        data.positions[this.m_indexD].a = aD -= this.m_iD * impulse * JwD;
        return linearError < 0.005f;
    }
}

