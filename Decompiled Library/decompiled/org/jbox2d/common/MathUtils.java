/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.common;

import java.util.Random;
import org.jbox2d.common.PlatformMathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;

public class MathUtils
extends PlatformMathUtils {
    public static final float PI = (float)Math.PI;
    public static final float TWOPI = (float)Math.PI * 2;
    public static final float INV_PI = 0.31830987f;
    public static final float HALF_PI = 1.5707964f;
    public static final float QUARTER_PI = 0.7853982f;
    public static final float THREE_HALVES_PI = 4.712389f;
    public static final float DEG2RAD = (float)Math.PI / 180;
    public static final float RAD2DEG = 57.295776f;
    public static final float[] sinLUT = new float[Settings.SINCOS_LUT_LENGTH];

    public static final float sin(float x) {
        if (Settings.SINCOS_LUT_ENABLED) {
            return MathUtils.sinLUT(x);
        }
        return (float)StrictMath.sin(x);
    }

    public static final float sinLUT(float x) {
        if ((x %= (float)Math.PI * 2) < 0.0f) {
            x += (float)Math.PI * 2;
        }
        if (Settings.SINCOS_LUT_LERP) {
            int index = (int)(x /= 1.1E-4f);
            if (index != 0) {
                x %= (float)index;
            }
            if (index == Settings.SINCOS_LUT_LENGTH - 1) {
                return (1.0f - x) * sinLUT[index] + x * sinLUT[0];
            }
            return (1.0f - x) * sinLUT[index] + x * sinLUT[index + 1];
        }
        return sinLUT[MathUtils.round(x / 1.1E-4f) % Settings.SINCOS_LUT_LENGTH];
    }

    public static final float cos(float x) {
        if (Settings.SINCOS_LUT_ENABLED) {
            return MathUtils.sinLUT(1.5707964f - x);
        }
        return (float)StrictMath.cos(x);
    }

    public static final float abs(float x) {
        if (Settings.FAST_ABS) {
            return x > 0.0f ? x : -x;
        }
        return Math.abs(x);
    }

    public static final int abs(int x) {
        int y = x >> 31;
        return (x ^ y) - y;
    }

    public static final int floor(float x) {
        if (Settings.FAST_FLOOR) {
            int y = (int)x;
            if (x < 0.0f && x != (float)y) {
                return y - 1;
            }
            return y;
        }
        return (int)Math.floor(x);
    }

    public static final int ceil(float x) {
        if (Settings.FAST_CEIL) {
            int y = (int)x;
            if (x > 0.0f && x != (float)y) {
                return y + 1;
            }
            return y;
        }
        return (int)Math.ceil(x);
    }

    public static final int round(float x) {
        if (Settings.FAST_ROUND) {
            return MathUtils.floor(x + 0.5f);
        }
        return StrictMath.round(x);
    }

    public static final int ceilPowerOf2(int x) {
        int pow2;
        for (pow2 = 1; pow2 < x; pow2 <<= 1) {
        }
        return pow2;
    }

    public static final float max(float a, float b) {
        return a > b ? a : b;
    }

    public static final int max(int a, int b) {
        return a > b ? a : b;
    }

    public static final float min(float a, float b) {
        return a < b ? a : b;
    }

    public static final int min(int a, int b) {
        return a < b ? a : b;
    }

    public static final float map(float val, float fromMin, float fromMax, float toMin, float toMax) {
        float mult = (val - fromMin) / (fromMax - fromMin);
        float res = toMin + mult * (toMax - toMin);
        return res;
    }

    public static final float clamp(float a, float low, float high) {
        return MathUtils.max(low, MathUtils.min(a, high));
    }

    public static final Vec2 clamp(Vec2 a, Vec2 low, Vec2 high) {
        Vec2 min = new Vec2();
        min.x = a.x < high.x ? a.x : high.x;
        min.y = a.y < high.y ? a.y : high.y;
        min.x = low.x > min.x ? low.x : min.x;
        min.y = low.y > min.y ? low.y : min.y;
        return min;
    }

    public static final void clampToOut(Vec2 a, Vec2 low, Vec2 high, Vec2 dest) {
        dest.x = a.x < high.x ? a.x : high.x;
        dest.y = a.y < high.y ? a.y : high.y;
        dest.x = low.x > dest.x ? low.x : dest.x;
        dest.y = low.y > dest.y ? low.y : dest.y;
    }

    public static final int nextPowerOfTwo(int x) {
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }

    public static final boolean isPowerOfTwo(int x) {
        return x > 0 && (x & x - 1) == 0;
    }

    public static final float atan2(float y, float x) {
        if (Settings.FAST_ATAN2) {
            return MathUtils.fastAtan2(y, x);
        }
        return (float)StrictMath.atan2(y, x);
    }

    public static final float fastAtan2(float y, float x) {
        float atan;
        if (x == 0.0f) {
            if (y > 0.0f) {
                return 1.5707964f;
            }
            if (y == 0.0f) {
                return 0.0f;
            }
            return -1.5707964f;
        }
        float z = y / x;
        if (MathUtils.abs(z) < 1.0f) {
            atan = z / (1.0f + 0.28f * z * z);
            if (x < 0.0f) {
                if (y < 0.0f) {
                    return atan - (float)Math.PI;
                }
                return atan + (float)Math.PI;
            }
        } else {
            atan = 1.5707964f - z / (z * z + 0.28f);
            if (y < 0.0f) {
                return atan - (float)Math.PI;
            }
        }
        return atan;
    }

    public static final float reduceAngle(float theta) {
        if (MathUtils.abs(theta %= (float)Math.PI * 2) > (float)Math.PI) {
            theta -= (float)Math.PI * 2;
        }
        if (MathUtils.abs(theta) > 1.5707964f) {
            theta = (float)Math.PI - theta;
        }
        return theta;
    }

    public static final float randomFloat(float argLow, float argHigh) {
        return (float)Math.random() * (argHigh - argLow) + argLow;
    }

    public static final float randomFloat(Random r, float argLow, float argHigh) {
        return r.nextFloat() * (argHigh - argLow) + argLow;
    }

    public static final float sqrt(float x) {
        return (float)StrictMath.sqrt(x);
    }

    public static final float distanceSquared(Vec2 v1, Vec2 v2) {
        float dx = v1.x - v2.x;
        float dy = v1.y - v2.y;
        return dx * dx + dy * dy;
    }

    public static final float distance(Vec2 v1, Vec2 v2) {
        return MathUtils.sqrt(MathUtils.distanceSquared(v1, v2));
    }

    static {
        for (int i = 0; i < Settings.SINCOS_LUT_LENGTH; ++i) {
            MathUtils.sinLUT[i] = (float)Math.sin((float)i * 1.1E-4f);
        }
    }
}

