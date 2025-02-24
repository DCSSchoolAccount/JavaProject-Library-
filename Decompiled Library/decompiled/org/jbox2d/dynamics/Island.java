/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Timer;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Profile;
import org.jbox2d.dynamics.SolverData;
import org.jbox2d.dynamics.TimeStep;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactSolver;
import org.jbox2d.dynamics.contacts.ContactVelocityConstraint;
import org.jbox2d.dynamics.contacts.Position;
import org.jbox2d.dynamics.contacts.Velocity;
import org.jbox2d.dynamics.joints.Joint;

public class Island {
    public ContactListener m_listener;
    public Body[] m_bodies;
    public Contact[] m_contacts;
    public Joint[] m_joints;
    public Position[] m_positions;
    public Velocity[] m_velocities;
    public int m_bodyCount;
    public int m_jointCount;
    public int m_contactCount;
    public int m_bodyCapacity;
    public int m_contactCapacity;
    public int m_jointCapacity;
    private final ContactSolver contactSolver = new ContactSolver();
    private final Timer timer = new Timer();
    private final SolverData solverData = new SolverData();
    private final ContactSolver.ContactSolverDef solverDef = new ContactSolver.ContactSolverDef();
    private final ContactSolver toiContactSolver = new ContactSolver();
    private final ContactSolver.ContactSolverDef toiSolverDef = new ContactSolver.ContactSolverDef();
    private final ContactImpulse impulse = new ContactImpulse();

    public void init(int bodyCapacity, int contactCapacity, int jointCapacity, ContactListener listener) {
        int i;
        Object[] old;
        this.m_bodyCapacity = bodyCapacity;
        this.m_contactCapacity = contactCapacity;
        this.m_jointCapacity = jointCapacity;
        this.m_bodyCount = 0;
        this.m_contactCount = 0;
        this.m_jointCount = 0;
        this.m_listener = listener;
        if (this.m_bodies == null || this.m_bodyCapacity > this.m_bodies.length) {
            this.m_bodies = new Body[this.m_bodyCapacity];
        }
        if (this.m_joints == null || this.m_jointCapacity > this.m_joints.length) {
            this.m_joints = new Joint[this.m_jointCapacity];
        }
        if (this.m_contacts == null || this.m_contactCapacity > this.m_contacts.length) {
            this.m_contacts = new Contact[this.m_contactCapacity];
        }
        if (this.m_velocities == null || this.m_bodyCapacity > this.m_velocities.length) {
            old = this.m_velocities == null ? new Velocity[]{} : this.m_velocities;
            this.m_velocities = new Velocity[this.m_bodyCapacity];
            System.arraycopy(old, 0, this.m_velocities, 0, old.length);
            for (i = old.length; i < this.m_velocities.length; ++i) {
                this.m_velocities[i] = new Velocity();
            }
        }
        if (this.m_positions == null || this.m_bodyCapacity > this.m_positions.length) {
            old = this.m_positions == null ? new Position[]{} : this.m_positions;
            this.m_positions = new Position[this.m_bodyCapacity];
            System.arraycopy(old, 0, this.m_positions, 0, old.length);
            for (i = old.length; i < this.m_positions.length; ++i) {
                this.m_positions[i] = new Position();
            }
        }
    }

    public void clear() {
        this.m_bodyCount = 0;
        this.m_contactCount = 0;
        this.m_jointCount = 0;
    }

    public void solve(Profile profile, TimeStep step, Vec2 gravity, boolean allowSleep) {
        int i;
        int i2;
        float h = step.dt;
        for (i2 = 0; i2 < this.m_bodyCount; ++i2) {
            Body b = this.m_bodies[i2];
            Vec2 c = b.m_sweep.c;
            float a = b.m_sweep.a;
            Vec2 v = b.m_linearVelocity;
            float w = b.m_angularVelocity;
            b.m_sweep.c0.set(b.m_sweep.c);
            b.m_sweep.a0 = b.m_sweep.a;
            if (b.m_type == BodyType.DYNAMIC) {
                v.x += h * (b.m_gravityScale * gravity.x + b.m_invMass * b.m_force.x);
                v.y += h * (b.m_gravityScale * gravity.y + b.m_invMass * b.m_force.y);
                w += h * b.m_invI * b.m_torque;
                float a1 = MathUtils.clamp(1.0f - h * b.m_linearDamping, 0.0f, 1.0f);
                v.x *= a1;
                v.y *= a1;
                w *= MathUtils.clamp(1.0f - h * b.m_angularDamping, 0.0f, 1.0f);
            }
            this.m_positions[i2].c.x = c.x;
            this.m_positions[i2].c.y = c.y;
            this.m_positions[i2].a = a;
            this.m_velocities[i2].v.x = v.x;
            this.m_velocities[i2].v.y = v.y;
            this.m_velocities[i2].w = w;
        }
        this.timer.reset();
        this.solverData.step = step;
        this.solverData.positions = this.m_positions;
        this.solverData.velocities = this.m_velocities;
        this.solverDef.step = step;
        this.solverDef.contacts = this.m_contacts;
        this.solverDef.count = this.m_contactCount;
        this.solverDef.positions = this.m_positions;
        this.solverDef.velocities = this.m_velocities;
        this.contactSolver.init(this.solverDef);
        this.contactSolver.initializeVelocityConstraints();
        if (step.warmStarting) {
            this.contactSolver.warmStart();
        }
        for (i2 = 0; i2 < this.m_jointCount; ++i2) {
            this.m_joints[i2].initVelocityConstraints(this.solverData);
        }
        profile.solveInit = this.timer.getMilliseconds();
        this.timer.reset();
        for (i2 = 0; i2 < step.velocityIterations; ++i2) {
            for (int j = 0; j < this.m_jointCount; ++j) {
                this.m_joints[j].solveVelocityConstraints(this.solverData);
            }
            this.contactSolver.solveVelocityConstraints();
        }
        this.contactSolver.storeImpulses();
        profile.solveVelocity = this.timer.getMilliseconds();
        for (i2 = 0; i2 < this.m_bodyCount; ++i2) {
            float rotation;
            Vec2 c = this.m_positions[i2].c;
            float a = this.m_positions[i2].a;
            Vec2 v = this.m_velocities[i2].v;
            float w = this.m_velocities[i2].w;
            float translationx = v.x * h;
            float translationy = v.y * h;
            if (translationx * translationx + translationy * translationy > 4.0f) {
                float ratio = 2.0f / MathUtils.sqrt(translationx * translationx + translationy * translationy);
                v.x *= ratio;
                v.y *= ratio;
            }
            if ((rotation = h * w) * rotation > Settings.maxRotationSquared) {
                float ratio = 1.5707964f / MathUtils.abs(rotation);
                w *= ratio;
            }
            c.x += h * v.x;
            c.y += h * v.y;
            this.m_positions[i2].a = a += h * w;
            this.m_velocities[i2].w = w;
        }
        this.timer.reset();
        boolean positionSolved = false;
        for (i = 0; i < step.positionIterations; ++i) {
            boolean contactsOkay = this.contactSolver.solvePositionConstraints();
            boolean jointsOkay = true;
            for (int j = 0; j < this.m_jointCount; ++j) {
                boolean jointOkay = this.m_joints[j].solvePositionConstraints(this.solverData);
                jointsOkay = jointsOkay && jointOkay;
            }
            if (!contactsOkay || !jointsOkay) continue;
            positionSolved = true;
            break;
        }
        for (i = 0; i < this.m_bodyCount; ++i) {
            Body body = this.m_bodies[i];
            body.m_sweep.c.x = this.m_positions[i].c.x;
            body.m_sweep.c.y = this.m_positions[i].c.y;
            body.m_sweep.a = this.m_positions[i].a;
            body.m_linearVelocity.x = this.m_velocities[i].v.x;
            body.m_linearVelocity.y = this.m_velocities[i].v.y;
            body.m_angularVelocity = this.m_velocities[i].w;
            body.synchronizeTransform();
        }
        profile.solvePosition = this.timer.getMilliseconds();
        this.report(this.contactSolver.m_velocityConstraints);
        if (allowSleep) {
            int i3;
            float minSleepTime = Float.MAX_VALUE;
            float linTolSqr = 1.0E-4f;
            float angTolSqr = 0.0012184699f;
            for (i3 = 0; i3 < this.m_bodyCount; ++i3) {
                Body b = this.m_bodies[i3];
                if (b.getType() == BodyType.STATIC) continue;
                if ((b.m_flags & 4) == 0 || b.m_angularVelocity * b.m_angularVelocity > 0.0012184699f || Vec2.dot(b.m_linearVelocity, b.m_linearVelocity) > 1.0E-4f) {
                    b.m_sleepTime = 0.0f;
                    minSleepTime = 0.0f;
                    continue;
                }
                b.m_sleepTime += h;
                minSleepTime = MathUtils.min(minSleepTime, b.m_sleepTime);
            }
            if (minSleepTime >= 0.5f && positionSolved) {
                for (i3 = 0; i3 < this.m_bodyCount; ++i3) {
                    Body b = this.m_bodies[i3];
                    b.setAwake(false);
                }
            }
        }
    }

    public void solveTOI(TimeStep subStep, int toiIndexA, int toiIndexB) {
        boolean contactsOkay;
        int i;
        assert (toiIndexA < this.m_bodyCount);
        assert (toiIndexB < this.m_bodyCount);
        for (i = 0; i < this.m_bodyCount; ++i) {
            this.m_positions[i].c.x = this.m_bodies[i].m_sweep.c.x;
            this.m_positions[i].c.y = this.m_bodies[i].m_sweep.c.y;
            this.m_positions[i].a = this.m_bodies[i].m_sweep.a;
            this.m_velocities[i].v.x = this.m_bodies[i].m_linearVelocity.x;
            this.m_velocities[i].v.y = this.m_bodies[i].m_linearVelocity.y;
            this.m_velocities[i].w = this.m_bodies[i].m_angularVelocity;
        }
        this.toiSolverDef.contacts = this.m_contacts;
        this.toiSolverDef.count = this.m_contactCount;
        this.toiSolverDef.step = subStep;
        this.toiSolverDef.positions = this.m_positions;
        this.toiSolverDef.velocities = this.m_velocities;
        this.toiContactSolver.init(this.toiSolverDef);
        for (i = 0; i < subStep.positionIterations && !(contactsOkay = this.toiContactSolver.solveTOIPositionConstraints(toiIndexA, toiIndexB)); ++i) {
        }
        this.m_bodies[toiIndexA].m_sweep.c0.x = this.m_positions[toiIndexA].c.x;
        this.m_bodies[toiIndexA].m_sweep.c0.y = this.m_positions[toiIndexA].c.y;
        this.m_bodies[toiIndexA].m_sweep.a0 = this.m_positions[toiIndexA].a;
        this.m_bodies[toiIndexB].m_sweep.c0.set(this.m_positions[toiIndexB].c);
        this.m_bodies[toiIndexB].m_sweep.a0 = this.m_positions[toiIndexB].a;
        this.toiContactSolver.initializeVelocityConstraints();
        for (i = 0; i < subStep.velocityIterations; ++i) {
            this.toiContactSolver.solveVelocityConstraints();
        }
        float h = subStep.dt;
        for (int i2 = 0; i2 < this.m_bodyCount; ++i2) {
            float rotation;
            Vec2 c = this.m_positions[i2].c;
            float a = this.m_positions[i2].a;
            Vec2 v = this.m_velocities[i2].v;
            float w = this.m_velocities[i2].w;
            float translationx = v.x * h;
            float translationy = v.y * h;
            if (translationx * translationx + translationy * translationy > 4.0f) {
                float ratio = 2.0f / MathUtils.sqrt(translationx * translationx + translationy * translationy);
                v.mulLocal(ratio);
            }
            if ((rotation = h * w) * rotation > Settings.maxRotationSquared) {
                float ratio = 1.5707964f / MathUtils.abs(rotation);
                w *= ratio;
            }
            c.x += v.x * h;
            c.y += v.y * h;
            this.m_positions[i2].c.x = c.x;
            this.m_positions[i2].c.y = c.y;
            this.m_positions[i2].a = a += h * w;
            this.m_velocities[i2].v.x = v.x;
            this.m_velocities[i2].v.y = v.y;
            this.m_velocities[i2].w = w;
            Body body = this.m_bodies[i2];
            body.m_sweep.c.x = c.x;
            body.m_sweep.c.y = c.y;
            body.m_sweep.a = a;
            body.m_linearVelocity.x = v.x;
            body.m_linearVelocity.y = v.y;
            body.m_angularVelocity = w;
            body.synchronizeTransform();
        }
        this.report(this.toiContactSolver.m_velocityConstraints);
    }

    public void add(Body body) {
        assert (this.m_bodyCount < this.m_bodyCapacity);
        body.m_islandIndex = this.m_bodyCount;
        this.m_bodies[this.m_bodyCount] = body;
        ++this.m_bodyCount;
    }

    public void add(Contact contact) {
        assert (this.m_contactCount < this.m_contactCapacity);
        this.m_contacts[this.m_contactCount++] = contact;
    }

    public void add(Joint joint) {
        assert (this.m_jointCount < this.m_jointCapacity);
        this.m_joints[this.m_jointCount++] = joint;
    }

    public void report(ContactVelocityConstraint[] constraints) {
        if (this.m_listener == null) {
            return;
        }
        for (int i = 0; i < this.m_contactCount; ++i) {
            Contact c = this.m_contacts[i];
            ContactVelocityConstraint vc = constraints[i];
            this.impulse.count = vc.pointCount;
            for (int j = 0; j < vc.pointCount; ++j) {
                this.impulse.normalImpulses[j] = vc.points[j].normalImpulse;
                this.impulse.tangentImpulses[j] = vc.points[j].tangentImpulse;
            }
            this.m_listener.postSolve(c, this.impulse);
        }
    }
}

