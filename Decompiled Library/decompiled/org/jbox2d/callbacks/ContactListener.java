/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.callbacks;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;

public interface ContactListener {
    public void beginContact(Contact var1);

    public void endContact(Contact var1);

    public void preSolve(Contact var1, Manifold var2);

    public void postSolve(Contact var1, ContactImpulse var2);
}

