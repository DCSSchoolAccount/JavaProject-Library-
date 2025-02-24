/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.pooling.normal;

public abstract class OrderedStack<E> {
    private final Object[] pool;
    private int index;
    private final int size;
    private final Object[] container;

    public OrderedStack(int argStackSize, int argContainerSize) {
        this.size = argStackSize;
        this.pool = new Object[argStackSize];
        for (int i = 0; i < argStackSize; ++i) {
            this.pool[i] = this.newInstance();
        }
        this.index = 0;
        this.container = new Object[argContainerSize];
    }

    public final E pop() {
        assert (this.index < this.size) : "End of stack reached, there is probably a leak somewhere";
        return (E)this.pool[this.index++];
    }

    public final E[] pop(int argNum) {
        assert (this.index + argNum < this.size) : "End of stack reached, there is probably a leak somewhere";
        assert (argNum <= this.container.length) : "Container array is too small";
        System.arraycopy(this.pool, this.index, this.container, 0, argNum);
        this.index += argNum;
        return this.container;
    }

    public final void push(int argNum) {
        this.index -= argNum;
        assert (this.index >= 0) : "Beginning of stack reached, push/pops are unmatched";
    }

    protected abstract E newInstance();
}

