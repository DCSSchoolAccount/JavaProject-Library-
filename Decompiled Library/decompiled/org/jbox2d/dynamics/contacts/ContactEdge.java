/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.contacts;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.contacts.Contact;

public class ContactEdge {
    public Body other = null;
    public Contact contact = null;
    public ContactEdge prev = null;
    public ContactEdge next = null;
}

