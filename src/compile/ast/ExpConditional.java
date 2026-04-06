package compile.ast;

import compile.SymbolTable;

public class ExpConditional extends Exp {

    public final Exp exp;
    public final Exp thenExp;
    public final Exp elseExp;


    public ExpConditional(Exp exp, Exp thenExp, Exp elseExp) {
        this.exp = exp;
        this.thenExp = thenExp;
        this.elseExp = elseExp;
    }

    @Override
    public void compile(SymbolTable st) {

        String elseLabel = st.freshLabel("elseLabel");
        String endLabel = st.freshLabel("endLabel");

        exp.compile(st);
        emit("jumpi_z "+ elseLabel);

        thenExp.compile(st);
        emit("jumpi "+ endLabel);

        emit(elseLabel + ":");
        elseExp.compile(st);

        emit(endLabel +":");
    }
}
