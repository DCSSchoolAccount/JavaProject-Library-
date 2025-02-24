/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.collision.broadphase;

public class Pair
implements Comparable<Pair> {
    public int proxyIdA;
    public int proxyIdB;

    @Override
    public int compareTo(Pair pair2) {
        if (this.proxyIdA < pair2.proxyIdA) {
            return -1;
        }
        if (this.proxyIdA == pair2.proxyIdA) {
            return this.proxyIdB < pair2.proxyIdB ? -1 : (this.proxyIdB == pair2.proxyIdB ? 0 : 1);
        }
        return 1;
    }
}

