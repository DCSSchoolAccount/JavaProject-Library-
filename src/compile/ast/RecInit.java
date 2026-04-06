package compile.ast;

import compile.SymbolTable;

import java.util.List;

public class RecInit extends Exp {
    public final String id;
    public final List<Exp> paramList;

    public RecInit(String id, List<Exp> paramList) {
        this.id = id;
        this.paramList = paramList;
    }


    @Override
    public void compile(SymbolTable st) {
        RecDef rec = st.getRecord(id);
        int fieldCount = rec.fieldDecls.size();

        String heapFail = st.freshLabel("heap_fail");
        String done = st.freshLabel("rec_init_done");

        // Allocate 4 * fieldCount bytes to heap
        emit("push " + (4 * fieldCount));
        emit("sysc CALLOC");

        // Heap exhausted?
        emit("dup");
        emit("jumpi_z " + heapFail);


        // Initialise the fields left-to-right
        for (int i = 0; i < rec.fieldDecls.size(); i++) {
            FieldDecl f = rec.fieldDecls.get(i);

            emit("dup");
            emit("push " + st.getFieldOffset(id, f.id));
            emit("add");

            paramList.get(i).compile(st);
            emit("store");
        }

        emit("jumpi " + done);

        emit(heapFail + ":");
        emit("push 3");
        emit("halt");

        emit(done + ":");
    }
}
