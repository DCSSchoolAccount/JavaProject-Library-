package compile.ast;

import compile.SymbolTable;

public class StmFree extends Stm {
    public final Exp exp;

    public StmFree(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        String ok = st.freshLabel("free_ok");
        String fail = st.freshLabel("free_null");

        exp.compile(st);
        emit("dup");
        emit("jumpi_z " + fail);
        emit("jumpi " + ok);

        emit(fail + ":");
        emit("push 1");
        emit("halt");

        emit(ok + ":");
        emit("sysc FREE");
    }
}
