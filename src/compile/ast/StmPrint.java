package compile.ast;

import compile.SymbolTable;

public class StmPrint extends Stm {

    public final Exp exp;

    public StmPrint(Exp exp) {
        this.exp = exp;
    }


    @Override
    public void compile(SymbolTable st) {
        exp.compile(st);
        emit("sysc OUT_DEC");
    }
}
