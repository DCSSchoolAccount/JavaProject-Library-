package compile.ast;

import compile.SymbolTable;

public class StmWhile extends Stm {

    public final Exp condition;
    public final Stm body;

    public StmWhile(Exp condition, Stm body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void compile(SymbolTable st) {
        String loopStartLabel = st.freshLabel("while_start");
        String loopEndLabel = st.freshLabel("while_end");

        emit(loopStartLabel + ":");
        condition.compile(st);
        emit("jumpi_z " + loopEndLabel);
        body.compile(st);
        emit("jumpi " + loopStartLabel);
        emit(loopEndLabel + ":");
    }

}
