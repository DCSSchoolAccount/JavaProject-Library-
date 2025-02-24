/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.common;

class PlatformMathUtils {
    private static final float SHIFT23 = 8388608.0f;
    private static final float INV_SHIFT23 = 1.1920929E-7f;

    PlatformMathUtils() {
    }

    public static final float fastPow(float a, float b) {
        float x = Float.floatToRawIntBits(a);
        x *= 1.1920929E-7f;
        float y = x - (float)((x -= 127.0f) >= 0.0f ? (int)x : (int)x - 1);
        y = b - (float)(b >= 0.0f ? (int)b : (int)(b *= x + (y - y * y) * 0.346607f) - 1);
        y = (y - y * y) * 0.33971f;
        return Float.intBitsToFloat((int)((b + 127.0f - y) * 8388608.0f));
    }
}

