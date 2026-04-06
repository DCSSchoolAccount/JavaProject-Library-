package compile.ast;

import compile.SymbolTable;

public class StmAssign extends Stm {

    public final Exp lhsExpression;
    public final Exp rhsExpression;

    public StmAssign(Exp lhsExpression, Exp rhsExpression) {
        this.lhsExpression = lhsExpression;
        this.rhsExpression = rhsExpression;
    }

    private void compileArrayAssignment(ExpArrayAccess lhsExpression, SymbolTable st) {
        String nullFail = st.freshLabel("arr_null");
        String boundsFail = st.freshLabel("arr_oob");
        String ok = st.freshLabel("arr_ok");
        String done = st.freshLabel("arr_done");

        lhsExpression.array.compile(st);
        emit("dup");
        emit("jumpi_z " + nullFail);

        lhsExpression.index.compile(st);
        emit("dup");
        emit("jumpi_n " + boundsFail);

        emit("swap");
        emit("dup");
        emit("load");
        emit("rot");

        emit("dup");
        emit("rot");
        emit("sub");
        emit("jumpi_n " + ok);
        emit("jumpi " + boundsFail);

        emit(ok + ":");


        emit("push 4");
        emit("mul");
        emit("push 4");
        emit("add");
        emit("add");

        emit("jumpi " + done);

        emit(nullFail + ":");
        emit("push 1");
        emit("halt");

        emit(boundsFail + ":");
        emit("push 2");
        emit("halt");

        emit(done + ":");

        rhsExpression.compile(st);
        emit("store");

    }

    private void compileVarAssignment(ExpVar lhsExpression, SymbolTable st) {
        String id = lhsExpression.id;

        if (st.isStackVariable(id)) {
            int offset = st.getStackOffset(id);

            emit("get_fp");
            emit("push " + offset);
            emit("add");
            rhsExpression.compile(st);
            emit("store");

        } else if (st.isGlobal(id)) {
            rhsExpression.compile(st);
            emit("storei " + SymbolTable.makeIdLabel(id));

        } else {
            throw new RuntimeException("Undeclared variable: " + id);
        }
    }

    private void compileRecAssignment(ExpRecAccess lhsExpression, SymbolTable st) {
        Type baseType = st.inferExprType(lhsExpression.recExp);

        if (!(baseType instanceof TypeRec)) {
            throw new RuntimeException("Field assignment on non-record");
        }

        String recordName = ((TypeRec) baseType).id;
        int offset = st.getFieldOffset(recordName, lhsExpression.fieldName);

        String nullFail = st.freshLabel("rec_null");
        String done = st.freshLabel("rec_done");

        lhsExpression.recExp.compile(st);
        emit("dup");
        emit("jumpi_z " + nullFail);

        emit("push " + offset);
        emit("add");

        emit("jumpi " + done);

        emit(nullFail + ":");
        emit("push 1");
        emit("halt");

        emit(done + ":");

        rhsExpression.compile(st);
        emit("store");
    }

    @Override
    public void compile(SymbolTable st) {

        if (lhsExpression instanceof ExpVar) {
            compileVarAssignment((ExpVar) lhsExpression, st);
            return;
        }
        if (lhsExpression instanceof ExpArrayAccess) {
            compileArrayAssignment((ExpArrayAccess) lhsExpression, st);
            return;
        }
        if (lhsExpression instanceof ExpRecAccess) {
            compileRecAssignment((ExpRecAccess) lhsExpression, st);
            return;
        }
        throw new RuntimeException("Invalid assignment target");
    }
}
