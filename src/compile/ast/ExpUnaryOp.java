package compile.ast;

import compile.SymbolTable;

public class ExpUnaryOp extends Exp {
    public final String op;
    public final Exp e;

    public ExpUnaryOp(String op, Exp e) {
        this.op = op;
        this.e = e;
    }

    @Override
    public void compile(SymbolTable st) {
        String trueLabel = st.freshLabel("TrueLabel");
        String endLabel = st.freshLabel("EndLabel");

        e.compile(st);

        emit("jumpi_z " + trueLabel);
        emit("push 0");
        emit("jumpi " + endLabel);
        emit(trueLabel + ":");
        emit("push 1");
        emit(endLabel + ":");
    }
}
