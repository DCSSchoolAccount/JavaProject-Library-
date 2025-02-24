/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.profile;

import org.jbox2d.collision.broadphase.DynamicTree;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.PrismaticJoint;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.pooling.normal.DefaultWorldPool;
import org.jbox2d.profile.SettingsPerformanceTest;

public class PistonBenchmark
extends SettingsPerformanceTest {
    public static int iters = 5;
    public static int frames = 800;
    public static float timeStep = 0.016666668f;
    public static int velIters = 8;
    public static int posIters = 3;
    public RevoluteJoint m_joint1;
    public PrismaticJoint m_joint2;
    public World world;

    public PistonBenchmark() {
        super(iters);
    }

    public static void main(String[] args) {
        PistonBenchmark benchmark = new PistonBenchmark();
        benchmark.go();
    }

    @Override
    public void runBenchmarkWorld() {
        Body body;
        DynamicTree strategy = new DynamicTree();
        this.world = new World(new Vec2(0.0f, -10.0f), new DefaultWorldPool(100, 10), strategy);
        Body ground = null;
        BodyDef bd = new BodyDef();
        ground = this.world.createBody(bd);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(5.0f, 100.0f);
        bd = new BodyDef();
        bd.type = BodyType.STATIC;
        FixtureDef sides = new FixtureDef();
        sides.shape = shape;
        sides.density = 0.0f;
        sides.friction = 0.0f;
        sides.restitution = 0.8f;
        sides.filter.categoryBits = 4;
        sides.filter.maskBits = 2;
        bd.position.set(-10.01f, 50.0f);
        Body bod = this.world.createBody(bd);
        bod.createFixture(sides);
        bd.position.set(10.01f, 50.0f);
        bod = this.world.createBody(bd);
        bod.createFixture(sides);
        FixtureDef fd = new FixtureDef();
        BodyDef bd2 = new BodyDef();
        bd2.type = BodyType.DYNAMIC;
        int numPieces = 5;
        float radius = 4.0f;
        bd2.position = new Vec2(0.0f, 25.0f);
        Body body2 = this.world.createBody(bd2);
        for (int i = 0; i < numPieces; ++i) {
            CircleShape cd = new CircleShape();
            cd.m_radius = 0.5f;
            fd.shape = cd;
            fd.density = 25.0f;
            fd.friction = 0.1f;
            fd.restitution = 0.9f;
            float xPos = radius * (float)Math.cos(Math.PI * 2 * (double)((float)i / (float)numPieces));
            float yPos = radius * (float)Math.sin(Math.PI * 2 * (double)((float)i / (float)numPieces));
            cd.m_p.set(xPos, yPos);
            body2.createFixture(fd);
        }
        RevoluteJointDef rjd = new RevoluteJointDef();
        rjd.initialize(body2, ground, body2.getPosition());
        rjd.motorSpeed = (float)Math.PI;
        rjd.maxMotorTorque = 1000000.0f;
        rjd.enableMotor = true;
        this.world.createJoint(rjd);
        Body prevBody = ground;
        shape = new PolygonShape();
        shape.setAsBox(0.5f, 2.0f);
        bd2 = new BodyDef();
        bd2.type = BodyType.DYNAMIC;
        bd2.position.set(0.0f, 7.0f);
        Body body3 = this.world.createBody(bd2);
        body3.createFixture(shape, 2.0f);
        RevoluteJointDef rjd2 = new RevoluteJointDef();
        rjd2.initialize(prevBody, body3, new Vec2(0.0f, 5.0f));
        rjd2.motorSpeed = (float)Math.PI;
        rjd2.maxMotorTorque = 20000.0f;
        rjd2.enableMotor = true;
        this.m_joint1 = (RevoluteJoint)this.world.createJoint(rjd2);
        prevBody = body3;
        shape = new PolygonShape();
        shape.setAsBox(0.5f, 4.0f);
        bd2 = new BodyDef();
        bd2.type = BodyType.DYNAMIC;
        bd2.position.set(0.0f, 13.0f);
        body3 = this.world.createBody(bd2);
        body3.createFixture(shape, 2.0f);
        rjd2 = new RevoluteJointDef();
        rjd2.initialize(prevBody, body3, new Vec2(0.0f, 9.0f));
        rjd2.enableMotor = false;
        this.world.createJoint(rjd2);
        prevBody = body3;
        shape = new PolygonShape();
        shape.setAsBox(7.0f, 2.0f);
        bd2 = new BodyDef();
        bd2.type = BodyType.DYNAMIC;
        bd2.position.set(0.0f, 17.0f);
        body3 = this.world.createBody(bd2);
        FixtureDef piston = new FixtureDef();
        piston.shape = shape;
        piston.density = 2.0f;
        piston.filter.categoryBits = 1;
        piston.filter.maskBits = 2;
        body3.createFixture(piston);
        RevoluteJointDef rjd3 = new RevoluteJointDef();
        rjd3.initialize(prevBody, body3, new Vec2(0.0f, 17.0f));
        this.world.createJoint(rjd3);
        PrismaticJointDef pjd = new PrismaticJointDef();
        pjd.initialize(ground, body3, new Vec2(0.0f, 17.0f), new Vec2(0.0f, 1.0f));
        pjd.maxMotorForce = 1000.0f;
        pjd.enableMotor = true;
        this.m_joint2 = (PrismaticJoint)this.world.createJoint(pjd);
        PolygonShape sd = new PolygonShape();
        bd2 = new BodyDef();
        bd2.type = BodyType.DYNAMIC;
        FixtureDef fixture = new FixtureDef();
        for (int i = 0; i < 100; ++i) {
            sd.setAsBox(0.4f, 0.3f);
            bd2.position.set(-1.0f, 23.0f + (float)i);
            bd2.bullet = false;
            body = this.world.createBody(bd2);
            fixture.shape = sd;
            fixture.density = 0.1f;
            fixture.filter.categoryBits = 2;
            fixture.filter.maskBits = 7;
            body.createFixture(fixture);
        }
        CircleShape cd = new CircleShape();
        cd.m_radius = 0.36f;
        for (int i = 0; i < 100; ++i) {
            bd2.position.set(1.0f, 23.0f + (float)i);
            bd2.bullet = false;
            fixture.shape = cd;
            fixture.density = 2.0f;
            fixture.filter.categoryBits = 2;
            fixture.filter.maskBits = 7;
            body = this.world.createBody(bd2);
            body.createFixture(fixture);
        }
        float angle = 0.0f;
        float delta = 1.0471976f;
        Vec2[] vertices = new Vec2[6];
        for (int i = 0; i < 6; ++i) {
            vertices[i] = new Vec2(0.3f * MathUtils.cos(angle), 0.3f * MathUtils.sin(angle));
            angle += delta;
        }
        PolygonShape shape2 = new PolygonShape();
        shape2.set(vertices, 6);
        for (int i = 0; i < 100; ++i) {
            bd2.position.set(0.0f, 23.0f + (float)i);
            bd2.type = BodyType.DYNAMIC;
            bd2.fixedRotation = true;
            bd2.bullet = false;
            fixture.shape = shape2;
            fixture.density = 1.0f;
            fixture.filter.categoryBits = 2;
            fixture.filter.maskBits = 7;
            body = this.world.createBody(bd2);
            body.createFixture(fixture);
        }
        for (int i = 0; i < frames; ++i) {
            this.world.step(timeStep, posIters, velIters);
        }
    }

    @Override
    public int getFrames(int testNum) {
        return frames;
    }
}

