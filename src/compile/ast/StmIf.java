package compile.ast;

import compile.SymbolTable;

public class StmIf extends Stm {

    public final Exp condition;
    public final Stm thenBranch;
    public final Stm elseBranch;

    public StmIf(Exp condition, Stm thenBranch, Stm elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }


    @Override
    public void compile(SymbolTable st) {
        String elseLabel = st.freshLabel("if_else");
        String endLabel  = st.freshLabel("if_end");

        //if...
        condition.compile(st);
        emit("jumpi_z " + elseLabel);

        //then..
        thenBranch.compile(st);
        emit("jumpi " + endLabel); //skip else

        //else..
        emit(elseLabel + ":");
        if (elseBranch != null) {
            elseBranch.compile(st);
        }
        emit(endLabel + ":");
    }
}
