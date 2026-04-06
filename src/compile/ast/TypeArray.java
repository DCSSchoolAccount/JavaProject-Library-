package compile.ast;

public class TypeArray extends Type {
    public final Type typeArray;

    public TypeArray(Type t) {
        this.typeArray = t;
    }
}
