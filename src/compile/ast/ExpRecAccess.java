package compile.ast;

import compile.SymbolTable;

public class ExpRecAccess extends Exp {
    public final Exp recExp;
    public final String fieldName;

    public ExpRecAccess(Exp recexp, String fieldName) {
        this.recExp = recexp;
        this.fieldName = fieldName;
    }


    @Override
    public void compile(SymbolTable st) {
        Type t = st.inferExprType(recExp);
        if (!(t instanceof TypeRec)) {
            throw new RuntimeException("Field access on non-record");
        }

        String recordTypeName = ((TypeRec) t).id;
        int offset = st.getFieldOffset(recordTypeName, fieldName);

        String nullFail = st.freshLabel("rec_null");
        String done = st.freshLabel("rec_done");

        recExp.compile(st);
        emit("dup");
        emit("jumpi_z " + nullFail);

        emit("push " + offset);
        emit("add");
        emit("load");
        emit("jumpi " + done);

        emit(nullFail + ":");
        emit("push 1");
        emit("halt");

        emit(done + ":");
    }
}
