/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.dynamics.contacts;

import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Transform;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.pooling.IWorldPool;

public class ChainAndCircleContact
extends Contact {
    private final EdgeShape edge = new EdgeShape();

    public ChainAndCircleContact(IWorldPool argPool) {
        super(argPool);
    }

    @Override
    public void init(Fixture fA, int indexA, Fixture fB, int indexB) {
        super.init(fA, indexA, fB, indexB);
        assert (this.m_fixtureA.getType() == ShapeType.CHAIN);
        assert (this.m_fixtureB.getType() == ShapeType.CIRCLE);
    }

    @Override
    public void evaluate(Manifold manifold, Transform xfA, Transform xfB) {
        ChainShape chain = (ChainShape)this.m_fixtureA.getShape();
        chain.getChildEdge(this.edge, this.m_indexA);
        this.pool.getCollision().collideEdgeAndCircle(manifold, this.edge, xfA, (CircleShape)this.m_fixtureB.getShape(), xfB);
    }
}

