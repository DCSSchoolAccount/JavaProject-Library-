package compile.ast;

import compile.SymbolTable;

public class ExpLength extends Exp {
    public Exp exp;
    public ExpLength(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        String notNull = st.freshLabel("length_not_null");
        String nullLength = st.freshLabel("length_null");

        exp.compile(st);
        emit("dup");
        emit("jumpi_z " + nullLength);
        emit("jumpi " + notNull);

        emit(nullLength + ":");
        emit("push 1");
        emit("halt");

        emit(notNull + ":");
        emit("load");
    }
}