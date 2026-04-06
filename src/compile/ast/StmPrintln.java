package compile.ast;

import compile.SymbolTable;

public class StmPrintln extends Stm {

    public final Exp exp;

    public StmPrintln(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        if (exp == null) {
            emit("sysc OUT_LN");
        } else {
        exp.compile(st);
        emit("sysc OUT_DEC");
        emit("sysc OUT_LN");
        }
    }

}
