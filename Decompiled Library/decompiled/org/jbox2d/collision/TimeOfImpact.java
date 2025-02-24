/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.collision;

import org.jbox2d.collision.Distance;
import org.jbox2d.collision.DistanceInput;
import org.jbox2d.collision.DistanceOutput;
import org.jbox2d.collision.SeparationFunction;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Sweep;
import org.jbox2d.common.Transform;
import org.jbox2d.pooling.IWorldPool;

public class TimeOfImpact {
    public static final int MAX_ITERATIONS = 1000;
    public static int toiCalls = 0;
    public static int toiIters = 0;
    public static int toiMaxIters = 0;
    public static int toiRootIters = 0;
    public static int toiMaxRootIters = 0;
    private final Distance.SimplexCache cache = new Distance.SimplexCache();
    private final DistanceInput distanceInput = new DistanceInput();
    private final Transform xfA = new Transform();
    private final Transform xfB = new Transform();
    private final DistanceOutput distanceOutput = new DistanceOutput();
    private final SeparationFunction fcn = new SeparationFunction();
    private final int[] indexes = new int[2];
    private final Sweep sweepA = new Sweep();
    private final Sweep sweepB = new Sweep();
    private final IWorldPool pool;

    public TimeOfImpact(IWorldPool argPool) {
        this.pool = argPool;
    }

    public final void timeOfImpact(TOIOutput output, TOIInput input) {
        int iter;
        block13: {
            ++toiCalls;
            output.state = TOIOutputState.UNKNOWN;
            output.t = input.tMax;
            Distance.DistanceProxy proxyA = input.proxyA;
            Distance.DistanceProxy proxyB = input.proxyB;
            this.sweepA.set(input.sweepA);
            this.sweepB.set(input.sweepB);
            this.sweepA.normalize();
            this.sweepB.normalize();
            float tMax = input.tMax;
            float totalRadius = proxyA.m_radius + proxyB.m_radius;
            float target = MathUtils.max(0.005f, totalRadius - 0.015f);
            float tolerance = 0.00125f;
            assert (target > tolerance);
            float t1 = 0.0f;
            iter = 0;
            this.cache.count = 0;
            this.distanceInput.proxyA = input.proxyA;
            this.distanceInput.proxyB = input.proxyB;
            this.distanceInput.useRadii = false;
            do {
                this.sweepA.getTransform(this.xfA, t1);
                this.sweepB.getTransform(this.xfB, t1);
                this.distanceInput.transformA = this.xfA;
                this.distanceInput.transformB = this.xfB;
                this.pool.getDistance().distance(this.distanceOutput, this.cache, this.distanceInput);
                if (this.distanceOutput.distance <= 0.0f) {
                    output.state = TOIOutputState.OVERLAPPED;
                    output.t = 0.0f;
                    break block13;
                }
                if (this.distanceOutput.distance < target + tolerance) {
                    output.state = TOIOutputState.TOUCHING;
                    output.t = t1;
                    break block13;
                }
                this.fcn.initialize(this.cache, proxyA, this.sweepA, proxyB, this.sweepB, t1);
                boolean done = false;
                float t2 = tMax;
                int pushBackIter = 0;
                do {
                    float s2;
                    if ((s2 = this.fcn.findMinSeparation(this.indexes, t2)) > target + tolerance) {
                        output.state = TOIOutputState.SEPARATED;
                        output.t = tMax;
                        done = true;
                        break;
                    }
                    if (s2 > target - tolerance) {
                        t1 = t2;
                        break;
                    }
                    float s1 = this.fcn.evaluate(this.indexes[0], this.indexes[1], t1);
                    if (s1 < target - tolerance) {
                        output.state = TOIOutputState.FAILED;
                        output.t = t1;
                        done = true;
                        break;
                    }
                    if (s1 <= target + tolerance) {
                        output.state = TOIOutputState.TOUCHING;
                        output.t = t1;
                        done = true;
                        break;
                    }
                    int rootIterCount = 0;
                    float a1 = t1;
                    float a2 = t2;
                    do {
                        float t;
                        float s;
                        if (MathUtils.abs((s = this.fcn.evaluate(this.indexes[0], this.indexes[1], t = rootIterCount & true ? a1 + (target - s1) * (a2 - a1) / (s2 - s1) : 0.5f * (a1 + a2))) - target) < tolerance) {
                            t2 = t;
                            break;
                        }
                        if (s > target) {
                            a1 = t;
                            s1 = s;
                        } else {
                            a2 = t;
                            s2 = s;
                        }
                        ++toiRootIters;
                    } while (++rootIterCount != 50);
                    toiMaxRootIters = MathUtils.max(toiMaxRootIters, rootIterCount);
                } while (++pushBackIter != 8);
                ++iter;
                ++toiIters;
                if (done) break block13;
            } while (iter != 1000);
            output.state = TOIOutputState.FAILED;
            output.t = t1;
        }
        toiMaxIters = MathUtils.max(toiMaxIters, iter);
    }

    public static class TOIOutput {
        public TOIOutputState state;
        public float t;
    }

    public static enum TOIOutputState {
        UNKNOWN,
        FAILED,
        OVERLAPPED,
        TOUCHING,
        SEPARATED;

    }

    public static class TOIInput {
        public final Distance.DistanceProxy proxyA = new Distance.DistanceProxy();
        public final Distance.DistanceProxy proxyB = new Distance.DistanceProxy();
        public final Sweep sweepA = new Sweep();
        public final Sweep sweepB = new Sweep();
        public float tMax;
    }
}

