package compile.ast;

import compile.SymbolTable;

import java.util.List;

public class Program extends AST {

    public final List<RecDef> records;
    public final List<Stm> body;
    public final List<MethodDef> methods;

    /**
     * Initialise a new Program AST.
     *
     * @param body    the statements in the main body of the program
     * @param methods the declared methods after the end of the body
     */
    public Program(List<RecDef> records, List<Stm> body, List<MethodDef> methods) {
        this.records = records;
        this.body = List.copyOf(body);
        this.methods = List.copyOf(methods);
    }


    /**
     * Emit SSM assembly code for this program.
     */
    public void compile() {
        SymbolTable st = new SymbolTable(this);

        for (RecDef rec : records) {
            st.declareRecord(rec);
        }

        for (Stm stm: body){
            if ( stm instanceof VarDecl) {
                st.declareVariable((VarDecl) stm);
            }
        }
        for(Stm stm: body) {
            stm.compile(st);
        }
        emit("halt");

        for (MethodDef m : methods) {
            m.compile(st);
        }

        emit(".data");
        for (VarDecl global: st.getGlobals()) {
            emit(SymbolTable.makeIdLabel(global.name)+ ": 0");
        }
    }

}
