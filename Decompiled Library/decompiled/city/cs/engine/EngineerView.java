/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.CircleShape;
import city.cs.engine.CollisionEvent;
import city.cs.engine.DynamicBody;
import city.cs.engine.Fixture;
import city.cs.engine.Shape;
import city.cs.engine.SimulationSettings;
import city.cs.engine.SolidFixture;
import city.cs.engine.StaticBody;
import city.cs.engine.World;
import city.cs.engine.WorldView;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

public class EngineerView
extends WorldView {
    private boolean drawStats = false;
    private Color textColor = Color.WHITE;
    private Vec2 dragPoint;
    private Point shootPoint;
    private Point lastMouse;
    private org.jbox2d.dynamics.Body groundBody;
    private MouseJoint mouseJoint;
    private static final ColorScheme STATIC_SCHEME = new ColorScheme(127, 229, 127);
    private static final ColorScheme DYNAMIC_SCHEME = new ColorScheme(229, 229, 229);
    private static final ColorScheme ASLEEP_SCHEME = new ColorScheme(127, 127, 229);
    private static final Color CONTACT_PERSIST_COLOR = new Color(255, 150, 150);
    private static final Color CONTACT_ADD_COLOR = new Color(255, 0, 0);
    private static final Color CONTACT_REMOVE_COLOR = new Color(0, 155, 155);
    private static final Color NORMAL_COLOR = new Color(102, 229, 102);
    private static final Color FORCE_COLOR = new Color(229, 229, 76);
    private static final Color VELOCITY_COLOR = new Color(76, 229, 229);
    private static final float ZOOM_FACTOR = 1.2f;
    private static final float textLineHeight = 16.0f;
    private static final float textMargin = 10.0f;
    private static final Shape bulletShape = new CircleShape(0.2f);

    private static Color fillColor(Color c) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 63);
    }

    public EngineerView(World w, int width, int height) {
        super(w, width, height);
        this.setBackground(Color.BLACK);
        this.initControls();
        this.addMouseListener(new MouseHandler());
        this.addMouseMotionListener(new MouseMotionHandler());
        this.addMouseWheelListener(new MouseWheelHandler());
    }

    void initControls() {
        this.dragPoint = null;
        this.shootPoint = null;
        this.groundBody = this.world.getBox2DWorld().createBody(new BodyDef());
    }

    private void setDrawStats(boolean b) {
        this.drawStats = b;
    }

    private void setTextColor(Color c) {
        this.textColor = c;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        if (this.drawStats) {
            this.drawStats(g2);
        }
        if (this.shootPoint != null) {
            Line2D.Float line = new Line2D.Float(this.lastMouse, this.shootPoint);
            g2.setColor(Color.GRAY);
            g2.draw(line);
        }
        if (!this.world.isRunning()) {
            for (CollisionEvent ce : this.world.getCollisionEvents()) {
                Vec2 p = ce.getPosition();
                Point2D.Float c = this.worldToView(p);
                g2.setColor(CONTACT_ADD_COLOR);
                float r = 2.5f;
                g2.fill(new Ellipse2D.Float(c.x - r, c.y - r, 2.0f * r, 2.0f * r));
                g2.setColor(NORMAL_COLOR);
                this.drawLine(g2, p.add(ce.getNormal()), p.sub(ce.getNormal()));
            }
        }
    }

    private void drawLine(Graphics2D g, Vec2 p1, Vec2 p2) {
        g.draw(new Line2D.Float(this.worldToView(p1), this.worldToView(p2)));
    }

    @Override
    void paintBody(Graphics2D g, Body body) {
        ColorScheme scheme = body instanceof StaticBody ? STATIC_SCHEME : (body.getBox2DBody().isAwake() ? DYNAMIC_SCHEME : ASLEEP_SCHEME);
        for (Fixture f : body.getFixtures()) {
            java.awt.Shape shape = this.transformToView(f.awtShape());
            g.setColor(scheme.fill);
            g.fill(shape);
            g.setColor(scheme.stroke);
            g.setStroke(f.getOutlineStyle());
            g.draw(shape);
            Shape s = f.getShape();
            if (!(s instanceof CircleShape)) continue;
            g.draw(this.transformToView(((CircleShape)s).awtRadius(body.getBox2DBody().getTransform())));
        }
    }

    private void drawStats(Graphics2D g) {
        float textLine = 32.0f;
        g.setColor(this.textColor);
        SimulationSettings settings = this.world.getSimulationSettings();
        float averagedFPS = settings.getAveragedFPS();
        float totalFPS = settings.getTotalFPS();
        g.drawString("Target FPS: " + settings.getFrameRate(), 10.0f, textLine);
        g.drawString("Average FPS (" + settings.getFpsAverageCount() + " frames): " + averagedFPS, 10.0f, textLine += 16.0f);
        g.drawString("Average FPS (entire test): " + totalFPS, 10.0f, textLine += 16.0f);
        textLine += 16.0f;
    }

    private class MouseMotionHandler
    implements MouseMotionListener {
        private MouseMotionHandler() {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            EngineerView.this.lastMouse = e.getPoint();
            if (EngineerView.this.dragPoint != null) {
                EngineerView.this.setCentre(EngineerView.this.getCentre().sub(EngineerView.this.viewToWorld(EngineerView.this.lastMouse)).add(EngineerView.this.dragPoint));
            }
            if (EngineerView.this.mouseJoint != null) {
                EngineerView.this.mouseJoint.setTarget(EngineerView.this.viewToWorld(e.getPoint()));
            }
        }
    }

    private class MouseHandler
    extends MouseAdapter {
        private MouseHandler() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == 1) {
                if (e.isShiftDown()) {
                    EngineerView.this.shootPoint = e.getPoint();
                    EngineerView.this.lastMouse = EngineerView.this.shootPoint;
                } else {
                    this.dragBody(EngineerView.this.viewToWorld(e.getPoint()));
                }
            } else if (e.getButton() == 3) {
                EngineerView.this.dragPoint = EngineerView.this.viewToWorld(e.getPoint());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == 3) {
                EngineerView.this.dragPoint = null;
            }
            if (e.getButton() == 1) {
                if (EngineerView.this.shootPoint != null) {
                    Vec2 rel_pos = EngineerView.this.viewToWorld(EngineerView.this.shootPoint);
                    Vec2 pos = EngineerView.this.viewToWorld(e.getPoint());
                    DynamicBody bullet = new DynamicBody(EngineerView.this.world);
                    new SolidFixture((Body)bullet, bulletShape, 10.0f);
                    bullet.setBullet(true);
                    bullet.setPosition(rel_pos);
                    bullet.setLinearVelocity(rel_pos.sub(pos).mul(5.0f));
                    bullet.setFillColor(Color.RED);
                    EngineerView.this.shootPoint = null;
                }
                this.clearMouseJoint();
            }
        }

        private void dragBody(Vec2 p) {
            this.clearMouseJoint();
            Vec2 d = new Vec2(0.001f, 0.001f);
            AABB queryAABB = new AABB(p.sub(d), p.add(d));
            DragQueryCallback callback = new DragQueryCallback(p);
            EngineerView.this.world.getBox2DWorld().queryAABB(callback, queryAABB);
        }

        private void clearMouseJoint() {
            if (EngineerView.this.mouseJoint != null) {
                EngineerView.this.world.getBox2DWorld().destroyJoint(EngineerView.this.mouseJoint);
                EngineerView.this.mouseJoint = null;
            }
        }

        private class DragQueryCallback
        implements QueryCallback {
            private final Vec2 point;

            public DragQueryCallback(Vec2 point) {
                this.point = point;
            }

            @Override
            public boolean reportFixture(org.jbox2d.dynamics.Fixture fixture) {
                org.jbox2d.dynamics.Body body = fixture.getBody();
                if (body.getType() == BodyType.DYNAMIC && fixture.testPoint(this.point)) {
                    org.jbox2d.dynamics.World b2world = EngineerView.this.world.getBox2DWorld();
                    MouseJointDef def = new MouseJointDef();
                    def.bodyA = EngineerView.this.groundBody;
                    def.bodyB = body;
                    def.target.set(this.point);
                    def.maxForce = 1000.0f * body.getMass();
                    EngineerView.this.mouseJoint = (MouseJoint)b2world.createJoint(def);
                    body.setAwake(true);
                    return false;
                }
                return true;
            }
        }
    }

    private class MouseWheelHandler
    implements MouseWheelListener {
        private MouseWheelHandler() {
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            float zoom = EngineerView.this.getZoom();
            int diff = e.getWheelRotation();
            zoom = (float)((double)zoom * Math.pow(1.2f, diff));
            EngineerView.this.setZoom(zoom);
        }
    }

    private static class ColorScheme {
        public Color fill;
        public Color stroke;

        ColorScheme(int r, int g, int b) {
            this.stroke = new Color(r, g, b);
            this.fill = new Color(r, g, b, 63);
        }
    }
}

