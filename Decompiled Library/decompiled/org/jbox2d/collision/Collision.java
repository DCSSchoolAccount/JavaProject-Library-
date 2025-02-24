/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.collision;

import org.jbox2d.collision.ContactID;
import org.jbox2d.collision.Distance;
import org.jbox2d.collision.DistanceInput;
import org.jbox2d.collision.DistanceOutput;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.ManifoldPoint;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.pooling.IWorldPool;

public class Collision {
    public static final int NULL_FEATURE = Integer.MAX_VALUE;
    private final IWorldPool pool;
    private final DistanceInput input = new DistanceInput();
    private final Distance.SimplexCache cache = new Distance.SimplexCache();
    private final DistanceOutput output = new DistanceOutput();
    private static Vec2 d = new Vec2();
    private final Vec2 temp = new Vec2();
    private final EdgeResults results1 = new EdgeResults();
    private final EdgeResults results2 = new EdgeResults();
    private final ClipVertex[] incidentEdge = new ClipVertex[2];
    private final Vec2 localTangent = new Vec2();
    private final Vec2 localNormal = new Vec2();
    private final Vec2 planePoint = new Vec2();
    private final Vec2 tangent = new Vec2();
    private final Vec2 v11 = new Vec2();
    private final Vec2 v12 = new Vec2();
    private final ClipVertex[] clipPoints1 = new ClipVertex[2];
    private final ClipVertex[] clipPoints2 = new ClipVertex[2];
    private final Vec2 Q = new Vec2();
    private final Vec2 e = new Vec2();
    private final ContactID cf = new ContactID();
    private final Vec2 e1 = new Vec2();
    private final Vec2 P = new Vec2();
    private final Vec2 n = new Vec2();
    private final EPCollider collider = new EPCollider();

    public Collision(IWorldPool argPool) {
        this.incidentEdge[0] = new ClipVertex();
        this.incidentEdge[1] = new ClipVertex();
        this.clipPoints1[0] = new ClipVertex();
        this.clipPoints1[1] = new ClipVertex();
        this.clipPoints2[0] = new ClipVertex();
        this.clipPoints2[1] = new ClipVertex();
        this.pool = argPool;
    }

    public final boolean testOverlap(Shape shapeA, int indexA, Shape shapeB, int indexB, Transform xfA, Transform xfB) {
        this.input.proxyA.set(shapeA, indexA);
        this.input.proxyB.set(shapeB, indexB);
        this.input.transformA.set(xfA);
        this.input.transformB.set(xfB);
        this.input.useRadii = true;
        this.cache.count = 0;
        this.pool.getDistance().distance(this.output, this.cache, this.input);
        return this.output.distance < 1.1920929E-6f;
    }

    public static final void getPointStates(PointState[] state1, PointState[] state2, Manifold manifold1, Manifold manifold2) {
        int j;
        ContactID id;
        int i;
        for (i = 0; i < 2; ++i) {
            state1[i] = PointState.NULL_STATE;
            state2[i] = PointState.NULL_STATE;
        }
        block1: for (i = 0; i < manifold1.pointCount; ++i) {
            id = manifold1.points[i].id;
            state1[i] = PointState.REMOVE_STATE;
            for (j = 0; j < manifold2.pointCount; ++j) {
                if (!manifold2.points[j].id.isEqual(id)) continue;
                state1[i] = PointState.PERSIST_STATE;
                continue block1;
            }
        }
        block3: for (i = 0; i < manifold2.pointCount; ++i) {
            id = manifold2.points[i].id;
            state2[i] = PointState.ADD_STATE;
            for (j = 0; j < manifold1.pointCount; ++j) {
                if (!manifold1.points[j].id.isEqual(id)) continue;
                state2[i] = PointState.PERSIST_STATE;
                continue block3;
            }
        }
    }

    public static final int clipSegmentToLine(ClipVertex[] vOut, ClipVertex[] vIn, Vec2 normal, float offset, int vertexIndexA) {
        int numOut = 0;
        ClipVertex vIn0 = vIn[0];
        ClipVertex vIn1 = vIn[1];
        Vec2 vIn0v = vIn0.v;
        Vec2 vIn1v = vIn1.v;
        float distance0 = Vec2.dot(normal, vIn0v) - offset;
        float distance1 = Vec2.dot(normal, vIn1v) - offset;
        if (distance0 <= 0.0f) {
            vOut[numOut++].set(vIn0);
        }
        if (distance1 <= 0.0f) {
            vOut[numOut++].set(vIn1);
        }
        if (distance0 * distance1 < 0.0f) {
            float interp = distance0 / (distance0 - distance1);
            ClipVertex vOutNO = vOut[numOut];
            vOutNO.v.x = vIn0v.x + interp * (vIn1v.x - vIn0v.x);
            vOutNO.v.y = vIn0v.y + interp * (vIn1v.y - vIn0v.y);
            vOutNO.id.indexA = (byte)vertexIndexA;
            vOutNO.id.indexB = vIn0.id.indexB;
            vOutNO.id.typeA = (byte)ContactID.Type.VERTEX.ordinal();
            vOutNO.id.typeB = (byte)ContactID.Type.FACE.ordinal();
            ++numOut;
        }
        return numOut;
    }

    public final void collideCircles(Manifold manifold, CircleShape circle1, Transform xfA, CircleShape circle2, Transform xfB) {
        manifold.pointCount = 0;
        Vec2 circle1p = circle1.m_p;
        Vec2 circle2p = circle2.m_p;
        float pBx = xfB.q.c * circle2p.x - xfB.q.s * circle2p.y + xfB.p.x;
        float pAx = xfA.q.c * circle1p.x - xfA.q.s * circle1p.y + xfA.p.x;
        float dx = pBx - pAx;
        float pBy = xfB.q.s * circle2p.x + xfB.q.c * circle2p.y + xfB.p.y;
        float pAy = xfA.q.s * circle1p.x + xfA.q.c * circle1p.y + xfA.p.y;
        float dy = pBy - pAy;
        float distSqr = dx * dx + dy * dy;
        float radius = circle1.m_radius + circle2.m_radius;
        if (distSqr > radius * radius) {
            return;
        }
        manifold.type = Manifold.ManifoldType.CIRCLES;
        manifold.localPoint.set(circle1p);
        manifold.localNormal.setZero();
        manifold.pointCount = 1;
        manifold.points[0].localPoint.set(circle2p);
        manifold.points[0].id.zero();
    }

    public final void collidePolygonAndCircle(Manifold manifold, PolygonShape polygon, Transform xfA, CircleShape circle, Transform xfB) {
        manifold.pointCount = 0;
        Vec2 circlep = circle.m_p;
        Rot xfBq = xfB.q;
        Rot xfAq = xfA.q;
        float cx = xfBq.c * circlep.x - xfBq.s * circlep.y + xfB.p.x;
        float cy = xfBq.s * circlep.x + xfBq.c * circlep.y + xfB.p.y;
        float px = cx - xfA.p.x;
        float py = cy - xfA.p.y;
        float cLocalx = xfAq.c * px + xfAq.s * py;
        float cLocaly = -xfAq.s * px + xfAq.c * py;
        int normalIndex = 0;
        float separation = -3.4028235E38f;
        float radius = polygon.m_radius + circle.m_radius;
        int vertexCount = polygon.m_count;
        Vec2[] vertices = polygon.m_vertices;
        Vec2[] normals = polygon.m_normals;
        for (int i = 0; i < vertexCount; ++i) {
            Vec2 vertex = vertices[i];
            float tempx = cLocalx - vertex.x;
            float tempy = cLocaly - vertex.y;
            float s = normals[i].x * tempx + normals[i].y * tempy;
            if (s > radius) {
                return;
            }
            if (!(s > separation)) continue;
            separation = s;
            normalIndex = i;
        }
        int vertIndex1 = normalIndex;
        int vertIndex2 = vertIndex1 + 1 < vertexCount ? vertIndex1 + 1 : 0;
        Vec2 v1 = vertices[vertIndex1];
        Vec2 v2 = vertices[vertIndex2];
        if (separation < 1.1920929E-7f) {
            manifold.pointCount = 1;
            manifold.type = Manifold.ManifoldType.FACE_A;
            Vec2 normal = normals[normalIndex];
            manifold.localNormal.x = normal.x;
            manifold.localNormal.y = normal.y;
            manifold.localPoint.x = (v1.x + v2.x) * 0.5f;
            manifold.localPoint.y = (v1.y + v2.y) * 0.5f;
            ManifoldPoint mpoint = manifold.points[0];
            mpoint.localPoint.x = circlep.x;
            mpoint.localPoint.y = circlep.y;
            mpoint.id.zero();
            return;
        }
        float tempX = cLocalx - v1.x;
        float tempY = cLocaly - v1.y;
        float temp2X = v2.x - v1.x;
        float temp2Y = v2.y - v1.y;
        float u1 = tempX * temp2X + tempY * temp2Y;
        float temp3X = cLocalx - v2.x;
        float temp3Y = cLocaly - v2.y;
        float temp4X = v1.x - v2.x;
        float temp4Y = v1.y - v2.y;
        float u2 = temp3X * temp4X + temp3Y * temp4Y;
        if (u1 <= 0.0f) {
            float dx = cLocalx - v1.x;
            float dy = cLocaly - v1.y;
            if (dx * dx + dy * dy > radius * radius) {
                return;
            }
            manifold.pointCount = 1;
            manifold.type = Manifold.ManifoldType.FACE_A;
            manifold.localNormal.x = cLocalx - v1.x;
            manifold.localNormal.y = cLocaly - v1.y;
            manifold.localNormal.normalize();
            manifold.localPoint.set(v1);
            manifold.points[0].localPoint.set(circlep);
            manifold.points[0].id.zero();
        } else if (u2 <= 0.0f) {
            float dx = cLocalx - v2.x;
            float dy = cLocaly - v2.y;
            if (dx * dx + dy * dy > radius * radius) {
                return;
            }
            manifold.pointCount = 1;
            manifold.type = Manifold.ManifoldType.FACE_A;
            manifold.localNormal.x = cLocalx - v2.x;
            manifold.localNormal.y = cLocaly - v2.y;
            manifold.localNormal.normalize();
            manifold.localPoint.set(v2);
            manifold.points[0].localPoint.set(circlep);
            manifold.points[0].id.zero();
        } else {
            float fcx = (v1.x + v2.x) * 0.5f;
            float fcy = (v1.y + v2.y) * 0.5f;
            float tx = cLocalx - fcx;
            float ty = cLocaly - fcy;
            Vec2 normal = normals[vertIndex1];
            separation = tx * normal.x + ty * normal.y;
            if (separation > radius) {
                return;
            }
            manifold.pointCount = 1;
            manifold.type = Manifold.ManifoldType.FACE_A;
            manifold.localNormal.set(normals[vertIndex1]);
            manifold.localPoint.x = fcx;
            manifold.localPoint.y = fcy;
            manifold.points[0].localPoint.set(circlep);
            manifold.points[0].id.zero();
        }
    }

    public final float edgeSeparation(PolygonShape poly1, Transform xf1, int edge1, PolygonShape poly2, Transform xf2) {
        int count1 = poly1.m_count;
        Vec2[] vertices1 = poly1.m_vertices;
        Vec2[] normals1 = poly1.m_normals;
        int count2 = poly2.m_count;
        Vec2[] vertices2 = poly2.m_vertices;
        assert (0 <= edge1 && edge1 < count1);
        Rot xf1q = xf1.q;
        Rot xf2q = xf2.q;
        Rot q = xf1q;
        Vec2 v = normals1[edge1];
        float normal1Worldx = q.c * v.x - q.s * v.y;
        float normal1Worldy = q.s * v.x + q.c * v.y;
        Rot q1 = xf2q;
        float normal1x = q1.c * normal1Worldx + q1.s * normal1Worldy;
        float normal1y = -q1.s * normal1Worldx + q1.c * normal1Worldy;
        int index = 0;
        float minDot = Float.MAX_VALUE;
        for (int i = 0; i < count2; ++i) {
            Vec2 a = vertices2[i];
            float dot = a.x * normal1x + a.y * normal1y;
            if (!(dot < minDot)) continue;
            minDot = dot;
            index = i;
        }
        Vec2 v3 = vertices1[edge1];
        float v1x = xf1q.c * v3.x - xf1q.s * v3.y + xf1.p.x;
        float v1y = xf1q.s * v3.x + xf1q.c * v3.y + xf1.p.y;
        Vec2 v4 = vertices2[index];
        float v2x = xf2q.c * v4.x - xf2q.s * v4.y + xf2.p.x - v1x;
        float v2y = xf2q.s * v4.x + xf2q.c * v4.y + xf2.p.y - v1y;
        float separation = v2x * normal1Worldx + v2y * normal1Worldy;
        return separation;
    }

    public final void findMaxSeparation(EdgeResults results, PolygonShape poly1, Transform xf1, PolygonShape poly2, Transform xf2) {
        float bestSeparation;
        int bestEdge;
        int increment;
        int count1 = poly1.m_count;
        Vec2[] normals1 = poly1.m_normals;
        Vec2 poly1centroid = poly1.m_centroid;
        Vec2 poly2centroid = poly2.m_centroid;
        Rot xf2q = xf2.q;
        Rot xf1q = xf1.q;
        float dx = xf2q.c * poly2centroid.x - xf2q.s * poly2centroid.y + xf2.p.x;
        float dy = xf2q.s * poly2centroid.x + xf2q.c * poly2centroid.y + xf2.p.y;
        float dLocal1x = xf1q.c * (dx -= xf1q.c * poly1centroid.x - xf1q.s * poly1centroid.y + xf1.p.x) + xf1q.s * (dy -= xf1q.s * poly1centroid.x + xf1q.c * poly1centroid.y + xf1.p.y);
        float dLocal1y = -xf1q.s * dx + xf1q.c * dy;
        int edge = 0;
        float maxDot = -3.4028235E38f;
        for (int i = 0; i < count1; ++i) {
            Vec2 normal = normals1[i];
            float dot = normal.x * dLocal1x + normal.y * dLocal1y;
            if (!(dot > maxDot)) continue;
            maxDot = dot;
            edge = i;
        }
        float s = this.edgeSeparation(poly1, xf1, edge, poly2, xf2);
        int prevEdge = edge - 1 >= 0 ? edge - 1 : count1 - 1;
        float sPrev = this.edgeSeparation(poly1, xf1, prevEdge, poly2, xf2);
        int nextEdge = edge + 1 < count1 ? edge + 1 : 0;
        float sNext = this.edgeSeparation(poly1, xf1, nextEdge, poly2, xf2);
        if (sPrev > s && sPrev > sNext) {
            increment = -1;
            bestEdge = prevEdge;
            bestSeparation = sPrev;
        } else if (sNext > s) {
            increment = 1;
            bestEdge = nextEdge;
            bestSeparation = sNext;
        } else {
            results.edgeIndex = edge;
            results.separation = s;
            return;
        }
        while ((s = this.edgeSeparation(poly1, xf1, edge = increment == -1 ? (bestEdge - 1 >= 0 ? bestEdge - 1 : count1 - 1) : (bestEdge + 1 < count1 ? bestEdge + 1 : 0), poly2, xf2)) > bestSeparation) {
            bestEdge = edge;
            bestSeparation = s;
        }
        results.edgeIndex = bestEdge;
        results.separation = bestSeparation;
    }

    public final void findIncidentEdge(ClipVertex[] c, PolygonShape poly1, Transform xf1, int edge1, PolygonShape poly2, Transform xf2) {
        int count1 = poly1.m_count;
        Vec2[] normals1 = poly1.m_normals;
        int count2 = poly2.m_count;
        Vec2[] vertices2 = poly2.m_vertices;
        Vec2[] normals2 = poly2.m_normals;
        assert (0 <= edge1 && edge1 < count1);
        ClipVertex c0 = c[0];
        ClipVertex c1 = c[1];
        Rot xf1q = xf1.q;
        Rot xf2q = xf2.q;
        Vec2 v = normals1[edge1];
        float tempx = xf1q.c * v.x - xf1q.s * v.y;
        float tempy = xf1q.s * v.x + xf1q.c * v.y;
        float normal1x = xf2q.c * tempx + xf2q.s * tempy;
        float normal1y = -xf2q.s * tempx + xf2q.c * tempy;
        int index = 0;
        float minDot = Float.MAX_VALUE;
        for (int i = 0; i < count2; ++i) {
            Vec2 b = normals2[i];
            float dot = normal1x * b.x + normal1y * b.y;
            if (!(dot < minDot)) continue;
            minDot = dot;
            index = i;
        }
        int i1 = index;
        int i2 = i1 + 1 < count2 ? i1 + 1 : 0;
        Vec2 v1 = vertices2[i1];
        Vec2 out = c0.v;
        out.x = xf2q.c * v1.x - xf2q.s * v1.y + xf2.p.x;
        out.y = xf2q.s * v1.x + xf2q.c * v1.y + xf2.p.y;
        c0.id.indexA = (byte)edge1;
        c0.id.indexB = (byte)i1;
        c0.id.typeA = (byte)ContactID.Type.FACE.ordinal();
        c0.id.typeB = (byte)ContactID.Type.VERTEX.ordinal();
        Vec2 v2 = vertices2[i2];
        Vec2 out1 = c1.v;
        out1.x = xf2q.c * v2.x - xf2q.s * v2.y + xf2.p.x;
        out1.y = xf2q.s * v2.x + xf2q.c * v2.y + xf2.p.y;
        c1.id.indexA = (byte)edge1;
        c1.id.indexB = (byte)i2;
        c1.id.typeA = (byte)ContactID.Type.FACE.ordinal();
        c1.id.typeB = (byte)ContactID.Type.VERTEX.ordinal();
    }

    public final void collidePolygons(Manifold manifold, PolygonShape polyA, Transform xfA, PolygonShape polyB, Transform xfB) {
        boolean flip;
        int edge1;
        Transform xf2;
        Transform xf1;
        PolygonShape poly2;
        PolygonShape poly1;
        manifold.pointCount = 0;
        float totalRadius = polyA.m_radius + polyB.m_radius;
        this.findMaxSeparation(this.results1, polyA, xfA, polyB, xfB);
        if (this.results1.separation > totalRadius) {
            return;
        }
        this.findMaxSeparation(this.results2, polyB, xfB, polyA, xfA);
        if (this.results2.separation > totalRadius) {
            return;
        }
        float k_relativeTol = 0.98f;
        float k_absoluteTol = 0.001f;
        if (this.results2.separation > 0.98f * this.results1.separation + 0.001f) {
            poly1 = polyB;
            poly2 = polyA;
            xf1 = xfB;
            xf2 = xfA;
            edge1 = this.results2.edgeIndex;
            manifold.type = Manifold.ManifoldType.FACE_B;
            flip = true;
        } else {
            poly1 = polyA;
            poly2 = polyB;
            xf1 = xfA;
            xf2 = xfB;
            edge1 = this.results1.edgeIndex;
            manifold.type = Manifold.ManifoldType.FACE_A;
            flip = false;
        }
        Rot xf1q = xf1.q;
        this.findIncidentEdge(this.incidentEdge, poly1, xf1, edge1, poly2, xf2);
        int count1 = poly1.m_count;
        Vec2[] vertices1 = poly1.m_vertices;
        int iv1 = edge1;
        int iv2 = edge1 + 1 < count1 ? edge1 + 1 : 0;
        this.v11.set(vertices1[iv1]);
        this.v12.set(vertices1[iv2]);
        this.localTangent.x = this.v12.x - this.v11.x;
        this.localTangent.y = this.v12.y - this.v11.y;
        this.localTangent.normalize();
        this.localNormal.x = 1.0f * this.localTangent.y;
        this.localNormal.y = -1.0f * this.localTangent.x;
        this.planePoint.x = (this.v11.x + this.v12.x) * 0.5f;
        this.planePoint.y = (this.v11.y + this.v12.y) * 0.5f;
        this.tangent.x = xf1q.c * this.localTangent.x - xf1q.s * this.localTangent.y;
        this.tangent.y = xf1q.s * this.localTangent.x + xf1q.c * this.localTangent.y;
        float normalx = 1.0f * this.tangent.y;
        float normaly = -1.0f * this.tangent.x;
        Transform.mulToOut(xf1, this.v11, this.v11);
        Transform.mulToOut(xf1, this.v12, this.v12);
        float frontOffset = normalx * this.v11.x + normaly * this.v11.y;
        float sideOffset1 = -(this.tangent.x * this.v11.x + this.tangent.y * this.v11.y) + totalRadius;
        float sideOffset2 = this.tangent.x * this.v12.x + this.tangent.y * this.v12.y + totalRadius;
        this.tangent.negateLocal();
        int np = Collision.clipSegmentToLine(this.clipPoints1, this.incidentEdge, this.tangent, sideOffset1, iv1);
        this.tangent.negateLocal();
        if (np < 2) {
            return;
        }
        np = Collision.clipSegmentToLine(this.clipPoints2, this.clipPoints1, this.tangent, sideOffset2, iv2);
        if (np < 2) {
            return;
        }
        manifold.localNormal.set(this.localNormal);
        manifold.localPoint.set(this.planePoint);
        int pointCount = 0;
        for (int i = 0; i < 2; ++i) {
            float separation = normalx * this.clipPoints2[i].v.x + normaly * this.clipPoints2[i].v.y - frontOffset;
            if (!(separation <= totalRadius)) continue;
            ManifoldPoint cp = manifold.points[pointCount];
            Vec2 out = cp.localPoint;
            float px = this.clipPoints2[i].v.x - xf2.p.x;
            float py = this.clipPoints2[i].v.y - xf2.p.y;
            out.x = xf2.q.c * px + xf2.q.s * py;
            out.y = -xf2.q.s * px + xf2.q.c * py;
            cp.id.set(this.clipPoints2[i].id);
            if (flip) {
                cp.id.flip();
            }
            ++pointCount;
        }
        manifold.pointCount = pointCount;
    }

    public void collideEdgeAndCircle(Manifold manifold, EdgeShape edgeA, Transform xfA, CircleShape circleB, Transform xfB) {
        manifold.pointCount = 0;
        Transform.mulToOutUnsafe(xfB, circleB.m_p, this.temp);
        Transform.mulTransToOutUnsafe(xfA, this.temp, this.Q);
        Vec2 A = edgeA.m_vertex1;
        Vec2 B = edgeA.m_vertex2;
        this.e.set(B).subLocal(A);
        float u = Vec2.dot(this.e, this.temp.set(B).subLocal(this.Q));
        float v = Vec2.dot(this.e, this.temp.set(this.Q).subLocal(A));
        float radius = edgeA.m_radius + circleB.m_radius;
        this.cf.indexB = 0;
        this.cf.typeB = (byte)ContactID.Type.VERTEX.ordinal();
        if (v <= 0.0f) {
            Vec2 P = A;
            d.set(this.Q).subLocal(P);
            float dd = Vec2.dot(d, d);
            if (dd > radius * radius) {
                return;
            }
            if (edgeA.m_hasVertex0) {
                Vec2 A1 = edgeA.m_vertex0;
                Vec2 B1 = A;
                this.e1.set(B1).subLocal(A1);
                float u1 = Vec2.dot(this.e1, this.temp.set(B1).subLocal(this.Q));
                if (u1 > 0.0f) {
                    return;
                }
            }
            this.cf.indexA = 0;
            this.cf.typeA = (byte)ContactID.Type.VERTEX.ordinal();
            manifold.pointCount = 1;
            manifold.type = Manifold.ManifoldType.CIRCLES;
            manifold.localNormal.setZero();
            manifold.localPoint.set(P);
            manifold.points[0].id.set(this.cf);
            manifold.points[0].localPoint.set(circleB.m_p);
            return;
        }
        if (u <= 0.0f) {
            Vec2 P = B;
            d.set(this.Q).subLocal(P);
            float dd = Vec2.dot(d, d);
            if (dd > radius * radius) {
                return;
            }
            if (edgeA.m_hasVertex3) {
                Vec2 B2 = edgeA.m_vertex3;
                Vec2 A2 = B;
                Vec2 e2 = this.e1;
                e2.set(B2).subLocal(A2);
                float v2 = Vec2.dot(e2, this.temp.set(this.Q).subLocal(A2));
                if (v2 > 0.0f) {
                    return;
                }
            }
            this.cf.indexA = 1;
            this.cf.typeA = (byte)ContactID.Type.VERTEX.ordinal();
            manifold.pointCount = 1;
            manifold.type = Manifold.ManifoldType.CIRCLES;
            manifold.localNormal.setZero();
            manifold.localPoint.set(P);
            manifold.points[0].id.set(this.cf);
            manifold.points[0].localPoint.set(circleB.m_p);
            return;
        }
        float den = Vec2.dot(this.e, this.e);
        assert (den > 0.0f);
        this.P.set(A).mulLocal(u).addLocal(this.temp.set(B).mulLocal(v));
        this.P.mulLocal(1.0f / den);
        d.set(this.Q).subLocal(this.P);
        float dd = Vec2.dot(d, d);
        if (dd > radius * radius) {
            return;
        }
        this.n.x = -this.e.y;
        this.n.y = this.e.x;
        if (Vec2.dot(this.n, this.temp.set(this.Q).subLocal(A)) < 0.0f) {
            this.n.set(-this.n.x, -this.n.y);
        }
        this.n.normalize();
        this.cf.indexA = 0;
        this.cf.typeA = (byte)ContactID.Type.FACE.ordinal();
        manifold.pointCount = 1;
        manifold.type = Manifold.ManifoldType.FACE_A;
        manifold.localNormal.set(this.n);
        manifold.localPoint.set(A);
        manifold.points[0].id.set(this.cf);
        manifold.points[0].localPoint.set(circleB.m_p);
    }

    public void collideEdgeAndPolygon(Manifold manifold, EdgeShape edgeA, Transform xfA, PolygonShape polygonB, Transform xfB) {
        this.collider.collide(manifold, edgeA, xfA, polygonB, xfB);
    }

    static class EPCollider {
        final TempPolygon m_polygonB = new TempPolygon();
        final Transform m_xf = new Transform();
        final Vec2 m_centroidB = new Vec2();
        Vec2 m_v0 = new Vec2();
        Vec2 m_v1 = new Vec2();
        Vec2 m_v2 = new Vec2();
        Vec2 m_v3 = new Vec2();
        final Vec2 m_normal0 = new Vec2();
        final Vec2 m_normal1 = new Vec2();
        final Vec2 m_normal2 = new Vec2();
        final Vec2 m_normal = new Vec2();
        VertexType m_type1;
        VertexType m_type2;
        final Vec2 m_lowerLimit = new Vec2();
        final Vec2 m_upperLimit = new Vec2();
        float m_radius;
        boolean m_front;
        private final Vec2 edge1 = new Vec2();
        private final Vec2 temp = new Vec2();
        private final Vec2 edge0 = new Vec2();
        private final Vec2 edge2 = new Vec2();
        private final ClipVertex[] ie = new ClipVertex[2];
        private final ClipVertex[] clipPoints1 = new ClipVertex[2];
        private final ClipVertex[] clipPoints2 = new ClipVertex[2];
        private final ReferenceFace rf = new ReferenceFace();
        private final EPAxis edgeAxis = new EPAxis();
        private final EPAxis polygonAxis = new EPAxis();
        private final Vec2 perp = new Vec2();
        private final Vec2 n = new Vec2();

        public EPCollider() {
            for (int i = 0; i < 2; ++i) {
                this.ie[i] = new ClipVertex();
                this.clipPoints1[i] = new ClipVertex();
                this.clipPoints2[i] = new ClipVertex();
            }
        }

        public void collide(Manifold manifold, EdgeShape edgeA, Transform xfA, PolygonShape polygonB, Transform xfB) {
            int i;
            Transform.mulTransToOutUnsafe(xfA, xfB, this.m_xf);
            Transform.mulToOutUnsafe(this.m_xf, polygonB.m_centroid, this.m_centroidB);
            this.m_v0 = edgeA.m_vertex0;
            this.m_v1 = edgeA.m_vertex1;
            this.m_v2 = edgeA.m_vertex2;
            this.m_v3 = edgeA.m_vertex3;
            boolean hasVertex0 = edgeA.m_hasVertex0;
            boolean hasVertex3 = edgeA.m_hasVertex3;
            this.edge1.set(this.m_v2).subLocal(this.m_v1);
            this.edge1.normalize();
            this.m_normal1.set(this.edge1.y, -this.edge1.x);
            float offset1 = Vec2.dot(this.m_normal1, this.temp.set(this.m_centroidB).subLocal(this.m_v1));
            float offset0 = 0.0f;
            float offset2 = 0.0f;
            boolean convex1 = false;
            boolean convex2 = false;
            if (hasVertex0) {
                this.edge0.set(this.m_v1).subLocal(this.m_v0);
                this.edge0.normalize();
                this.m_normal0.set(this.edge0.y, -this.edge0.x);
                convex1 = Vec2.cross(this.edge0, this.edge1) >= 0.0f;
                offset0 = Vec2.dot(this.m_normal0, this.temp.set(this.m_centroidB).subLocal(this.m_v0));
            }
            if (hasVertex3) {
                this.edge2.set(this.m_v3).subLocal(this.m_v2);
                this.edge2.normalize();
                this.m_normal2.set(this.edge2.y, -this.edge2.x);
                convex2 = Vec2.cross(this.edge1, this.edge2) > 0.0f;
                offset2 = Vec2.dot(this.m_normal2, this.temp.set(this.m_centroidB).subLocal(this.m_v2));
            }
            if (hasVertex0 && hasVertex3) {
                if (convex1 && convex2) {
                    boolean bl = this.m_front = offset0 >= 0.0f || offset1 >= 0.0f || offset2 >= 0.0f;
                    if (this.m_front) {
                        this.m_normal.x = this.m_normal1.x;
                        this.m_normal.y = this.m_normal1.y;
                        this.m_lowerLimit.x = this.m_normal0.x;
                        this.m_lowerLimit.y = this.m_normal0.y;
                        this.m_upperLimit.x = this.m_normal2.x;
                        this.m_upperLimit.y = this.m_normal2.y;
                    } else {
                        this.m_normal.x = -this.m_normal1.x;
                        this.m_normal.y = -this.m_normal1.y;
                        this.m_lowerLimit.x = -this.m_normal1.x;
                        this.m_lowerLimit.y = -this.m_normal1.y;
                        this.m_upperLimit.x = -this.m_normal1.x;
                        this.m_upperLimit.y = -this.m_normal1.y;
                    }
                } else if (convex1) {
                    boolean bl = this.m_front = offset0 >= 0.0f || offset1 >= 0.0f && offset2 >= 0.0f;
                    if (this.m_front) {
                        this.m_normal.x = this.m_normal1.x;
                        this.m_normal.y = this.m_normal1.y;
                        this.m_lowerLimit.x = this.m_normal0.x;
                        this.m_lowerLimit.y = this.m_normal0.y;
                        this.m_upperLimit.x = this.m_normal1.x;
                        this.m_upperLimit.y = this.m_normal1.y;
                    } else {
                        this.m_normal.x = -this.m_normal1.x;
                        this.m_normal.y = -this.m_normal1.y;
                        this.m_lowerLimit.x = -this.m_normal2.x;
                        this.m_lowerLimit.y = -this.m_normal2.y;
                        this.m_upperLimit.x = -this.m_normal1.x;
                        this.m_upperLimit.y = -this.m_normal1.y;
                    }
                } else if (convex2) {
                    boolean bl = this.m_front = offset2 >= 0.0f || offset0 >= 0.0f && offset1 >= 0.0f;
                    if (this.m_front) {
                        this.m_normal.x = this.m_normal1.x;
                        this.m_normal.y = this.m_normal1.y;
                        this.m_lowerLimit.x = this.m_normal1.x;
                        this.m_lowerLimit.y = this.m_normal1.y;
                        this.m_upperLimit.x = this.m_normal2.x;
                        this.m_upperLimit.y = this.m_normal2.y;
                    } else {
                        this.m_normal.x = -this.m_normal1.x;
                        this.m_normal.y = -this.m_normal1.y;
                        this.m_lowerLimit.x = -this.m_normal1.x;
                        this.m_lowerLimit.y = -this.m_normal1.y;
                        this.m_upperLimit.x = -this.m_normal0.x;
                        this.m_upperLimit.y = -this.m_normal0.y;
                    }
                } else {
                    boolean bl = this.m_front = offset0 >= 0.0f && offset1 >= 0.0f && offset2 >= 0.0f;
                    if (this.m_front) {
                        this.m_normal.x = this.m_normal1.x;
                        this.m_normal.y = this.m_normal1.y;
                        this.m_lowerLimit.x = this.m_normal1.x;
                        this.m_lowerLimit.y = this.m_normal1.y;
                        this.m_upperLimit.x = this.m_normal1.x;
                        this.m_upperLimit.y = this.m_normal1.y;
                    } else {
                        this.m_normal.x = -this.m_normal1.x;
                        this.m_normal.y = -this.m_normal1.y;
                        this.m_lowerLimit.x = -this.m_normal2.x;
                        this.m_lowerLimit.y = -this.m_normal2.y;
                        this.m_upperLimit.x = -this.m_normal0.x;
                        this.m_upperLimit.y = -this.m_normal0.y;
                    }
                }
            } else if (hasVertex0) {
                if (convex1) {
                    boolean bl = this.m_front = offset0 >= 0.0f || offset1 >= 0.0f;
                    if (this.m_front) {
                        this.m_normal.x = this.m_normal1.x;
                        this.m_normal.y = this.m_normal1.y;
                        this.m_lowerLimit.x = this.m_normal0.x;
                        this.m_lowerLimit.y = this.m_normal0.y;
                        this.m_upperLimit.x = -this.m_normal1.x;
                        this.m_upperLimit.y = -this.m_normal1.y;
                    } else {
                        this.m_normal.x = -this.m_normal1.x;
                        this.m_normal.y = -this.m_normal1.y;
                        this.m_lowerLimit.x = this.m_normal1.x;
                        this.m_lowerLimit.y = this.m_normal1.y;
                        this.m_upperLimit.x = -this.m_normal1.x;
                        this.m_upperLimit.y = -this.m_normal1.y;
                    }
                } else {
                    boolean bl = this.m_front = offset0 >= 0.0f && offset1 >= 0.0f;
                    if (this.m_front) {
                        this.m_normal.x = this.m_normal1.x;
                        this.m_normal.y = this.m_normal1.y;
                        this.m_lowerLimit.x = this.m_normal1.x;
                        this.m_lowerLimit.y = this.m_normal1.y;
                        this.m_upperLimit.x = -this.m_normal1.x;
                        this.m_upperLimit.y = -this.m_normal1.y;
                    } else {
                        this.m_normal.x = -this.m_normal1.x;
                        this.m_normal.y = -this.m_normal1.y;
                        this.m_lowerLimit.x = this.m_normal1.x;
                        this.m_lowerLimit.y = this.m_normal1.y;
                        this.m_upperLimit.x = -this.m_normal0.x;
                        this.m_upperLimit.y = -this.m_normal0.y;
                    }
                }
            } else if (hasVertex3) {
                if (convex2) {
                    boolean bl = this.m_front = offset1 >= 0.0f || offset2 >= 0.0f;
                    if (this.m_front) {
                        this.m_normal.x = this.m_normal1.x;
                        this.m_normal.y = this.m_normal1.y;
                        this.m_lowerLimit.x = -this.m_normal1.x;
                        this.m_lowerLimit.y = -this.m_normal1.y;
                        this.m_upperLimit.x = this.m_normal2.x;
                        this.m_upperLimit.y = this.m_normal2.y;
                    } else {
                        this.m_normal.x = -this.m_normal1.x;
                        this.m_normal.y = -this.m_normal1.y;
                        this.m_lowerLimit.x = -this.m_normal1.x;
                        this.m_lowerLimit.y = -this.m_normal1.y;
                        this.m_upperLimit.x = this.m_normal1.x;
                        this.m_upperLimit.y = this.m_normal1.y;
                    }
                } else {
                    boolean bl = this.m_front = offset1 >= 0.0f && offset2 >= 0.0f;
                    if (this.m_front) {
                        this.m_normal.x = this.m_normal1.x;
                        this.m_normal.y = this.m_normal1.y;
                        this.m_lowerLimit.x = -this.m_normal1.x;
                        this.m_lowerLimit.y = -this.m_normal1.y;
                        this.m_upperLimit.x = this.m_normal1.x;
                        this.m_upperLimit.y = this.m_normal1.y;
                    } else {
                        this.m_normal.x = -this.m_normal1.x;
                        this.m_normal.y = -this.m_normal1.y;
                        this.m_lowerLimit.x = -this.m_normal2.x;
                        this.m_lowerLimit.y = -this.m_normal2.y;
                        this.m_upperLimit.x = this.m_normal1.x;
                        this.m_upperLimit.y = this.m_normal1.y;
                    }
                }
            } else {
                boolean bl = this.m_front = offset1 >= 0.0f;
                if (this.m_front) {
                    this.m_normal.x = this.m_normal1.x;
                    this.m_normal.y = this.m_normal1.y;
                    this.m_lowerLimit.x = -this.m_normal1.x;
                    this.m_lowerLimit.y = -this.m_normal1.y;
                    this.m_upperLimit.x = -this.m_normal1.x;
                    this.m_upperLimit.y = -this.m_normal1.y;
                } else {
                    this.m_normal.x = -this.m_normal1.x;
                    this.m_normal.y = -this.m_normal1.y;
                    this.m_lowerLimit.x = this.m_normal1.x;
                    this.m_lowerLimit.y = this.m_normal1.y;
                    this.m_upperLimit.x = this.m_normal1.x;
                    this.m_upperLimit.y = this.m_normal1.y;
                }
            }
            this.m_polygonB.count = polygonB.m_count;
            for (int i2 = 0; i2 < polygonB.m_count; ++i2) {
                Transform.mulToOutUnsafe(this.m_xf, polygonB.m_vertices[i2], this.m_polygonB.vertices[i2]);
                Rot.mulToOutUnsafe(this.m_xf.q, polygonB.m_normals[i2], this.m_polygonB.normals[i2]);
            }
            this.m_radius = 0.02f;
            manifold.pointCount = 0;
            this.computeEdgeSeparation(this.edgeAxis);
            if (this.edgeAxis.type == EPAxis.Type.UNKNOWN) {
                return;
            }
            if (this.edgeAxis.separation > this.m_radius) {
                return;
            }
            this.computePolygonSeparation(this.polygonAxis);
            if (this.polygonAxis.type != EPAxis.Type.UNKNOWN && this.polygonAxis.separation > this.m_radius) {
                return;
            }
            float k_relativeTol = 0.98f;
            float k_absoluteTol = 0.001f;
            EPAxis primaryAxis = this.polygonAxis.type == EPAxis.Type.UNKNOWN ? this.edgeAxis : (this.polygonAxis.separation > 0.98f * this.edgeAxis.separation + 0.001f ? this.polygonAxis : this.edgeAxis);
            ClipVertex ie0 = this.ie[0];
            ClipVertex ie1 = this.ie[1];
            if (primaryAxis.type == EPAxis.Type.EDGE_A) {
                manifold.type = Manifold.ManifoldType.FACE_A;
                int bestIndex = 0;
                float bestValue = Vec2.dot(this.m_normal, this.m_polygonB.normals[0]);
                for (i = 1; i < this.m_polygonB.count; ++i) {
                    float value = Vec2.dot(this.m_normal, this.m_polygonB.normals[i]);
                    if (!(value < bestValue)) continue;
                    bestValue = value;
                    bestIndex = i;
                }
                int i1 = bestIndex;
                int i2 = i1 + 1 < this.m_polygonB.count ? i1 + 1 : 0;
                ie0.v.set(this.m_polygonB.vertices[i1]);
                ie0.id.indexA = 0;
                ie0.id.indexB = (byte)i1;
                ie0.id.typeA = (byte)ContactID.Type.FACE.ordinal();
                ie0.id.typeB = (byte)ContactID.Type.VERTEX.ordinal();
                ie1.v.set(this.m_polygonB.vertices[i2]);
                ie1.id.indexA = 0;
                ie1.id.indexB = (byte)i2;
                ie1.id.typeA = (byte)ContactID.Type.FACE.ordinal();
                ie1.id.typeB = (byte)ContactID.Type.VERTEX.ordinal();
                if (this.m_front) {
                    this.rf.i1 = 0;
                    this.rf.i2 = 1;
                    this.rf.v1.set(this.m_v1);
                    this.rf.v2.set(this.m_v2);
                    this.rf.normal.set(this.m_normal1);
                } else {
                    this.rf.i1 = 1;
                    this.rf.i2 = 0;
                    this.rf.v1.set(this.m_v2);
                    this.rf.v2.set(this.m_v1);
                    this.rf.normal.set(this.m_normal1).negateLocal();
                }
            } else {
                manifold.type = Manifold.ManifoldType.FACE_B;
                ie0.v.set(this.m_v1);
                ie0.id.indexA = 0;
                ie0.id.indexB = (byte)primaryAxis.index;
                ie0.id.typeA = (byte)ContactID.Type.VERTEX.ordinal();
                ie0.id.typeB = (byte)ContactID.Type.FACE.ordinal();
                ie1.v.set(this.m_v2);
                ie1.id.indexA = 0;
                ie1.id.indexB = (byte)primaryAxis.index;
                ie1.id.typeA = (byte)ContactID.Type.VERTEX.ordinal();
                ie1.id.typeB = (byte)ContactID.Type.FACE.ordinal();
                this.rf.i1 = primaryAxis.index;
                this.rf.i2 = this.rf.i1 + 1 < this.m_polygonB.count ? this.rf.i1 + 1 : 0;
                this.rf.v1.set(this.m_polygonB.vertices[this.rf.i1]);
                this.rf.v2.set(this.m_polygonB.vertices[this.rf.i2]);
                this.rf.normal.set(this.m_polygonB.normals[this.rf.i1]);
            }
            this.rf.sideNormal1.set(this.rf.normal.y, -this.rf.normal.x);
            this.rf.sideNormal2.set(this.rf.sideNormal1).negateLocal();
            this.rf.sideOffset1 = Vec2.dot(this.rf.sideNormal1, this.rf.v1);
            this.rf.sideOffset2 = Vec2.dot(this.rf.sideNormal2, this.rf.v2);
            int np = Collision.clipSegmentToLine(this.clipPoints1, this.ie, this.rf.sideNormal1, this.rf.sideOffset1, this.rf.i1);
            if (np < 2) {
                return;
            }
            np = Collision.clipSegmentToLine(this.clipPoints2, this.clipPoints1, this.rf.sideNormal2, this.rf.sideOffset2, this.rf.i2);
            if (np < 2) {
                return;
            }
            if (primaryAxis.type == EPAxis.Type.EDGE_A) {
                manifold.localNormal.set(this.rf.normal);
                manifold.localPoint.set(this.rf.v1);
            } else {
                manifold.localNormal.set(polygonB.m_normals[this.rf.i1]);
                manifold.localPoint.set(polygonB.m_vertices[this.rf.i1]);
            }
            int pointCount = 0;
            for (i = 0; i < 2; ++i) {
                float separation = Vec2.dot(this.rf.normal, this.temp.set(this.clipPoints2[i].v).subLocal(this.rf.v1));
                if (!(separation <= this.m_radius)) continue;
                ManifoldPoint cp = manifold.points[pointCount];
                if (primaryAxis.type == EPAxis.Type.EDGE_A) {
                    Transform.mulTransToOutUnsafe(this.m_xf, this.clipPoints2[i].v, cp.localPoint);
                    cp.id.set(this.clipPoints2[i].id);
                } else {
                    cp.localPoint.set(this.clipPoints2[i].v);
                    cp.id.typeA = this.clipPoints2[i].id.typeB;
                    cp.id.typeB = this.clipPoints2[i].id.typeA;
                    cp.id.indexA = this.clipPoints2[i].id.indexB;
                    cp.id.indexB = this.clipPoints2[i].id.indexA;
                }
                ++pointCount;
            }
            manifold.pointCount = pointCount;
        }

        public void computeEdgeSeparation(EPAxis axis) {
            axis.type = EPAxis.Type.EDGE_A;
            axis.index = this.m_front ? 0 : 1;
            axis.separation = Float.MAX_VALUE;
            float nx = this.m_normal.x;
            float ny = this.m_normal.y;
            for (int i = 0; i < this.m_polygonB.count; ++i) {
                Vec2 v = this.m_polygonB.vertices[i];
                float tempx = v.x - this.m_v1.x;
                float tempy = v.y - this.m_v1.y;
                float s = nx * tempx + ny * tempy;
                if (!(s < axis.separation)) continue;
                axis.separation = s;
            }
        }

        public void computePolygonSeparation(EPAxis axis) {
            axis.type = EPAxis.Type.UNKNOWN;
            axis.index = -1;
            axis.separation = -3.4028235E38f;
            this.perp.x = -this.m_normal.y;
            this.perp.y = this.m_normal.x;
            for (int i = 0; i < this.m_polygonB.count; ++i) {
                Vec2 normalB = this.m_polygonB.normals[i];
                Vec2 vB = this.m_polygonB.vertices[i];
                this.n.x = -normalB.x;
                this.n.y = -normalB.y;
                float tempx = vB.x - this.m_v1.x;
                float tempy = vB.y - this.m_v1.y;
                float s1 = this.n.x * tempx + this.n.y * tempy;
                float s2 = this.n.x * (tempx = vB.x - this.m_v2.x) + this.n.y * (tempy = vB.y - this.m_v2.y);
                float s = MathUtils.min(s1, s2);
                if (s > this.m_radius) {
                    axis.type = EPAxis.Type.EDGE_B;
                    axis.index = i;
                    axis.separation = s;
                    return;
                }
                if ((!(this.n.x * this.perp.x + this.n.y * this.perp.y >= 0.0f) ? Vec2.dot(this.temp.set(this.n).subLocal(this.m_lowerLimit), this.m_normal) < -0.03490659f : Vec2.dot(this.temp.set(this.n).subLocal(this.m_upperLimit), this.m_normal) < -0.03490659f) || !(s > axis.separation)) continue;
                axis.type = EPAxis.Type.EDGE_B;
                axis.index = i;
                axis.separation = s;
            }
        }

        static enum VertexType {
            ISOLATED,
            CONCAVE,
            CONVEX;

        }
    }

    static class ReferenceFace {
        int i1;
        int i2;
        final Vec2 v1 = new Vec2();
        final Vec2 v2 = new Vec2();
        final Vec2 normal = new Vec2();
        final Vec2 sideNormal1 = new Vec2();
        float sideOffset1;
        final Vec2 sideNormal2 = new Vec2();
        float sideOffset2;

        ReferenceFace() {
        }
    }

    static class TempPolygon {
        final Vec2[] vertices = new Vec2[8];
        final Vec2[] normals = new Vec2[8];
        int count;

        public TempPolygon() {
            for (int i = 0; i < this.vertices.length; ++i) {
                this.vertices[i] = new Vec2();
                this.normals[i] = new Vec2();
            }
        }
    }

    static class EPAxis {
        Type type;
        int index;
        float separation;

        EPAxis() {
        }

        static enum Type {
            UNKNOWN,
            EDGE_A,
            EDGE_B;

        }
    }

    public static enum PointState {
        NULL_STATE,
        ADD_STATE,
        PERSIST_STATE,
        REMOVE_STATE;

    }

    public static class ClipVertex {
        public final Vec2 v = new Vec2();
        public final ContactID id = new ContactID();

        public void set(ClipVertex cv) {
            Vec2 v1 = cv.v;
            this.v.x = v1.x;
            this.v.y = v1.y;
            ContactID c = cv.id;
            this.id.indexA = c.indexA;
            this.id.indexB = c.indexB;
            this.id.typeA = c.typeA;
            this.id.typeB = c.typeB;
        }
    }

    private static class EdgeResults {
        public float separation;
        public int edgeIndex;

        private EdgeResults() {
        }
    }
}

