package compile.ast;

import compile.StaticAnalysisException;
import compile.SymbolTable;

import java.util.List;

public class RecDef extends AST{

    public final String recId;
    public final List<FieldDecl> fieldDecls;
    public RecDef(String recId, List<FieldDecl> fieldDecls) {
        this.recId = recId;
        this.fieldDecls = fieldDecls;
    }


    /**
     * Emit SSM assembly code which implements this statement.
     * @param st the symbol table for the program being compiled
     */
    public void compile(SymbolTable st) {
        throw new StaticAnalysisException("Compilation not implemented for " + this.getClass().getSimpleName());
    }
}
