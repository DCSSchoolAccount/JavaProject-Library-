/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.CollisionEvent;
import city.cs.engine.DynamicBody;
import city.cs.engine.Fixture;
import city.cs.engine.Sensor;
import city.cs.engine.SensorEvent;
import city.cs.engine.SimulationSettings;
import city.cs.engine.SolidFixture;
import city.cs.engine.StaticBody;
import city.cs.engine.StepEvent;
import city.cs.engine.StepListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.Contact;

public class World {
    public static final float DEFAULT_GRAVITY = 9.8f;
    private static final Vec2 UNIT_UP = new Vec2(0.0f, 1.0f);
    private List<DeferredContact> contactEvents;
    private Queue<DeferredContact> contactEventDispatchQueue;
    private List<StepListener> stepListeners;
    private Queue<StepListener> stepListenerDispatchQueue;
    private List<ChangeListener> changeListeners;
    private org.jbox2d.dynamics.World b2world;
    private float pendingSimTime;
    private Timer simTimer;
    private SimulationSettings simulationSettings;
    public static final int version = 40;

    public World(int framesPerSecond) {
        this.simulationSettings = new SimulationSettings(framesPerSecond);
        Vec2 worldGravity = new Vec2(0.0f, -9.8f);
        this.b2world = new org.jbox2d.dynamics.World(worldGravity);
        this.stepListeners = new LinkedList<StepListener>();
        this.contactEvents = new LinkedList<DeferredContact>();
        this.contactEventDispatchQueue = null;
        this.changeListeners = new LinkedList<ChangeListener>();
        this.b2world.setContactListener(new CollisionUpdater());
        this.pendingSimTime = 0.0f;
        this.simTimer = new Timer(this.simulationSettings.getTimeStepMilli(), new ActionListener(){
            private int tsm;
            {
                this.tsm = World.this.simulationSettings.getTimeStepMilli();
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                World.this.step();
                int tsmNow = World.this.simulationSettings.getTimeStepMilli();
                if (tsmNow != this.tsm) {
                    System.err.println("changing frame rate");
                    this.tsm = tsmNow;
                    Timer t = (Timer)evt.getSource();
                    t.setDelay(this.tsm);
                }
            }
        });
    }

    public World() {
        this(24);
    }

    public void start() {
        if (!this.simTimer.isRunning()) {
            this.simTimer.start();
        }
    }

    public void stop() {
        if (this.simTimer.isRunning()) {
            this.simTimer.stop();
        }
    }

    public boolean isRunning() {
        return this.simTimer.isRunning();
    }

    public void oneStep() {
        if (!this.isRunning()) {
            float simTimeStep = this.simulationSettings.getSimTimeStep();
            StepEvent e = new StepEvent(this, simTimeStep);
            this.preStep(e);
            this.simulationStep(simTimeStep);
            this.postStep(e);
            this.propagateChanges();
        }
    }

    private void step() {
        float simTimeStep = this.simulationSettings.getSimTimeStep();
        this.pendingSimTime += this.simulationSettings.getTimeStep();
        if (this.pendingSimTime >= simTimeStep) {
            do {
                StepEvent e = new StepEvent(this, simTimeStep);
                this.preStep(e);
                if (!this.isRunning()) break;
                this.simulationStep(simTimeStep);
                this.postStep(e);
                this.pendingSimTime -= simTimeStep;
            } while (this.pendingSimTime >= simTimeStep);
            this.propagateChanges();
        }
        this.simulationSettings.step();
    }

    private void simulationStep(float simTimeStep) {
        this.b2world.step(simTimeStep, this.simulationSettings.getVelocityIterations(), this.simulationSettings.getPositionIterations());
        this.dispatchContacts();
    }

    public SimulationSettings getSimulationSettings() {
        return this.simulationSettings;
    }

    void destroyBody(Body b) {
        org.jbox2d.dynamics.Body b2body = b.getBox2DBody();
        if (b.getWorld() != this) {
            return;
        }
        if (b instanceof StepListener) {
            this.removeStepListener((StepListener)((Object)b));
        }
        this.removeCollisions(b);
        b2body.setUserData(null);
        this.b2world.destroyBody(b2body);
        b.dispose();
    }

    private void removeCollisions(Body b) {
        this.removeCollisions(this.contactEvents, b);
        if (this.contactEventDispatchQueue != null) {
            this.removeCollisions(this.contactEventDispatchQueue, b);
        }
    }

    private void removeCollisions(Iterable<DeferredContact> cl, Body b) {
        Iterator<DeferredContact> ci = cl.iterator();
        while (ci.hasNext()) {
            DeferredContact ce = ci.next();
            if (ce.getReportingFixture().getBody() != b && ce.getOtherFixture().getBody() != b) continue;
            ci.remove();
        }
    }

    void removeCollisions(Fixture f) {
        this.removeCollisions(this.contactEvents, f);
        if (this.contactEventDispatchQueue != null) {
            this.removeCollisions(this.contactEventDispatchQueue, f);
        }
    }

    private void removeCollisions(Iterable<DeferredContact> cl, Fixture f) {
        Iterator<DeferredContact> ci = cl.iterator();
        while (ci.hasNext()) {
            DeferredContact ce = ci.next();
            if (ce.getReportingFixture() != f && ce.getOtherFixture() != f) continue;
            ci.remove();
        }
    }

    public void setGravity(float g) {
        this.b2world.setGravity(new Vec2(0.0f, -g));
    }

    public float getGravity() {
        return this.b2world.getGravity().y * -1.0f;
    }

    org.jbox2d.dynamics.World getBox2DWorld() {
        return this.b2world;
    }

    public void addStepListener(StepListener listener) {
        this.stepListeners.add(listener);
    }

    public void removeStepListener(StepListener listener) {
        this.stepListeners.remove(listener);
        if (this.stepListenerDispatchQueue != null) {
            this.stepListenerDispatchQueue.remove(listener);
        }
    }

    private void preStep(StepEvent e) {
        this.stepListenerDispatchQueue = new LinkedList<StepListener>();
        this.stepListenerDispatchQueue.addAll(this.stepListeners);
        StepListener sl = this.stepListenerDispatchQueue.poll();
        while (sl != null) {
            sl.preStep(e);
            sl = this.stepListenerDispatchQueue.poll();
        }
        this.stepListenerDispatchQueue = null;
        this.contactEvents.clear();
    }

    private void postStep(StepEvent se) {
        this.stepListenerDispatchQueue = new LinkedList<StepListener>();
        this.stepListenerDispatchQueue.addAll(this.stepListeners);
        StepListener sl = this.stepListenerDispatchQueue.poll();
        while (sl != null) {
            sl.postStep(se);
            sl = this.stepListenerDispatchQueue.poll();
        }
        this.stepListenerDispatchQueue = null;
    }

    void addChangeListener(ChangeListener listener) {
        this.changeListeners.add(listener);
    }

    void removeChangeListener(ChangeListener listener) {
        this.changeListeners.remove(listener);
    }

    private void propagateChanges() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener cl : this.changeListeners) {
            cl.stateChanged(e);
        }
    }

    private void dispatchContacts() {
        this.contactEventDispatchQueue = new LinkedList<DeferredContact>();
        for (DeferredContact e : this.contactEvents) {
            this.contactEventDispatchQueue.add(e);
            if (!(e instanceof DeferredCollision)) continue;
            DeferredCollision ce = (DeferredCollision)e;
            this.contactEventDispatchQueue.add(ce.inverse());
        }
        DeferredContact ce = this.contactEventDispatchQueue.poll();
        while (ce != null) {
            ce.dispatch();
            ce = this.contactEventDispatchQueue.poll();
        }
        this.contactEventDispatchQueue = null;
    }

    List<CollisionEvent> getCollisionEvents() {
        LinkedList<CollisionEvent> collisions = new LinkedList<CollisionEvent>();
        for (DeferredContact ce : this.contactEvents) {
            if (!(ce instanceof DeferredCollision)) continue;
            collisions.add(((DeferredCollision)ce).event);
        }
        return collisions;
    }

    List<Body> getBodies() {
        LinkedList<Body> bodiesList = new LinkedList<Body>();
        for (org.jbox2d.dynamics.Body b2body = this.b2world.getBodyList(); b2body != null; b2body = b2body.getNext()) {
            Body body = Body.getGameBody(b2body);
            if (body == null) continue;
            bodiesList.add(body);
        }
        return bodiesList;
    }

    public List<StaticBody> getStaticBodies() {
        LinkedList<StaticBody> bodiesList = new LinkedList<StaticBody>();
        for (org.jbox2d.dynamics.Body b2body = this.b2world.getBodyList(); b2body != null; b2body = b2body.getNext()) {
            Body body = Body.getGameBody(b2body);
            if (!(body instanceof StaticBody)) continue;
            bodiesList.add((StaticBody)body);
        }
        return bodiesList;
    }

    public List<DynamicBody> getDynamicBodies() {
        LinkedList<DynamicBody> bodiesList = new LinkedList<DynamicBody>();
        for (org.jbox2d.dynamics.Body b2body = this.b2world.getBodyList(); b2body != null; b2body = b2body.getNext()) {
            Body body = Body.getGameBody(b2body);
            if (!(body instanceof DynamicBody)) continue;
            bodiesList.add((DynamicBody)body);
        }
        return bodiesList;
    }

    private class CollisionUpdater
    implements ContactListener {
        private CollisionUpdater() {
        }

        @Override
        public void beginContact(Contact contact) {
            this.addSensorEvent(contact.getFixtureA(), contact.getFixtureB(), true);
        }

        @Override
        public void endContact(Contact contact) {
            this.addSensorEvent(contact.getFixtureA(), contact.getFixtureB(), false);
        }

        private void addSensorEvent(org.jbox2d.dynamics.Fixture b2fixtureA, org.jbox2d.dynamics.Fixture b2fixtureB, boolean begin) {
            if (b2fixtureA.isSensor() == b2fixtureB.isSensor()) {
                return;
            }
            Fixture fixtureA = Fixture.getGameFixture(b2fixtureA);
            Fixture fixtureB = Fixture.getGameFixture(b2fixtureB);
            if (b2fixtureA.isSensor() && fixtureA instanceof Sensor && fixtureB instanceof SolidFixture) {
                World.this.contactEvents.add(new DeferredSensor((Sensor)fixtureA, (SolidFixture)fixtureB, begin));
            } else if (b2fixtureB.isSensor() && fixtureB instanceof Sensor && fixtureA instanceof SolidFixture) {
                World.this.contactEvents.add(new DeferredSensor((Sensor)fixtureB, (SolidFixture)fixtureA, begin));
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
            org.jbox2d.dynamics.Fixture b2fixtureA = contact.getFixtureA();
            org.jbox2d.dynamics.Fixture b2fixtureB = contact.getFixtureB();
            Fixture fixtureA = Fixture.getGameFixture(b2fixtureA);
            Fixture fixtureB = Fixture.getGameFixture(b2fixtureB);
            if (fixtureA == null || fixtureB == null || !(fixtureA instanceof SolidFixture) || !(fixtureB instanceof SolidFixture)) {
                return;
            }
            Body bodyA = Body.getGameBodyFromFixture(b2fixtureA);
            Body bodyB = Body.getGameBodyFromFixture(b2fixtureB);
            if (bodyA == null || bodyB == null) {
                return;
            }
            Collision.PointState[] state1 = new Collision.PointState[2];
            Collision.PointState[] state2 = new Collision.PointState[2];
            Collision.getPointStates(state1, state2, oldManifold, contact.getManifold());
            if (state2[0] == Collision.PointState.ADD_STATE) {
                CollisionEvent ce = new CollisionEvent((SolidFixture)fixtureA, (SolidFixture)fixtureB, contact);
                World.this.contactEvents.add(new DeferredCollision(ce));
            }
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
        }
    }

    private class DeferredCollision
    implements DeferredContact {
        public CollisionEvent event;

        public DeferredCollision(CollisionEvent event) {
            this.event = event;
        }

        @Override
        public Fixture getReportingFixture() {
            return this.event.getReportingFixture();
        }

        @Override
        public Fixture getOtherFixture() {
            return this.event.getOtherFixture();
        }

        @Override
        public void dispatch() {
            this.event.getReportingBody().collide(this.event);
        }

        public DeferredCollision inverse() {
            return new DeferredCollision(this.event.inverse());
        }
    }

    private class DeferredSensor
    implements DeferredContact {
        public boolean begin;
        public SensorEvent event;

        public DeferredSensor(Sensor sensor, SolidFixture f, boolean begin) {
            this.event = new SensorEvent(sensor, f);
            this.begin = begin;
        }

        @Override
        public Fixture getReportingFixture() {
            return this.event.getSensor();
        }

        @Override
        public Fixture getOtherFixture() {
            return this.event.getContactFixture();
        }

        @Override
        public void dispatch() {
            if (this.begin) {
                this.event.getSensor().beginContact(this.event);
            } else {
                this.event.getSensor().endContact(this.event);
            }
        }
    }

    private static interface DeferredContact {
        public Fixture getReportingFixture();

        public Fixture getOtherFixture();

        public void dispatch();
    }
}

