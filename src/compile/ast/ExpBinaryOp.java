package compile.ast;

import compile.SymbolTable;

public class ExpBinaryOp extends Exp {

    public final String operator;
    public final Exp left, right;

    public ExpBinaryOp(String operator, Exp left, Exp right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public void compile(SymbolTable st) {
        left.compile(st);
        right.compile(st);
        switch (operator) {
            case "==":
                emit("sub", "test_z");
                return;
            case "/":
                emit("div");
                return;
            case "+":
                emit("add");
                return;
            case "-":
                emit("sub");
                return;
            case "*":
                emit("mul");
                return;
            case "<=":
                emit("sub", "dup", "test_z", "swap", "test_n", "add", "test_z", "test_z");
                return;
            case "<":
                emit("sub", "test_n");
                return;
            case ">":
                emit("swap", "sub", "test_n");
                return;
            case ">=":
                emit("swap", "sub", "dup", "test_z", "swap", "test_n", "add", "test_z", "test_z");
                return;
            case "&&":
                emit("mul","test_z","test_z");
                return;
            case "||":
                emit( "test_z", "test_z", "swap", "test_z", "test_z", "add", "test_z", "test_z");
                return;
            default:
                throw new IllegalStateException("Unrecognised binary operator: " + operator);
        }
    }

}
