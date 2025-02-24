/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.AttachedImage;
import city.cs.engine.BodyImage;
import city.cs.engine.CollisionEvent;
import city.cs.engine.CollisionListener;
import city.cs.engine.DestructionEvent;
import city.cs.engine.DestructionListener;
import city.cs.engine.Fixture;
import city.cs.engine.Sensor;
import city.cs.engine.Shape;
import city.cs.engine.SolidFixture;
import city.cs.engine.World;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.contacts.ContactEdge;

public abstract class Body {
    private static final Vec2 UP = new Vec2(0.0f, 1.0f);
    private static final Vec2 DOWN = new Vec2(0.0f, -1.0f);
    org.jbox2d.dynamics.Body b2body;
    private World world;
    private List<CollisionListener> collisionListeners;
    private String name;
    private List<AttachedImage> images;
    private Color fillColor;
    private Color lineColor;
    private boolean clipped;
    private boolean alwaysOutline;
    private Iterable<Fixture> fixtures;
    private List<DestructionListener> destructionListeners;

    Body(World world, BodyType type) {
        this.world = world;
        this.collisionListeners = new LinkedList<CollisionListener>();
        this.destructionListeners = new LinkedList<DestructionListener>();
        this.fixtures = new Iterable<Fixture>(){

            @Override
            public Iterator<Fixture> iterator() {
                return new FixtureIterator();
            }
        };
        BodyDef bd = new BodyDef();
        bd.type = type;
        this.b2body = world.getBox2DWorld().createBody(bd);
        this.b2body.setUserData(this);
        this.images = new LinkedList<AttachedImage>();
        this.fillColor = Color.WHITE;
        this.lineColor = Color.BLACK;
        this.clipped = false;
        this.alwaysOutline = false;
    }

    Body(World world, Shape shape, BodyType type) {
        this(world, type);
        this.addShape(shape);
    }

    public void addCollisionListener(CollisionListener listener) {
        this.collisionListeners.add(listener);
    }

    public void removeCollisionListener(CollisionListener listener) {
        this.collisionListeners.remove(listener);
    }

    public void removeAllCollisionListeners() {
        this.collisionListeners.clear();
    }

    void collide(CollisionEvent e) {
        for (CollisionListener cl : this.collisionListeners) {
            cl.collide(e);
        }
    }

    void attachImage(AttachedImage image) {
        this.images.add(image);
    }

    public AttachedImage addImage(BodyImage image) {
        return new AttachedImage(this, image, 1.0f, 0.0f, new Vec2());
    }

    public void removeAttachedImage(AttachedImage image) {
        this.images.remove(image);
    }

    public void removeAllImages() {
        this.images.clear();
    }

    public List<AttachedImage> getImages() {
        ArrayList<AttachedImage> copy = new ArrayList<AttachedImage>();
        copy.addAll(this.images);
        return copy;
    }

    public void setClipped(boolean clipped) {
        this.clipped = clipped;
    }

    public boolean isClipped() {
        return this.clipped;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addDestructionListener(DestructionListener listener) {
        this.destructionListeners.add(listener);
    }

    public void removeDestructionListener(DestructionListener listener) {
        this.destructionListeners.remove(listener);
    }

    public void removeAllDestructionListeners() {
        this.destructionListeners.clear();
    }

    public void destroy() {
        DestructionEvent de = new DestructionEvent(this);
        for (DestructionListener dl : this.destructionListeners) {
            dl.destroy(de);
        }
        this.removeAllDestructionListeners();
        this.removeAllCollisionListeners();
        for (Fixture f : this.getFixtures()) {
            if (!(f instanceof Sensor)) continue;
            ((Sensor)f).removeAllSensorListeners();
        }
        this.world.destroyBody(this);
    }

    public List<Fixture> getFixtureList() {
        LinkedList<Fixture> list = new LinkedList<Fixture>();
        for (Fixture f : this.getFixtures()) {
            list.add(f);
        }
        return list;
    }

    Iterable<Fixture> getFixtures() {
        return this.fixtures;
    }

    public String toString() {
        return super.toString() + "[name=" + this.name + "]";
    }

    org.jbox2d.dynamics.Body getBox2DBody() {
        return this.b2body;
    }

    SolidFixture addShape(Shape shape) {
        return new SolidFixture(this, shape);
    }

    public World getWorld() {
        return this.world;
    }

    public Vec2 getPosition() {
        return new Vec2(this.b2body.getPosition());
    }

    public void move(Vec2 dp) {
        this.setPosition(this.getPosition().addLocal(dp));
    }

    public void setPosition(Vec2 position) {
        this.b2body.setTransform(new Vec2(position), this.b2body.getAngle());
    }

    public Vec2 getLinearVelocity() {
        return new Vec2(this.b2body.getLinearVelocity());
    }

    public float getAngularVelocity() {
        return this.b2body.getAngularVelocity();
    }

    public float getAngle() {
        return this.b2body.getAngle();
    }

    public float getAngleDegrees() {
        return 180.0f * (float)((double)this.getAngle() / Math.PI);
    }

    public void setAngle(float theta) {
        this.b2body.setTransform(this.b2body.getPosition(), theta);
    }

    public void setAngleDegrees(float theta) {
        this.setAngle((float)((double)(theta / 180.0f) * Math.PI));
    }

    public void rotate(Vec2 p, float dTheta) {
        this.setAngle(this.getAngle() + dTheta);
        float cos = MathUtils.cos(dTheta);
        float sin = MathUtils.sin(dTheta);
        Vec2 v = this.getPosition().subLocal(p);
        this.setPosition(new Vec2(p.x + cos * v.x - sin * v.y, p.y + sin * v.x + cos * v.y));
    }

    public void rotate(float dTheta) {
        this.setAngle(this.getAngle() + dTheta);
    }

    public void rotateDegrees(Vec2 p, float dTheta) {
        this.rotate(p, dTheta * ((float)Math.PI * 2) / 360.0f);
    }

    public void rotateDegrees(float dTheta) {
        this.rotate(dTheta * ((float)Math.PI * 2) / 360.0f);
    }

    private boolean putOn(Vec2 displacement, Body other) {
        if (other == this) {
            throw new IllegalArgumentException("cannot put a body on top of itself");
        }
        Vec2 top = other.extremalPoint(UP);
        Vec2 bottom = this.extremalPoint(DOWN);
        if (top == null) {
            throw new IllegalArgumentException("top body is empty (has no shapes)");
        }
        if (bottom == null) {
            throw new IllegalArgumentException("bottom body is empty (has no shapes)");
        }
        Vec2 translate = top.sub(bottom);
        this.b2body.setTransform(this.b2body.getPosition().add(translate).add(displacement), this.getAngle());
        this.b2body.setAwake(true);
        return true;
    }

    public void putOn(Body other) {
        this.putOn(other.getPosition().x, other);
    }

    public void putOn(float x, Body other) {
        if (other == this) {
            throw new IllegalArgumentException("cannot put a body on top of itself");
        }
        if (!this.isSolid()) {
            throw new IllegalArgumentException("top body has no solid fixtures");
        }
        if (!other.isSolid()) {
            throw new IllegalArgumentException("bottom body has no solid fixtures");
        }
        Transform xf_top = other.getTop(x);
        Transform xf_bot = this.getBottom(this.getPosition().x);
        if (xf_top != null && xf_bot != null) {
            Vec2 here = this.getPosition();
            Vec2 rel_centre = here.sub(xf_bot.p);
            Vec2 pos = Transform.mul(xf_top, rel_centre);
            float angle = xf_top.q.getAngle() + xf_bot.q.getAngle() - this.getAngle();
            this.b2body.setTransform(pos, angle);
        }
    }

    private Vec2 extremalPoint(Vec2 direction) {
        Vec2 best = null;
        float dot_best = Float.NEGATIVE_INFINITY;
        for (Fixture f : this.getFixtures()) {
            Vec2 p;
            float dot_p;
            if (!(f instanceof SolidFixture) || !((dot_p = Vec2.dot(direction, p = f.extremalPoint(direction))) > dot_best)) continue;
            best = p;
            dot_best = dot_p;
        }
        return best;
    }

    public Color getFillColor() {
        return this.fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public Color getLineColor() {
        return this.lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public boolean isAlwaysOutline() {
        return this.alwaysOutline;
    }

    public void setAlwaysOutline(boolean b) {
        this.alwaysOutline = b;
    }

    void dispose() {
    }

    static Body getGameBodyFromFixture(org.jbox2d.dynamics.Fixture s) {
        return Body.getGameBody(s.getBody());
    }

    static Body getGameBody(org.jbox2d.dynamics.Body b) {
        Object userData = b.getUserData();
        if (userData != null && userData instanceof Body) {
            return (Body)userData;
        }
        return null;
    }

    public boolean contains(Vec2 p) {
        for (Fixture f : this.getFixtures()) {
            if (!f.contains(p)) continue;
            return true;
        }
        return false;
    }

    AffineTransform awtTransform() {
        Vec2 pos = this.b2body.getPosition();
        float angle = this.b2body.getAngle();
        AffineTransform transform = AffineTransform.getRotateInstance(angle);
        transform.preConcatenate(AffineTransform.getTranslateInstance(pos.x, pos.y));
        return transform;
    }

    java.awt.Shape awtShape() {
        Area a = new Area();
        for (Fixture f : this.getFixtures()) {
            if (!f.isVisible()) continue;
            a.add(new Area(f.awtShape()));
        }
        return a;
    }

    public boolean intersects(Body b) {
        Area testArea = (Area)this.awtShape();
        testArea.intersect((Area)b.awtShape());
        return !testArea.isEmpty();
    }

    public boolean intersects(Vec2 centre, float halfWidth, float halfHeight) {
        return this.awtShape().intersects(centre.x - halfWidth, centre.y - halfHeight, 2.0f * halfWidth, 2.0f * halfHeight);
    }

    private Transform getTop(float x) {
        Transform best = null;
        for (Fixture f : this.getFixtures()) {
            Transform best_f;
            if (!(f instanceof SolidFixture) || (best_f = f.getTop(x)) == null || best != null && !(best_f.p.y > best.p.y)) continue;
            best = best_f;
        }
        return best;
    }

    private Transform getBottom(float x) {
        Transform best = null;
        for (Fixture f : this.getFixtures()) {
            Transform best_f;
            if (!(f instanceof SolidFixture) || (best_f = f.getBottom(x)) == null || best != null && !(best_f.p.y < best.p.y)) continue;
            best = best_f;
        }
        return best;
    }

    private boolean isSolid() {
        for (Fixture f : this.getFixtures()) {
            if (!(f instanceof SolidFixture)) continue;
            return true;
        }
        return false;
    }

    abstract float defaultDensity();

    public float getMass() {
        return this.b2body.getMass();
    }

    public List<Body> getBodiesInContact() {
        LinkedList<Body> contacts = new LinkedList<Body>();
        ContactEdge ce = this.b2body.getContactList();
        while (ce != null) {
            Object userData;
            if (ce.other != null && (userData = ce.other.getUserData()) instanceof Body) {
                contacts.add((Body)userData);
            }
            ce = ce.next;
        }
        return contacts;
    }

    private class FixtureIterator
    implements Iterator<Fixture> {
        private org.jbox2d.dynamics.Fixture b2fixture;

        public FixtureIterator() {
            this.b2fixture = Body.this.b2body.getFixtureList();
        }

        @Override
        public boolean hasNext() {
            return this.b2fixture != null;
        }

        @Override
        public Fixture next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            Fixture f = Fixture.getGameFixture(this.b2fixture);
            this.b2fixture = this.b2fixture.getNext();
            return f;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

