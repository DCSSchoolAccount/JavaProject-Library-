/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.common;

import java.io.Serializable;

public class Vec3
implements Serializable {
    private static final long serialVersionUID = 1L;
    public float x;
    public float y;
    public float z;

    public Vec3() {
        this.z = 0.0f;
        this.y = 0.0f;
        this.x = 0.0f;
    }

    public Vec3(float argX, float argY, float argZ) {
        this.x = argX;
        this.y = argY;
        this.z = argZ;
    }

    public Vec3(Vec3 argCopy) {
        this.x = argCopy.x;
        this.y = argCopy.y;
        this.z = argCopy.z;
    }

    public Vec3 set(Vec3 argVec) {
        this.x = argVec.x;
        this.y = argVec.y;
        this.z = argVec.z;
        return this;
    }

    public Vec3 set(float argX, float argY, float argZ) {
        this.x = argX;
        this.y = argY;
        this.z = argZ;
        return this;
    }

    public Vec3 addLocal(Vec3 argVec) {
        this.x += argVec.x;
        this.y += argVec.y;
        this.z += argVec.z;
        return this;
    }

    public Vec3 add(Vec3 argVec) {
        return new Vec3(this.x + argVec.x, this.y + argVec.y, this.z + argVec.z);
    }

    public Vec3 subLocal(Vec3 argVec) {
        this.x -= argVec.x;
        this.y -= argVec.y;
        this.z -= argVec.z;
        return this;
    }

    public Vec3 sub(Vec3 argVec) {
        return new Vec3(this.x - argVec.x, this.y - argVec.y, this.z - argVec.z);
    }

    public Vec3 mulLocal(float argScalar) {
        this.x *= argScalar;
        this.y *= argScalar;
        this.z *= argScalar;
        return this;
    }

    public Vec3 mul(float argScalar) {
        return new Vec3(this.x * argScalar, this.y * argScalar, this.z * argScalar);
    }

    public Vec3 negate() {
        return new Vec3(-this.x, -this.y, -this.z);
    }

    public Vec3 negateLocal() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public void setZero() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }

    public Vec3 clone() {
        return new Vec3(this);
    }

    public String toString() {
        return "(" + this.x + "," + this.y + "," + this.z + ")";
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + Float.floatToIntBits(this.x);
        result = 31 * result + Float.floatToIntBits(this.y);
        result = 31 * result + Float.floatToIntBits(this.z);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Vec3 other = (Vec3)obj;
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
            return false;
        }
        if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) {
            return false;
        }
        return Float.floatToIntBits(this.z) == Float.floatToIntBits(other.z);
    }

    public static final float dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static final Vec3 cross(Vec3 a, Vec3 b) {
        return new Vec3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
    }

    public static final void crossToOut(Vec3 a, Vec3 b, Vec3 out) {
        float tempy = a.z * b.x - a.x * b.z;
        float tempz = a.x * b.y - a.y * b.x;
        out.x = a.y * b.z - a.z * b.y;
        out.y = tempy;
        out.z = tempz;
    }

    public static final void crossToOutUnsafe(Vec3 a, Vec3 b, Vec3 out) {
        assert (out != b);
        assert (out != a);
        out.x = a.y * b.z - a.z * b.y;
        out.y = a.z * b.x - a.x * b.z;
        out.z = a.x * b.y - a.y * b.x;
    }
}

