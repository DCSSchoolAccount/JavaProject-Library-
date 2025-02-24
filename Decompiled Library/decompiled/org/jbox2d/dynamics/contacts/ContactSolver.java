/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.contacts;

import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.ManifoldPoint;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.TimeStep;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactPositionConstraint;
import org.jbox2d.dynamics.contacts.ContactVelocityConstraint;
import org.jbox2d.dynamics.contacts.Position;
import org.jbox2d.dynamics.contacts.PositionSolverManifold;
import org.jbox2d.dynamics.contacts.Velocity;

public class ContactSolver {
    public static final boolean DEBUG_SOLVER = false;
    public static final float k_errorTol = 0.001f;
    public static final int INITIAL_NUM_CONSTRAINTS = 256;
    public static final float k_maxConditionNumber = 100.0f;
    public TimeStep m_step;
    public Position[] m_positions;
    public Velocity[] m_velocities;
    public ContactPositionConstraint[] m_positionConstraints;
    public ContactVelocityConstraint[] m_velocityConstraints;
    public Contact[] m_contacts;
    public int m_count;
    private final Vec2 tangent = new Vec2();
    private final Vec2 temp1 = new Vec2();
    private final Vec2 temp2 = new Vec2();
    private final Vec2 P = new Vec2();
    private final Vec2 temp = new Vec2();
    private final Transform xfA = new Transform();
    private final Transform xfB = new Transform();
    private final WorldManifold worldManifold = new WorldManifold();
    private final Vec2 a = new Vec2();
    private final Vec2 b = new Vec2();
    private final Vec2 dv1 = new Vec2();
    private final Vec2 dv2 = new Vec2();
    private final Vec2 x = new Vec2();
    private final Vec2 d = new Vec2();
    private final Vec2 P1 = new Vec2();
    private final Vec2 P2 = new Vec2();
    private final PositionSolverManifold psolver = new PositionSolverManifold();
    private final Vec2 rA = new Vec2();
    private final Vec2 rB = new Vec2();

    public ContactSolver() {
        this.m_positionConstraints = new ContactPositionConstraint[256];
        this.m_velocityConstraints = new ContactVelocityConstraint[256];
        for (int i = 0; i < 256; ++i) {
            this.m_positionConstraints[i] = new ContactPositionConstraint();
            this.m_velocityConstraints[i] = new ContactVelocityConstraint();
        }
    }

    public final void init(ContactSolverDef def) {
        int i;
        Object[] old;
        this.m_step = def.step;
        this.m_count = def.count;
        if (this.m_positionConstraints.length < this.m_count) {
            old = this.m_positionConstraints;
            this.m_positionConstraints = new ContactPositionConstraint[MathUtils.max(old.length * 2, this.m_count)];
            System.arraycopy(old, 0, this.m_positionConstraints, 0, old.length);
            for (i = old.length; i < this.m_positionConstraints.length; ++i) {
                this.m_positionConstraints[i] = new ContactPositionConstraint();
            }
        }
        if (this.m_velocityConstraints.length < this.m_count) {
            old = this.m_velocityConstraints;
            this.m_velocityConstraints = new ContactVelocityConstraint[MathUtils.max(old.length * 2, this.m_count)];
            System.arraycopy(old, 0, this.m_velocityConstraints, 0, old.length);
            for (i = old.length; i < this.m_velocityConstraints.length; ++i) {
                this.m_velocityConstraints[i] = new ContactVelocityConstraint();
            }
        }
        this.m_positions = def.positions;
        this.m_velocities = def.velocities;
        this.m_contacts = def.contacts;
        for (int i2 = 0; i2 < this.m_count; ++i2) {
            Contact contact = this.m_contacts[i2];
            Fixture fixtureA = contact.m_fixtureA;
            Fixture fixtureB = contact.m_fixtureB;
            Shape shapeA = fixtureA.getShape();
            Shape shapeB = fixtureB.getShape();
            float radiusA = shapeA.m_radius;
            float radiusB = shapeB.m_radius;
            Body bodyA = fixtureA.getBody();
            Body bodyB = fixtureB.getBody();
            Manifold manifold = contact.getManifold();
            int pointCount = manifold.pointCount;
            assert (pointCount > 0);
            ContactVelocityConstraint vc = this.m_velocityConstraints[i2];
            vc.friction = contact.m_friction;
            vc.restitution = contact.m_restitution;
            vc.tangentSpeed = contact.m_tangentSpeed;
            vc.indexA = bodyA.m_islandIndex;
            vc.indexB = bodyB.m_islandIndex;
            vc.invMassA = bodyA.m_invMass;
            vc.invMassB = bodyB.m_invMass;
            vc.invIA = bodyA.m_invI;
            vc.invIB = bodyB.m_invI;
            vc.contactIndex = i2;
            vc.pointCount = pointCount;
            vc.K.setZero();
            vc.normalMass.setZero();
            ContactPositionConstraint pc = this.m_positionConstraints[i2];
            pc.indexA = bodyA.m_islandIndex;
            pc.indexB = bodyB.m_islandIndex;
            pc.invMassA = bodyA.m_invMass;
            pc.invMassB = bodyB.m_invMass;
            pc.localCenterA.set(bodyA.m_sweep.localCenter);
            pc.localCenterB.set(bodyB.m_sweep.localCenter);
            pc.invIA = bodyA.m_invI;
            pc.invIB = bodyB.m_invI;
            pc.localNormal.set(manifold.localNormal);
            pc.localPoint.set(manifold.localPoint);
            pc.pointCount = pointCount;
            pc.radiusA = radiusA;
            pc.radiusB = radiusB;
            pc.type = manifold.type;
            for (int j = 0; j < pointCount; ++j) {
                ManifoldPoint cp = manifold.points[j];
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                if (this.m_step.warmStarting) {
                    vcp.normalImpulse = this.m_step.dtRatio * cp.normalImpulse;
                    vcp.tangentImpulse = this.m_step.dtRatio * cp.tangentImpulse;
                } else {
                    vcp.normalImpulse = 0.0f;
                    vcp.tangentImpulse = 0.0f;
                }
                vcp.rA.setZero();
                vcp.rB.setZero();
                vcp.normalMass = 0.0f;
                vcp.tangentMass = 0.0f;
                vcp.velocityBias = 0.0f;
                pc.localPoints[j].x = cp.localPoint.x;
                pc.localPoints[j].y = cp.localPoint.y;
            }
        }
    }

    public void warmStart() {
        for (int i = 0; i < this.m_count; ++i) {
            ContactVelocityConstraint vc = this.m_velocityConstraints[i];
            int indexA = vc.indexA;
            int indexB = vc.indexB;
            float mA = vc.invMassA;
            float iA = vc.invIA;
            float mB = vc.invMassB;
            float iB = vc.invIB;
            int pointCount = vc.pointCount;
            Vec2 vA = this.m_velocities[indexA].v;
            float wA = this.m_velocities[indexA].w;
            Vec2 vB = this.m_velocities[indexB].v;
            float wB = this.m_velocities[indexB].w;
            Vec2 normal = vc.normal;
            float tangentx = 1.0f * normal.y;
            float tangenty = -1.0f * normal.x;
            for (int j = 0; j < pointCount; ++j) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                float Px = tangentx * vcp.tangentImpulse + normal.x * vcp.normalImpulse;
                float Py = tangenty * vcp.tangentImpulse + normal.y * vcp.normalImpulse;
                wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);
                vA.x -= Px * mA;
                vA.y -= Py * mA;
                wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
                vB.x += Px * mB;
                vB.y += Py * mB;
            }
            this.m_velocities[indexA].w = wA;
            this.m_velocities[indexB].w = wB;
        }
    }

    public final void initializeVelocityConstraints() {
        for (int i = 0; i < this.m_count; ++i) {
            float k12;
            float rn2B;
            float rn2A;
            float k22;
            float rn1B;
            ContactVelocityConstraint vc = this.m_velocityConstraints[i];
            ContactPositionConstraint pc = this.m_positionConstraints[i];
            float radiusA = pc.radiusA;
            float radiusB = pc.radiusB;
            Manifold manifold = this.m_contacts[vc.contactIndex].getManifold();
            int indexA = vc.indexA;
            int indexB = vc.indexB;
            float mA = vc.invMassA;
            float mB = vc.invMassB;
            float iA = vc.invIA;
            float iB = vc.invIB;
            Vec2 localCenterA = pc.localCenterA;
            Vec2 localCenterB = pc.localCenterB;
            Vec2 cA = this.m_positions[indexA].c;
            float aA = this.m_positions[indexA].a;
            Vec2 vA = this.m_velocities[indexA].v;
            float wA = this.m_velocities[indexA].w;
            Vec2 cB = this.m_positions[indexB].c;
            float aB = this.m_positions[indexB].a;
            Vec2 vB = this.m_velocities[indexB].v;
            float wB = this.m_velocities[indexB].w;
            assert (manifold.pointCount > 0);
            this.xfA.q.set(aA);
            this.xfB.q.set(aB);
            this.xfA.p.x = cA.x - (this.xfA.q.c * localCenterA.x - this.xfA.q.s * localCenterA.y);
            this.xfA.p.y = cA.y - (this.xfA.q.s * localCenterA.x + this.xfA.q.c * localCenterA.y);
            this.xfB.p.x = cB.x - (this.xfB.q.c * localCenterB.x - this.xfB.q.s * localCenterB.y);
            this.xfB.p.y = cB.y - (this.xfB.q.s * localCenterB.x + this.xfB.q.c * localCenterB.y);
            this.worldManifold.initialize(manifold, this.xfA, radiusA, this.xfB, radiusB);
            vc.normal.set(this.worldManifold.normal);
            int pointCount = vc.pointCount;
            for (int j = 0; j < pointCount; ++j) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                vcp.rA.set(this.worldManifold.points[j]).subLocal(cA);
                vcp.rB.set(this.worldManifold.points[j]).subLocal(cB);
                float rnA = vcp.rA.x * vc.normal.y - vcp.rA.y * vc.normal.x;
                float rnB = vcp.rB.x * vc.normal.y - vcp.rB.y * vc.normal.x;
                float kNormal = mA + mB + iA * rnA * rnA + iB * rnB * rnB;
                vcp.normalMass = kNormal > 0.0f ? 1.0f / kNormal : 0.0f;
                float tangentx = 1.0f * vc.normal.y;
                float tangenty = -1.0f * vc.normal.x;
                float rtA = vcp.rA.x * tangenty - vcp.rA.y * tangentx;
                float rtB = vcp.rB.x * tangenty - vcp.rB.y * tangentx;
                float kTangent = mA + mB + iA * rtA * rtA + iB * rtB * rtB;
                vcp.tangentMass = kTangent > 0.0f ? 1.0f / kTangent : 0.0f;
                vcp.velocityBias = 0.0f;
                float tempx = vB.x + -wB * vcp.rB.y - vA.x - -wA * vcp.rA.y;
                float tempy = vB.y + wB * vcp.rB.x - vA.y - wA * vcp.rA.x;
                float vRel = vc.normal.x * tempx + vc.normal.y * tempy;
                if (!(vRel < -1.0f)) continue;
                vcp.velocityBias = -vc.restitution * vRel;
            }
            if (vc.pointCount != 2) continue;
            ContactVelocityConstraint.VelocityConstraintPoint vcp1 = vc.points[0];
            ContactVelocityConstraint.VelocityConstraintPoint vcp2 = vc.points[1];
            float rn1A = Vec2.cross(vcp1.rA, vc.normal);
            float k11 = mA + mB + iA * rn1A * rn1A + iB * (rn1B = Vec2.cross(vcp1.rB, vc.normal)) * rn1B;
            if (k11 * k11 < 100.0f * (k11 * (k22 = mA + mB + iA * (rn2A = Vec2.cross(vcp2.rA, vc.normal)) * rn2A + iB * (rn2B = Vec2.cross(vcp2.rB, vc.normal)) * rn2B) - (k12 = mA + mB + iA * rn1A * rn2A + iB * rn1B * rn2B) * k12)) {
                vc.K.ex.set(k11, k12);
                vc.K.ey.set(k12, k22);
                vc.K.invertToOut(vc.normalMass);
                continue;
            }
            vc.pointCount = 1;
        }
    }

    public final void solveVelocityConstraints() {
        for (int i = 0; i < this.m_count; ++i) {
            ContactVelocityConstraint vc = this.m_velocityConstraints[i];
            int indexA = vc.indexA;
            int indexB = vc.indexB;
            float mA = vc.invMassA;
            float mB = vc.invMassB;
            float iA = vc.invIA;
            float iB = vc.invIB;
            int pointCount = vc.pointCount;
            Vec2 vA = this.m_velocities[indexA].v;
            float wA = this.m_velocities[indexA].w;
            Vec2 vB = this.m_velocities[indexB].v;
            float wB = this.m_velocities[indexB].w;
            Vec2 normal = vc.normal;
            this.tangent.x = 1.0f * vc.normal.y;
            this.tangent.y = -1.0f * vc.normal.x;
            float friction = vc.friction;
            assert (pointCount == 1 || pointCount == 2);
            for (int j = 0; j < pointCount; ++j) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                Vec2 a = vcp.rA;
                float dvx = -wB * vcp.rB.y + vB.x - vA.x + wA * a.y;
                float dvy = wB * vcp.rB.x + vB.y - vA.y - wA * a.x;
                float vt = dvx * this.tangent.x + dvy * this.tangent.y - vc.tangentSpeed;
                float lambda = vcp.tangentMass * -vt;
                float maxFriction = friction * vcp.normalImpulse;
                float newImpulse = MathUtils.clamp(vcp.tangentImpulse + lambda, -maxFriction, maxFriction);
                lambda = newImpulse - vcp.tangentImpulse;
                vcp.tangentImpulse = newImpulse;
                float Px = this.tangent.x * lambda;
                float Py = this.tangent.y * lambda;
                vA.x -= Px * mA;
                vA.y -= Py * mA;
                wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);
                vB.x += Px * mB;
                vB.y += Py * mB;
                wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
            }
            if (vc.pointCount == 1) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[0];
                float dvx = -wB * vcp.rB.y + vB.x - vA.x + wA * vcp.rA.y;
                float dvy = wB * vcp.rB.x + vB.y - vA.y - wA * vcp.rA.x;
                float vn = dvx * normal.x + dvy * normal.y;
                float lambda = -vcp.normalMass * (vn - vcp.velocityBias);
                float a = vcp.normalImpulse + lambda;
                float newImpulse = a > 0.0f ? a : 0.0f;
                lambda = newImpulse - vcp.normalImpulse;
                vcp.normalImpulse = newImpulse;
                float Px = normal.x * lambda;
                float Py = normal.y * lambda;
                vA.x -= Px * mA;
                vA.y -= Py * mA;
                wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);
                vB.x += Px * mB;
                vB.y += Py * mB;
                wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
            } else {
                ContactVelocityConstraint.VelocityConstraintPoint cp1 = vc.points[0];
                ContactVelocityConstraint.VelocityConstraintPoint cp2 = vc.points[1];
                this.a.x = cp1.normalImpulse;
                this.a.y = cp2.normalImpulse;
                assert (this.a.x >= 0.0f && this.a.y >= 0.0f);
                this.dv1.x = -wB * cp1.rB.y + vB.x - vA.x + wA * cp1.rA.y;
                this.dv1.y = wB * cp1.rB.x + vB.y - vA.y - wA * cp1.rA.x;
                this.dv2.x = -wB * cp2.rB.y + vB.x - vA.x + wA * cp2.rA.y;
                this.dv2.y = wB * cp2.rB.x + vB.y - vA.y - wA * cp2.rA.x;
                float vn1 = this.dv1.x * normal.x + this.dv1.y * normal.y;
                float vn2 = this.dv2.x * normal.x + this.dv2.y * normal.y;
                this.b.x = vn1 - cp1.velocityBias;
                this.b.y = vn2 - cp2.velocityBias;
                Mat22 R = vc.K;
                this.b.x -= R.ex.x * this.a.x + R.ey.x * this.a.y;
                this.b.y -= R.ex.y * this.a.x + R.ey.y * this.a.y;
                Mat22.mulToOutUnsafe(vc.normalMass, this.b, this.x);
                this.x.x *= -1.0f;
                this.x.y *= -1.0f;
                if (this.x.x >= 0.0f && this.x.y >= 0.0f) {
                    this.d.set(this.x).subLocal(this.a);
                    this.P1.set(normal).mulLocal(this.d.x);
                    this.P2.set(normal).mulLocal(this.d.y);
                    this.temp1.set(this.P1).addLocal(this.P2);
                    this.temp2.set(this.temp1).mulLocal(mA);
                    vA.subLocal(this.temp2);
                    this.temp2.set(this.temp1).mulLocal(mB);
                    vB.addLocal(this.temp2);
                    wA -= iA * (Vec2.cross(cp1.rA, this.P1) + Vec2.cross(cp2.rA, this.P2));
                    wB += iB * (Vec2.cross(cp1.rB, this.P1) + Vec2.cross(cp2.rB, this.P2));
                    cp1.normalImpulse = this.x.x;
                    cp2.normalImpulse = this.x.y;
                } else {
                    this.x.x = -cp1.normalMass * this.b.x;
                    this.x.y = 0.0f;
                    vn1 = 0.0f;
                    vn2 = vc.K.ex.y * this.x.x + this.b.y;
                    if (this.x.x >= 0.0f && vn2 >= 0.0f) {
                        this.d.set(this.x).subLocal(this.a);
                        this.P1.set(normal).mulLocal(this.d.x);
                        this.P2.set(normal).mulLocal(this.d.y);
                        this.temp1.set(this.P1).addLocal(this.P2);
                        this.temp2.set(this.temp1).mulLocal(mA);
                        vA.subLocal(this.temp2);
                        this.temp2.set(this.temp1).mulLocal(mB);
                        vB.addLocal(this.temp2);
                        wA -= iA * (Vec2.cross(cp1.rA, this.P1) + Vec2.cross(cp2.rA, this.P2));
                        wB += iB * (Vec2.cross(cp1.rB, this.P1) + Vec2.cross(cp2.rB, this.P2));
                        cp1.normalImpulse = this.x.x;
                        cp2.normalImpulse = this.x.y;
                    } else {
                        this.x.x = 0.0f;
                        this.x.y = -cp2.normalMass * this.b.y;
                        vn1 = vc.K.ey.x * this.x.y + this.b.x;
                        vn2 = 0.0f;
                        if (this.x.y >= 0.0f && vn1 >= 0.0f) {
                            this.d.set(this.x).subLocal(this.a);
                            this.P1.set(normal).mulLocal(this.d.x);
                            this.P2.set(normal).mulLocal(this.d.y);
                            this.temp1.set(this.P1).addLocal(this.P2);
                            this.temp2.set(this.temp1).mulLocal(mA);
                            vA.subLocal(this.temp2);
                            this.temp2.set(this.temp1).mulLocal(mB);
                            vB.addLocal(this.temp2);
                            wA -= iA * (Vec2.cross(cp1.rA, this.P1) + Vec2.cross(cp2.rA, this.P2));
                            wB += iB * (Vec2.cross(cp1.rB, this.P1) + Vec2.cross(cp2.rB, this.P2));
                            cp1.normalImpulse = this.x.x;
                            cp2.normalImpulse = this.x.y;
                        } else {
                            this.x.x = 0.0f;
                            this.x.y = 0.0f;
                            vn1 = this.b.x;
                            vn2 = this.b.y;
                            if (vn1 >= 0.0f && vn2 >= 0.0f) {
                                this.d.set(this.x).subLocal(this.a);
                                this.P1.set(normal).mulLocal(this.d.x);
                                this.P2.set(normal).mulLocal(this.d.y);
                                this.temp1.set(this.P1).addLocal(this.P2);
                                this.temp2.set(this.temp1).mulLocal(mA);
                                vA.subLocal(this.temp2);
                                this.temp2.set(this.temp1).mulLocal(mB);
                                vB.addLocal(this.temp2);
                                wA -= iA * (Vec2.cross(cp1.rA, this.P1) + Vec2.cross(cp2.rA, this.P2));
                                wB += iB * (Vec2.cross(cp1.rB, this.P1) + Vec2.cross(cp2.rB, this.P2));
                                cp1.normalImpulse = this.x.x;
                                cp2.normalImpulse = this.x.y;
                            }
                        }
                    }
                }
            }
            this.m_velocities[indexA].w = wA;
            this.m_velocities[indexB].w = wB;
        }
    }

    public void storeImpulses() {
        for (int i = 0; i < this.m_count; ++i) {
            ContactVelocityConstraint vc = this.m_velocityConstraints[i];
            Manifold manifold = this.m_contacts[vc.contactIndex].getManifold();
            for (int j = 0; j < vc.pointCount; ++j) {
                manifold.points[j].normalImpulse = vc.points[j].normalImpulse;
                manifold.points[j].tangentImpulse = vc.points[j].tangentImpulse;
            }
        }
    }

    public final boolean solvePositionConstraints() {
        float minSeparation = 0.0f;
        for (int i = 0; i < this.m_count; ++i) {
            ContactPositionConstraint pc = this.m_positionConstraints[i];
            int indexA = pc.indexA;
            int indexB = pc.indexB;
            float mA = pc.invMassA;
            float iA = pc.invIA;
            Vec2 localCenterA = pc.localCenterA;
            float mB = pc.invMassB;
            float iB = pc.invIB;
            Vec2 localCenterB = pc.localCenterB;
            int pointCount = pc.pointCount;
            Vec2 cA = this.m_positions[indexA].c;
            float aA = this.m_positions[indexA].a;
            Vec2 cB = this.m_positions[indexB].c;
            float aB = this.m_positions[indexB].a;
            for (int j = 0; j < pointCount; ++j) {
                this.xfA.q.set(aA);
                this.xfB.q.set(aB);
                Rot.mulToOutUnsafe(this.xfA.q, localCenterA, this.xfA.p);
                this.xfA.p.negateLocal().addLocal(cA);
                Rot.mulToOutUnsafe(this.xfB.q, localCenterB, this.xfB.p);
                this.xfB.p.negateLocal().addLocal(cB);
                PositionSolverManifold psm = this.psolver;
                psm.initialize(pc, this.xfA, this.xfB, j);
                Vec2 normal = psm.normal;
                Vec2 point = psm.point;
                float separation = psm.separation;
                this.rA.set(point).subLocal(cA);
                this.rB.set(point).subLocal(cB);
                minSeparation = MathUtils.min(minSeparation, separation);
                float C = MathUtils.clamp(0.2f * (separation + 0.005f), -0.2f, 0.0f);
                float rnA = Vec2.cross(this.rA, normal);
                float rnB = Vec2.cross(this.rB, normal);
                float K = mA + mB + iA * rnA * rnA + iB * rnB * rnB;
                float impulse = K > 0.0f ? -C / K : 0.0f;
                this.P.set(normal).mulLocal(impulse);
                cA.subLocal(this.temp.set(this.P).mulLocal(mA));
                aA -= iA * Vec2.cross(this.rA, this.P);
                cB.addLocal(this.temp.set(this.P).mulLocal(mB));
                aB += iB * Vec2.cross(this.rB, this.P);
            }
            this.m_positions[indexA].a = aA;
            this.m_positions[indexB].a = aB;
        }
        return minSeparation >= -0.015f;
    }

    public boolean solveTOIPositionConstraints(int toiIndexA, int toiIndexB) {
        float minSeparation = 0.0f;
        for (int i = 0; i < this.m_count; ++i) {
            ContactPositionConstraint pc = this.m_positionConstraints[i];
            int indexA = pc.indexA;
            int indexB = pc.indexB;
            Vec2 localCenterA = pc.localCenterA;
            Vec2 localCenterB = pc.localCenterB;
            int pointCount = pc.pointCount;
            float mA = 0.0f;
            float iA = 0.0f;
            if (indexA == toiIndexA || indexA == toiIndexB) {
                mA = pc.invMassA;
                iA = pc.invIA;
            }
            float mB = 0.0f;
            float iB = 0.0f;
            if (indexB == toiIndexA || indexB == toiIndexB) {
                mB = pc.invMassB;
                iB = pc.invIB;
            }
            Vec2 cA = this.m_positions[indexA].c;
            float aA = this.m_positions[indexA].a;
            Vec2 cB = this.m_positions[indexB].c;
            float aB = this.m_positions[indexB].a;
            for (int j = 0; j < pointCount; ++j) {
                this.xfA.q.set(aA);
                this.xfB.q.set(aB);
                Rot.mulToOutUnsafe(this.xfA.q, localCenterA, this.xfA.p);
                this.xfA.p.negateLocal().addLocal(cA);
                Rot.mulToOutUnsafe(this.xfB.q, localCenterB, this.xfB.p);
                this.xfB.p.negateLocal().addLocal(cB);
                PositionSolverManifold psm = this.psolver;
                psm.initialize(pc, this.xfA, this.xfB, j);
                Vec2 normal = psm.normal;
                Vec2 point = psm.point;
                float separation = psm.separation;
                this.rA.set(point).subLocal(cA);
                this.rB.set(point).subLocal(cB);
                minSeparation = MathUtils.min(minSeparation, separation);
                float C = MathUtils.clamp(0.75f * (separation + 0.005f), -0.2f, 0.0f);
                float rnA = Vec2.cross(this.rA, normal);
                float rnB = Vec2.cross(this.rB, normal);
                float K = mA + mB + iA * rnA * rnA + iB * rnB * rnB;
                float impulse = K > 0.0f ? -C / K : 0.0f;
                this.P.set(normal).mulLocal(impulse);
                cA.subLocal(this.temp.set(this.P).mulLocal(mA));
                aA -= iA * Vec2.cross(this.rA, this.P);
                cB.addLocal(this.temp.set(this.P).mulLocal(mB));
                aB += iB * Vec2.cross(this.rB, this.P);
            }
            this.m_positions[indexA].a = aA;
            this.m_positions[indexB].a = aB;
        }
        return minSeparation >= -0.0075f;
    }

    public static class ContactSolverDef {
        public TimeStep step;
        public Contact[] contacts;
        public int count;
        public Position[] positions;
        public Velocity[] velocities;
    }
}

