package compile.ast;

public class FieldDecl extends AST {
    public final Type type;
    public final String id;
    public FieldDecl(Type type, String id) {
        this.type = type;
        this.id = id;
    }
}
