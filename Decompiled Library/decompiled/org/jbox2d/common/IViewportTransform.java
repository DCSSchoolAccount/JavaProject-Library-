/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.common;

import org.jbox2d.common.Vec2;

public interface IViewportTransform {
    public boolean isYFlip();

    public void setYFlip(boolean var1);

    public Vec2 getExtents();

    public void setExtents(Vec2 var1);

    public void setExtents(float var1, float var2);

    public Vec2 getCenter();

    public void setCenter(Vec2 var1);

    public void setCenter(float var1, float var2);

    public void setCamera(float var1, float var2, float var3);

    public void getWorldVectorToScreen(Vec2 var1, Vec2 var2);

    public void getScreenVectorToWorld(Vec2 var1, Vec2 var2);

    public void getWorldToScreen(Vec2 var1, Vec2 var2);

    public void getScreenToWorld(Vec2 var1, Vec2 var2);
}

