package compile.ast;

import compile.SymbolTable;

public class ExpArrayAccess extends Exp {
    public final Exp array;
    public final Exp index;
    public ExpArrayAccess(Exp exp, Exp index) {
        this.array = exp;
        this.index = index;
    }


    @Override
    public void compile(SymbolTable st) {
        String nullFail = st.freshLabel("arr_null");
        String boundsFail = st.freshLabel("arr_oob");
        String ok1 = st.freshLabel("arr_ok1");
        String ok2 = st.freshLabel("arr_ok2");

        array.compile(st);
        emit("dup");
        emit("jumpi_z " + nullFail);

        index.compile(st);

        // check index >= 0
        emit("dup");
        emit("jumpi_n " + boundsFail);

        // now need length
        emit("swap");
        emit("dup");
        emit("load");
        emit("rot");

        // check index < length
        emit("dup");
        emit("rot");
        emit("sub");
        emit("jumpi_n " + ok1);
        emit("jumpi " + boundsFail);

        emit(ok1 + ":");
        // compute base + 4 + 4*index
        emit("push 4");
        emit("mul");
        emit("push 4");
        emit("add");
        emit("add");
        emit("load");
        emit("jumpi " + ok2);

        emit(nullFail + ":");
        emit("push 1");
        emit("halt");

        emit(boundsFail + ":");
        emit("push 2");
        emit("halt");

        emit(ok2 + ":");
    }
}
