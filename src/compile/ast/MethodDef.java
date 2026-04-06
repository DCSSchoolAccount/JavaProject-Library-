package compile.ast;

import compile.StaticAnalysisException;
import compile.SymbolTable;

import java.util.List;

public abstract class MethodDef extends AST{
    public final String name;
    public final List<Formal> formals;
    public final List<Stm> stmBlock;

    public MethodDef(String name, List<Formal> formals, List<Stm> stmBlock){
        this.name = name;
        this.formals = List.copyOf(formals);
        this.stmBlock = List.copyOf(stmBlock);
    }

    /**
     * Emit SSM assembly code which implements this statement.
     * @param st the symbol table for the program being compiled
     */
    public void compile(SymbolTable st) {
        throw new StaticAnalysisException("Compilation not implemented for " + this.getClass().getSimpleName());
    }
}
