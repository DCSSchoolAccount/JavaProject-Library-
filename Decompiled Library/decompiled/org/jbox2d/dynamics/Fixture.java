/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.broadphase.BroadPhase;
import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.FixtureProxy;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;

public class Fixture {
    public float m_density;
    public Fixture m_next = null;
    public Body m_body = null;
    public Shape m_shape = null;
    public float m_friction;
    public float m_restitution;
    public FixtureProxy[] m_proxies = null;
    public int m_proxyCount = 0;
    public final Filter m_filter;
    public boolean m_isSensor;
    public Object m_userData = null;
    private final AABB pool1 = new AABB();
    private final AABB pool2 = new AABB();
    private final Vec2 displacement = new Vec2();

    public Fixture() {
        this.m_filter = new Filter();
    }

    public ShapeType getType() {
        return this.m_shape.getType();
    }

    public Shape getShape() {
        return this.m_shape;
    }

    public boolean isSensor() {
        return this.m_isSensor;
    }

    public void setSensor(boolean sensor) {
        if (sensor != this.m_isSensor) {
            this.m_body.setAwake(true);
            this.m_isSensor = sensor;
        }
    }

    public void setFilterData(Filter filter) {
        this.m_filter.set(filter);
        this.refilter();
    }

    public Filter getFilterData() {
        return this.m_filter;
    }

    public void refilter() {
        if (this.m_body == null) {
            return;
        }
        ContactEdge edge = this.m_body.getContactList();
        while (edge != null) {
            Contact contact = edge.contact;
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            if (fixtureA == this || fixtureB == this) {
                contact.flagForFiltering();
            }
            edge = edge.next;
        }
        World world = this.m_body.getWorld();
        if (world == null) {
            return;
        }
        BroadPhase broadPhase = world.m_contactManager.m_broadPhase;
        for (int i = 0; i < this.m_proxyCount; ++i) {
            broadPhase.touchProxy(this.m_proxies[i].proxyId);
        }
    }

    public Body getBody() {
        return this.m_body;
    }

    public Fixture getNext() {
        return this.m_next;
    }

    public void setDensity(float density) {
        assert (density >= 0.0f);
        this.m_density = density;
    }

    public float getDensity() {
        return this.m_density;
    }

    public Object getUserData() {
        return this.m_userData;
    }

    public void setUserData(Object data) {
        this.m_userData = data;
    }

    public boolean testPoint(Vec2 p) {
        return this.m_shape.testPoint(this.m_body.m_xf, p);
    }

    public boolean raycast(RayCastOutput output, RayCastInput input, int childIndex) {
        return this.m_shape.raycast(output, input, this.m_body.m_xf, childIndex);
    }

    public void getMassData(MassData massData) {
        this.m_shape.computeMass(massData, this.m_density);
    }

    public float getFriction() {
        return this.m_friction;
    }

    public void setFriction(float friction) {
        this.m_friction = friction;
    }

    public float getRestitution() {
        return this.m_restitution;
    }

    public void setRestitution(float restitution) {
        this.m_restitution = restitution;
    }

    public AABB getAABB(int childIndex) {
        assert (childIndex >= 0 && childIndex < this.m_proxyCount);
        return this.m_proxies[childIndex].aabb;
    }

    public void dump(int bodyIndex) {
    }

    public void create(Body body, FixtureDef def) {
        this.m_userData = def.userData;
        this.m_friction = def.friction;
        this.m_restitution = def.restitution;
        this.m_body = body;
        this.m_next = null;
        this.m_filter.set(def.filter);
        this.m_isSensor = def.isSensor;
        this.m_shape = def.shape.clone();
        int childCount = this.m_shape.getChildCount();
        if (this.m_proxies == null) {
            this.m_proxies = new FixtureProxy[childCount];
            for (int i = 0; i < childCount; ++i) {
                this.m_proxies[i] = new FixtureProxy();
                this.m_proxies[i].fixture = null;
                this.m_proxies[i].proxyId = -1;
            }
        }
        if (this.m_proxies.length < childCount) {
            FixtureProxy[] old = this.m_proxies;
            int newLen = MathUtils.max(old.length * 2, childCount);
            this.m_proxies = new FixtureProxy[newLen];
            System.arraycopy(old, 0, this.m_proxies, 0, old.length);
            for (int i = 0; i < newLen; ++i) {
                if (i >= old.length) {
                    this.m_proxies[i] = new FixtureProxy();
                }
                this.m_proxies[i].fixture = null;
                this.m_proxies[i].proxyId = -1;
            }
        }
        this.m_proxyCount = 0;
        this.m_density = def.density;
    }

    public void destroy() {
        assert (this.m_proxyCount == 0);
        this.m_shape = null;
        this.m_proxies = null;
        this.m_next = null;
    }

    public void createProxies(BroadPhase broadPhase, Transform xf) {
        assert (this.m_proxyCount == 0);
        this.m_proxyCount = this.m_shape.getChildCount();
        int i = 0;
        while (i < this.m_proxyCount) {
            FixtureProxy proxy = this.m_proxies[i];
            this.m_shape.computeAABB(proxy.aabb, xf, i);
            proxy.proxyId = broadPhase.createProxy(proxy.aabb, proxy);
            proxy.fixture = this;
            proxy.childIndex = i++;
        }
    }

    public void destroyProxies(BroadPhase broadPhase) {
        for (int i = 0; i < this.m_proxyCount; ++i) {
            FixtureProxy proxy = this.m_proxies[i];
            broadPhase.destroyProxy(proxy.proxyId);
            proxy.proxyId = -1;
        }
        this.m_proxyCount = 0;
    }

    protected void synchronize(BroadPhase broadPhase, Transform transform1, Transform transform2) {
        if (this.m_proxyCount == 0) {
            return;
        }
        for (int i = 0; i < this.m_proxyCount; ++i) {
            FixtureProxy proxy = this.m_proxies[i];
            AABB aabb1 = this.pool1;
            AABB aab = this.pool2;
            this.m_shape.computeAABB(aabb1, transform1, proxy.childIndex);
            this.m_shape.computeAABB(aab, transform2, proxy.childIndex);
            proxy.aabb.lowerBound.x = aabb1.lowerBound.x < aab.lowerBound.x ? aabb1.lowerBound.x : aab.lowerBound.x;
            proxy.aabb.lowerBound.y = aabb1.lowerBound.y < aab.lowerBound.y ? aabb1.lowerBound.y : aab.lowerBound.y;
            proxy.aabb.upperBound.x = aabb1.upperBound.x > aab.upperBound.x ? aabb1.upperBound.x : aab.upperBound.x;
            proxy.aabb.upperBound.y = aabb1.upperBound.y > aab.upperBound.y ? aabb1.upperBound.y : aab.upperBound.y;
            this.displacement.x = transform2.p.x - transform1.p.x;
            this.displacement.y = transform2.p.y - transform1.p.y;
            broadPhase.moveProxy(proxy.proxyId, proxy.aabb, this.displacement);
        }
    }
}

