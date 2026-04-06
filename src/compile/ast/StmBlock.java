package compile.ast;

import compile.SymbolTable;

import java.util.List;

public class StmBlock extends Stm {

    public final List<Stm> stms;

    public StmBlock(List<Stm> stms) {
        this.stms = List.copyOf(stms);
    }

    @Override
    public void compile(SymbolTable st) {
        SymbolTable blockst = new SymbolTable(st,false);
        for (Stm stm : stms) {
            if (stm instanceof VarDecl) {
                blockst.declareLocal((VarDecl) stm);
            }
        }
        for (Stm stm : stms) {
            stm.compile(blockst);
        }
    }
}
