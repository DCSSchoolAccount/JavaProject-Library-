package compile.ast;

import compile.SymbolTable;

public class VarDecl extends Stm {

    public final Type type;
    public final String name;
    public final Exp exp;

    public VarDecl(Type type, String name, Exp exp) {
        this.type = type;
        this.name = name;
        this.exp = exp;
    }

    public void compile(SymbolTable st) {
        if (exp == null){
            return;
        }
        if (st.isLocal(name)){
            int offset = st.getLocalOffset(name);
            emit("get_fp");
            emit("push "+ offset);
            emit("add");
            exp.compile(st);
            emit("store");
            return;
        }
        if (st.isGlobal(name)){
            exp.compile(st);
            emit("storei "+ SymbolTable.makeIdLabel(name));
            return;
        }
        throw new RuntimeException("Undeclared variable: "+name);
    }

}