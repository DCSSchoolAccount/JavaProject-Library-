package compile.ast;

import compile.SymbolTable;

public class ExpNewArray extends Exp {
    public final Type t;
    public final Exp size;

    public ExpNewArray(Type t, Exp size) {
        this.t = t;
        this.size = size;
    }

    @Override
    public void compile(SymbolTable st) {
        String heapFail = st.freshLabel("heap_fail");
        String done = st.freshLabel("new_array_done");

        size.compile(st);
        emit("dup");

        // Compute bytes = 4 * (n + 1)
        emit("push 1");
        emit("add");
        emit("push 4");
        emit("mul");

        // Allocate zero-filled heap block
        emit("sysc CALLOC");

        // If addr == 0 -> heap exhausted
        emit("dup");
        emit("jumpi_z " + heapFail);

        // Store header length
        emit("dup");
        emit("rot");
        emit("store");
        emit("jumpi " + done);

        emit(heapFail + ":");
        emit("pop");
        emit("pop");
        emit("push 3");
        emit("halt");

        emit(done + ":");
    }
}
