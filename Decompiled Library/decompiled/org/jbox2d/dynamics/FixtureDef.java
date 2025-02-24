/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Filter;

public class FixtureDef {
    public Shape shape = null;
    public Object userData = null;
    public float friction = 0.2f;
    public float restitution = 0.0f;
    public float density = 0.0f;
    public boolean isSensor = false;
    public Filter filter = new Filter();
}

