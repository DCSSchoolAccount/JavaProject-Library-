package compile.ast;

import compile.SymbolTable;

import java.util.List;

public class ExpMethodCall extends Exp {
    public final String methodName;
    public final List<Exp> paramList;

    public ExpMethodCall(String methodName, List<Exp> paramList) {
        this.methodName = methodName;
        this.paramList = paramList;
    }


    @Override
    public void compile(SymbolTable st) {
        for (Exp param : paramList){
            param.compile(st);
        }

        emit("push "+ paramList.size());
        emit("calli " + SymbolTable.makeMethodLabel(methodName));
    }
}
