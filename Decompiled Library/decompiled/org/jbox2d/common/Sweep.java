/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.common;

import java.io.Serializable;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

public class Sweep
implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Vec2 localCenter = new Vec2();
    public final Vec2 c0 = new Vec2();
    public final Vec2 c = new Vec2();
    public float a0;
    public float a;
    public float alpha0;

    public String toString() {
        String s = "Sweep:\nlocalCenter: " + this.localCenter + "\n";
        s = s + "c0: " + this.c0 + ", c: " + this.c + "\n";
        s = s + "a0: " + this.a0 + ", a: " + this.a + "\n";
        return s;
    }

    public final void normalize() {
        float d = (float)Math.PI * 2 * (float)MathUtils.floor(this.a0 / ((float)Math.PI * 2));
        this.a0 -= d;
        this.a -= d;
    }

    public final Sweep set(Sweep argCloneFrom) {
        this.localCenter.set(argCloneFrom.localCenter);
        this.c0.set(argCloneFrom.c0);
        this.c.set(argCloneFrom.c);
        this.a0 = argCloneFrom.a0;
        this.a = argCloneFrom.a;
        return this;
    }

    public final void getTransform(Transform xf, float beta) {
        assert (xf != null);
        xf.p.x = (1.0f - beta) * this.c0.x + beta * this.c.x;
        xf.p.y = (1.0f - beta) * this.c0.y + beta * this.c.y;
        xf.q.set((1.0f - beta) * this.a0 + beta * this.a);
        Rot q = xf.q;
        xf.p.x -= q.c * this.localCenter.x - q.s * this.localCenter.y;
        xf.p.y -= q.s * this.localCenter.x + q.c * this.localCenter.y;
    }

    public final void advance(float alpha) {
        this.c0.x = (1.0f - alpha) * this.c0.x + alpha * this.c.x;
        this.c0.y = (1.0f - alpha) * this.c0.y + alpha * this.c.y;
        this.a0 = (1.0f - alpha) * this.a0 + alpha * this.a;
    }
}

