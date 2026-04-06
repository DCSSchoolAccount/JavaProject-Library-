package compile.ast;

import compile.SymbolTable;

public class ExpNull extends Exp {


    @Override
    public void compile(SymbolTable st) {
        emit("push 0");
    }
}
