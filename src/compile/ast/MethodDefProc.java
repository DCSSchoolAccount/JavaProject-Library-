package compile.ast;

import compile.SymbolTable;

import java.util.List;

public class MethodDefProc extends MethodDef {



    public MethodDefProc(String procName, List<Formal> procFormals, List<Stm> procStms) {
        super(procName,procFormals, procStms);
    }

    /**
     * Iterate through Stm's blocks in methods (Procedures) to identify and declare all local variables
     * @param s
     * @param st
     */

    private void collectLocalsFromStm(Stm s, SymbolTable st) {
        if (s instanceof VarDecl) {
            st.declareLocal((VarDecl) s);
            return;
        }

        if (s instanceof StmBlock) {
            for (Stm inner : ((StmBlock) s).stms) {
                collectLocalsFromStm(inner, st);
            }
            return;
        }

        if (s instanceof StmIf) {
            StmIf ifStm = (StmIf) s;
            collectLocalsFromStm(ifStm.thenBranch, st);
            collectLocalsFromStm(ifStm.elseBranch, st);
            return;
        }

        if (s instanceof StmWhile) {
            collectLocalsFromStm(((StmWhile) s).body, st);
        }
    }

    @Override
    public void compile(SymbolTable st) {
            emit(SymbolTable.makeMethodLabel(name) + ":");
            SymbolTable methodst = new SymbolTable(st,true);

            for (Formal f : formals) {
                methodst.declareParameter(f);
            }
            for (Stm s: stmBlock) {
                collectLocalsFromStm(s,methodst);
            } if (methodst.getLocalCount()>0){
                emit("salloc "+methodst.getLocalCount());
            }
            for (Stm s : stmBlock) {
                s.compile(methodst);
            }

            emit("push 0");
            emit("push " + (formals.size()+methodst.getLocalCount()));
            emit("ret");

    }
}