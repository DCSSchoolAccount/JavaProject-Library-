package compile.ast;

public class Formal {

    public final Type type;
    public final String id;

    public Formal(Type type, String id) {
        this.type = type;
        this.id = id;
    }
}
