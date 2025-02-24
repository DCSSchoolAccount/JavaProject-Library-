/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.pooling.normal;

import org.jbox2d.pooling.IDynamicStack;

public abstract class MutableStack<E>
implements IDynamicStack<E> {
    private Object[] stack = null;
    private int index = 0;
    private int size;

    public MutableStack(int argInitSize) {
        this.extendStack(argInitSize);
    }

    private void extendStack(int argSize) {
        Object[] newStack = new Object[argSize];
        if (this.stack != null) {
            System.arraycopy(this.stack, 0, newStack, 0, this.size);
        }
        for (int i = 0; i < newStack.length; ++i) {
            newStack[i] = this.newInstance();
        }
        this.stack = newStack;
        this.size = newStack.length;
    }

    @Override
    public final E pop() {
        if (this.index >= this.size) {
            this.extendStack(this.size * 2);
        }
        return (E)this.stack[this.index++];
    }

    @Override
    public final void push(E argObject) {
        assert (this.index > 0);
        this.stack[--this.index] = argObject;
    }

    protected abstract E newInstance();
}

