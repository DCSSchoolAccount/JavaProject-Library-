package compile.ast;

import compile.SymbolTable;

public class ExpVar extends Exp {
    public final String id;

    public ExpVar(String id) {
        this.id = id;
    }

    @Override
    public void compile(SymbolTable st) {
        if (st.isStackVariable(id)) {
            int offset = st.getStackOffset(id);
            emit("get_fp");
            emit("push " + offset);
            emit("add");
            emit("load");
        } else if (st.isGlobal(id)) {
            emit("loadi " + SymbolTable.makeIdLabel(id));
        } else {
            throw new RuntimeException("Undeclared variable: " + id);
        }    }

}
