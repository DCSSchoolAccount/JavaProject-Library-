package compile.ast;

import compile.StaticAnalysisException;
import compile.SymbolTable;

/**
 * The abstract parent type for all expression AST classes.
 */
public abstract class Exp extends AST {

    /**
     * Emit SSM assembly code which implements this expression.
     * @param st the symbol table for the program being compiled
     */
    public void compile(SymbolTable st) {
        throw new StaticAnalysisException("Compilation not implemented for " + this.getClass().getSimpleName());
    }

}