/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.contacts;

import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.pooling.IWorldPool;

public interface ContactCreator {
    public Contact contactCreateFcn(IWorldPool var1, Fixture var2, Fixture var3);

    public void contactDestroyFcn(IWorldPool var1, Contact var2);
}

