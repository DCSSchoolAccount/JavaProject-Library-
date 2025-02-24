/*
 * Decompiled with CFR 0.152.
 */
package city.cs.engine;

import city.cs.engine.Shape;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

public class PolygonShape
extends Shape {
    private org.jbox2d.collision.shapes.PolygonShape b2polygon;

    PolygonShape(org.jbox2d.collision.shapes.PolygonShape shape) {
        super(shape);
        this.b2polygon = shape;
    }

    public PolygonShape(List<Vec2> vertices) {
        this(PolygonShape.polygon(vertices));
    }

    public PolygonShape(float ... coords) {
        this(PolygonShape.verticesFromCoords(coords));
    }

    private static org.jbox2d.collision.shapes.PolygonShape polygon(List<Vec2> vertices) {
        org.jbox2d.collision.shapes.PolygonShape polyShape = new org.jbox2d.collision.shapes.PolygonShape();
        Vec2[] verts = PolygonShape.makeCanonical(vertices);
        polyShape.set(verts, verts.length);
        return polyShape;
    }

    private static ArrayList<Vec2> verticesFromCoords(float[] coords) {
        if (coords.length == 0) {
            throw new IllegalArgumentException("Coords list must be non-empty.");
        }
        if (coords.length % 2 != 0) {
            throw new IllegalArgumentException("Coords list must be of even length.");
        }
        ArrayList<Vec2> vertices = new ArrayList<Vec2>();
        for (int i = 0; i < coords.length; i += 2) {
            vertices.add(new Vec2(coords[i], coords[i + 1]));
        }
        return vertices;
    }

    private static boolean isConvex(Vec2[] verts, int i) {
        Vec2 v2;
        Vec2 v1 = PolygonShape.getVert(verts, i).sub(PolygonShape.getVert(verts, i - 1));
        return Vec2.cross(v1, v2 = PolygonShape.getVert(verts, i + 1).sub(PolygonShape.getVert(verts, i))) >= 0.0f;
    }

    private static Vec2 getVert(Vec2[] verts, int i) {
        int j = i % verts.length;
        if (j < 0) {
            j += verts.length;
        }
        return verts[j];
    }

    private static Vec2[] makeCanonical(List<Vec2> vertices) throws IllegalArgumentException {
        ArrayList<Vec2> _vertices = new ArrayList<Vec2>();
        for (Vec2 v : vertices) {
            _vertices.add(v);
        }
        int i = 0;
        while (i < _vertices.size() && _vertices.size() > 1) {
            int nextIndex = (i + 1) % _vertices.size();
            Vec2 p = (Vec2)_vertices.get(i);
            Vec2 q = (Vec2)_vertices.get(nextIndex);
            if (p.x == q.x && p.y == q.y) {
                _vertices.remove(i);
                continue;
            }
            ++i;
        }
        Vec2[] verts = _vertices.toArray(new Vec2[_vertices.size()]);
        PolygonShape.makeCCW(verts);
        for (int i2 = 0; i2 < verts.length; ++i2) {
            if (PolygonShape.isConvex(verts, i2)) continue;
            throw new IllegalArgumentException("Polygon is not convex.");
        }
        return verts;
    }

    private static boolean makeCCW(Vec2[] verts) {
        if (!PolygonShape.isCCW(verts)) {
            for (int i = 0; i < verts.length / 2; ++i) {
                int j = verts.length - i - 1;
                Vec2 v = verts[i];
                verts[i] = verts[j];
                verts[j] = v;
            }
            return true;
        }
        return false;
    }

    private static boolean isCCW(Vec2[] verts) {
        float c = 0.0f;
        for (int i = 0; i < verts.length; ++i) {
            Vec2 p1 = verts[i];
            Vec2 p2 = verts[(i + 1) % verts.length];
            c += p1.x * p2.y - p2.x * p1.y;
        }
        return c > 0.0f;
    }

    @Override
    Vec2 extremalPoint(Transform xf, Vec2 direction) {
        Vec2 dLocal = new Vec2();
        Rot.mulTrans(xf.q, direction, dLocal);
        return Transform.mul(xf, this.extremalPoint(dLocal));
    }

    private Vec2 extremalPoint(Vec2 dLocal) {
        int n = this.b2polygon.getVertexCount();
        Vec2[] vertices = this.b2polygon.getVertices();
        int bestIndex = 0;
        boolean edge = false;
        float bestValue = Vec2.dot(vertices[0], dLocal);
        boolean firstIsBest = true;
        for (int i = 1; i < n; ++i) {
            float value = Vec2.dot(vertices[i], dLocal);
            if (value > bestValue) {
                edge = false;
                firstIsBest = false;
                bestIndex = i;
                bestValue = value;
                continue;
            }
            if (value != bestValue) continue;
            edge = true;
            bestIndex = i;
        }
        Vec2 support = vertices[bestIndex];
        if (edge) {
            Vec2 equalBest = firstIsBest ? vertices[0] : vertices[bestIndex - 1];
            support = support.add(equalBest.sub(support).mul(0.5f));
        }
        return support;
    }

    @Override
    java.awt.Shape awtShape(Transform xf) {
        int vertexCount = this.b2polygon.getVertexCount();
        Vec2[] vertices = this.b2polygon.getVertices();
        GeneralPath p = new GeneralPath();
        for (int i = 0; i < vertexCount; ++i) {
            Vec2 v = Transform.mul(xf, vertices[i]);
            if (i == 0) {
                p.moveTo(v.x, v.y);
                continue;
            }
            p.lineTo(v.x, v.y);
        }
        p.closePath();
        return p;
    }

    @Override
    Transform getTop(Transform xf, float x) {
        int n = this.b2polygon.getVertexCount();
        Vec2[] vertices = this.b2polygon.getVertices();
        Vec2 last_p = Transform.mul(xf, vertices[0]);
        for (int i = 1; i < n + 1; ++i) {
            Vec2 p = Transform.mul(xf, vertices[i % n]);
            if (p.x <= x && x < last_p.x) {
                float xdiff = p.x - last_p.x;
                float ydiff = p.y - last_p.y;
                float y = last_p.y + (x - last_p.x) * ydiff / xdiff;
                float theta = -((float)Math.atan2(ydiff, -xdiff));
                return new Transform(new Vec2(x, y), new Rot(theta));
            }
            last_p = p;
        }
        return null;
    }

    @Override
    Transform getBottom(Transform xf, float x) {
        int n = this.b2polygon.getVertexCount();
        Vec2[] vertices = this.b2polygon.getVertices();
        Vec2 last_p = Transform.mul(xf, vertices[0]);
        for (int i = 1; i < n + 1; ++i) {
            Vec2 p = Transform.mul(xf, vertices[i % n]);
            if (last_p.x <= x && x < p.x) {
                float xdiff = last_p.x - p.x;
                float ydiff = last_p.y - p.y;
                float y = p.y + (x - p.x) * ydiff / xdiff;
                float theta = -((float)Math.atan2(ydiff, -xdiff));
                return new Transform(new Vec2(x, y), new Rot(theta));
            }
            last_p = p;
        }
        return null;
    }
}

