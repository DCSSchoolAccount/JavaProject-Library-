/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Body;
import city.cs.engine.World;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;

public abstract class WorldView
extends JPanel {
    private static final BasicStroke GRID_STROKE_1 = new BasicStroke(0.3f);
    private static final BasicStroke GRID_STROKE_5 = new BasicStroke(0.6f);
    private static final BasicStroke GRID_STROKE_10 = new BasicStroke(0.9f);
    private static final float DEFAULT_ZOOM = 20.0f;
    private static final Vec2 ORIGIN = new Vec2();
    World world;
    AffineTransform cameraTransform;
    AffineTransform inverseCameraTransform;
    private float zoom;
    private Vec2 centre;
    private float gridResolution;
    private final ChangeListener worldListener;

    public WorldView(World w, int width, int height) {
        this.world = w;
        this.setPreferredSize(new Dimension(width, height));
        this.setView(ORIGIN, 20.0f);
        this.gridResolution = 0.0f;
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized(ComponentEvent e) {
                WorldView.this.updateViewTransform();
            }
        });
        this.worldListener = new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                WorldView.this.repaint();
            }
        };
        this.world.addChangeListener(this.worldListener);
        this.setDoubleBuffered(true);
    }

    public void setGridResolution(float r) {
        this.gridResolution = r;
    }

    public float getGridResolution() {
        return this.gridResolution;
    }

    public void setView(Vec2 centre, float zoom) {
        this.centre = centre.clone();
        this.zoom = zoom;
        this.updateViewTransform();
    }

    public void setCentre(Vec2 centre) {
        this.centre = centre.clone();
        this.updateViewTransform();
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.updateViewTransform();
    }

    private void updateViewTransform() {
        int width = this.getWidth();
        int height = this.getHeight();
        this.cameraTransform = AffineTransform.getTranslateInstance(-this.centre.x, -this.centre.y);
        this.inverseCameraTransform = AffineTransform.getTranslateInstance(this.centre.x, this.centre.y);
        this.cameraTransform.preConcatenate(AffineTransform.getScaleInstance(this.zoom, -this.zoom));
        this.inverseCameraTransform.concatenate(AffineTransform.getScaleInstance(1.0f / this.zoom, -1.0f / this.zoom));
        this.cameraTransform.preConcatenate(AffineTransform.getTranslateInstance((double)width / 2.0, (double)height / 2.0));
        this.inverseCameraTransform.concatenate(AffineTransform.getTranslateInstance((double)(-width) / 2.0, (double)(-height) / 2.0));
    }

    public Vec2 getCentre() {
        return this.centre.clone();
    }

    public float getZoom() {
        return this.zoom;
    }

    private AABB getViewport() {
        Vec2 extent = new Vec2(this.getWidth(), this.getHeight());
        extent.mulLocal(0.5f / this.zoom);
        return new AABB(this.centre.sub(extent), this.centre.add(extent));
    }

    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        if (this.world != null) {
            this.world.removeChangeListener(this.worldListener);
        }
        this.world = world;
        world.addChangeListener(this.worldListener);
        this.setView(ORIGIN, 20.0f);
    }

    private void setRenderingPreferences(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        this.setRenderingPreferences(g2);
        this.paintBackground(g2);
        for (Body body : this.world.getBodies()) {
            this.paintBody(g2, body);
        }
        if (this.gridResolution > 0.0f) {
            this.drawGrid(g2);
        }
        this.paintForeground(g2);
    }

    abstract void paintBody(Graphics2D var1, Body var2);

    private void drawGrid(Graphics2D g) {
        AABB viewp = this.getViewport();
        float xMin = viewp.lowerBound.x;
        float yMin = viewp.lowerBound.y;
        float xMax = viewp.upperBound.x;
        float yMax = viewp.upperBound.y;
        int xMinGrid = (int)Math.ceil(xMin / this.gridResolution);
        int xMaxGrid = (int)Math.floor(xMax / this.gridResolution);
        for (int x = xMinGrid; x <= xMaxGrid; ++x) {
            this.gridLineStyle(g, x);
            float xVal = (float)x * this.gridResolution;
            this.drawLine(g, new Vec2(xVal, yMin), new Vec2(xVal, yMax));
        }
        int yMinGrid = (int)Math.ceil(yMin / this.gridResolution);
        int yMaxGrid = (int)Math.floor(yMax / this.gridResolution);
        for (int y = yMinGrid; y <= yMaxGrid; ++y) {
            this.gridLineStyle(g, y);
            float yVal = (float)y * this.gridResolution;
            this.drawLine(g, new Vec2(xMin, yVal), new Vec2(xMax, yVal));
        }
    }

    private void gridLineStyle(Graphics2D g, int x) {
        g.setStroke(x % 10 == 0 ? GRID_STROKE_10 : (x % 5 == 0 ? GRID_STROKE_5 : GRID_STROKE_1));
        g.setColor(x == 0 ? Color.RED : Color.BLUE);
    }

    private void drawLine(Graphics2D g, Vec2 v1, Vec2 v2) {
        Point2D.Float p1 = this.worldToView(v1);
        Point2D.Float p2 = this.worldToView(v2);
        Line2D.Float line = new Line2D.Float(p1, p2);
        g.draw(line);
    }

    private void drawPoint(Graphics2D g, Vec2 v) {
        Point2D.Float p = this.worldToView(v);
        float r = 2.0f;
        g.fill(new Ellipse2D.Float(p.x - r, p.y - r, 2.0f * r, 2.0f * r));
    }

    protected void paintBackground(Graphics2D g) {
    }

    protected void paintForeground(Graphics2D g) {
    }

    Shape transformToView(Shape s) {
        GeneralPath cameraReady = new GeneralPath(s);
        cameraReady.transform(this.cameraTransform);
        return cameraReady;
    }

    public Point2D.Float worldToView(Vec2 v) {
        Point2D.Float p = new Point2D.Float(v.x, v.y);
        this.cameraTransform.transform(p, p);
        return p;
    }

    public Vec2 viewToWorld(Point2D p) {
        Point2D.Float pw = new Point2D.Float();
        this.inverseCameraTransform.transform(p, pw);
        return new Vec2(pw.x, pw.y);
    }
}

