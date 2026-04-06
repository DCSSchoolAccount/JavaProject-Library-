package compile;

import compile.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {

    private final SymbolTable parent;
    private Map<String, Integer> localOffsets;
    private int nextLocalOffset;
    private int totalLocalCount;
    private List<RecDef> records;
    private List<VarDecl> globals;
    private List<Formal> parameters;
    private List<VarDecl> locals;
    private final boolean methodRoot;

    private static int freshNameCounter;

    /**
     * Initialise a new symbol table.
     *
     * @param program the program
     */
    public SymbolTable(Program program) {
        this.parent = null;
        freshNameCounter = 0;
        this.records = new ArrayList<>();
        this.globals = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.locals = new ArrayList<>();
        this.localOffsets = new HashMap<>();
        this.nextLocalOffset = -4;
        this.totalLocalCount = 0;
        this.methodRoot = false;
    }

    public static SymbolTable newMethodScope(SymbolTable parent){
        SymbolTable st = new SymbolTable(parent, true);
        st.nextLocalOffset = -4;
        st.totalLocalCount = 0;
        return st;
    }

    /**
     * Create a method/local scope symbol table, keeping access to globals.
     */
    public SymbolTable(SymbolTable parent, boolean methodRoot) {
        this.parent = parent;
        this.methodRoot = methodRoot;

        this.records = parent.records;
        this.globals = parent.globals;
        this.parameters = new ArrayList<>();
        this.locals = new ArrayList<>();
        this.localOffsets = new HashMap<>();
        if (methodRoot) {
            this.nextLocalOffset = -4;
            this.totalLocalCount = 0;
        }else{
            this.nextLocalOffset= parent.nextLocalOffset;
            this.totalLocalCount = parent.totalLocalCount;
        }
    }

    /**
     * The list of global variables declared so far.
     * @return the current list of global variable declarations
     */
    public List<VarDecl> getGlobals() {
        return List.copyOf(globals);
    }

    /**
     * Declare a new variable.
     * Keeps original behaviour: top-level declarations are globals.
     *
     * @param decl the new variable declaration
     */
    public void declareVariable(VarDecl decl) {
        declareGlobal(decl);
    }

    /**
     * Declare a new global variable.
     * @param decl the new global variable declaration
     */
    private void declareGlobal(VarDecl decl) {
        globals.add(decl);
    }

    /**
     * Declare a formal parameter.
     */
    public void declareParameter(Formal formal) {
        parameters.add(formal);
    }

    /**
     * Declare a local variable.
     */
    public void declareLocal(VarDecl decl) {
        locals.add(decl);
        localOffsets.put(decl.name,allocateLocalOffset());
    }

    /**
     * @return root symbol table of local variable
     */
    private SymbolTable localRoot() {
        if (methodRoot || parent == null) {
            return this;
        }
        return parent.localRoot();
    }

    /**
     * @return new offset counter for local variables
     */
    private int allocateLocalOffset() {
        SymbolTable root = localRoot();
        int offset = root.nextLocalOffset;
        root.nextLocalOffset -= 4;
        root.totalLocalCount++;
        return offset;
    }

    /**
     * @return count of local variables in current local scope
     */
    public int getLocalCount(){
        return localRoot().totalLocalCount;
    }

    /**
     * Check whether an identifier is a global variable.
     */
    public boolean isGlobal(String id) {
        for (VarDecl v : globals) {
            if (v.name.equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether an identifier is a parameter.
     */
    public boolean isParameter(String id) {
        for (Formal f : parameters) {
            if (f.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether an identifier is a local variable.
     */
    public boolean isLocal(String id) {
        return localOffsets.containsKey(id);
    }

    /**
     * Check whether an identifier is stored in the current stack frame.
     */
    public boolean isStackVariable(String id) {
        if (isParameter(id) || isLocal(id)) {
            return true;
        }
        return parent != null && parent.isStackVariable(id);
    }
    /**
     * @return the parameter offset relative to FP.
     * First parameter is at FP + 4, next at FP + 8, etc.
     */
    public int getParameterOffset(String id) {
        int offset = 4;
        for (Formal f : parameters) {
            if (f.id.equals(id)) {
                return offset;
            }
            offset += 4;
        }
        throw new RuntimeException("Unknown parameter: " + id);
    }

    /**
     * @return the local variable offset relative to FP.
     * First local is at FP - 4, next at FP - 8, etc.
     */
    public int getLocalOffset(String id) {
        Integer offset = localOffsets.get(id);
        if (offset != null){
            return offset;
        }
        if (parent != null){
            return parent.getLocalOffset(id);
        }
        throw new RuntimeException("Unknown local variable: " + id);
    }

    /**
     * @return The stack-frame offset for either a parameter or a local variable.
     */
    public int getStackOffset(String id) {
        if (isParameter(id)) {
            return getParameterOffset(id);
        }
        if (isLocal(id)) {
            return getLocalOffset(id);
        }
        if (parent != null) {
            return parent.getStackOffset(id);
        }
        throw new RuntimeException("Identifier is not a stack variable: " + id);
    }

    /**
     * @return Number of parameters in current method scope.
     */
    public int getParameterCount() {
        return parameters.size();
    }


    /**
     * Declare a record
     */
    public void declareRecord(RecDef rec) {
        records.add(rec);
    }

    /**
     * @return a copy of all records
     */
    public List<RecDef> getRecords(){
        return List.copyOf(records);
    }

    /**
     * @param id
     * @return Boolean check for if id points to a record
     */
    public boolean isRecordType(String id) {
        for (RecDef r : records) {
            if (r.recId.equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param id
     * @return record with matching id
     */
    public RecDef getRecord(String id) {
        for (RecDef r : records) {
            if (r.recId.equals(id)) {
                return r;
            }
        }
        throw new RuntimeException("Unknown record type: " + id);
    }

    /**
     *
     * @param recordName
     * @param fieldName
     * @return Boolean check for if a field is in a given record
     */
    public boolean hasField(String recordName, String fieldName) {
        RecDef rec = getRecord(recordName);
        for (FieldDecl f : rec.fieldDecls) {
            if (f.id.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param recordName
     * @param fieldName
     * @return field type
     */
    public Type getFieldType(String recordName, String fieldName) {
        RecDef rec = getRecord(recordName);
        for (FieldDecl field : rec.fieldDecls) {
            if (field.id.equals(fieldName)) {
                return field.type;
            }
        }
        throw new RuntimeException("Unknown field " + fieldName + " in record " + recordName);
    }

    /**
     * @param recordName
     * @param fieldName
     * @return offset for given field in record
     */

    public int getFieldOffset(String recordName, String fieldName) {
        RecDef rec = getRecord(recordName);
        int offset = 0;
        for (FieldDecl field : rec.fieldDecls) {
            if (field.id.equals(fieldName)) {
                return offset;
            }
            offset += 4;
        }
        throw new RuntimeException("Unknown field " + fieldName + " in record " + recordName);
    }

    /**
     *
     * @param id
     * @return variable type of local/parameter/parent/global
     */
    public Type getVariableType(String id) {
        for (VarDecl v : locals) {
            if (v.name.equals(id)) return v.type;
        }
        for (Formal f : parameters) {
            if (f.id.equals(id)) return f.type;
        }
        if (parent != null) {
            return parent.getVariableType(id);
        }
        for (VarDecl v : globals) {
            if (v.name.equals(id)) return v.type;
        }
        throw new RuntimeException("Unknown variable: " + id);
    }

    /**
     *
     * @param e
     * @return Expression type of variable/record/field
     */
    public Type inferExprType(Exp e) {

        // variable
        if (e instanceof ExpVar) {
            String id = ((ExpVar) e).id;
            return getVariableType(id);
        }

        // array access
        if (e instanceof ExpArrayAccess) {
            ExpArrayAccess arr = (ExpArrayAccess) e;
            Type baseType = inferExprType(arr.array);
            if (!(baseType instanceof TypeArray)) {
                throw new RuntimeException("Trying to Index a non-array");
            }
            return ((TypeArray) baseType).typeArray;
        }

        // record field access
        if (e instanceof ExpRecAccess) {
            ExpRecAccess rec = (ExpRecAccess) e;
            Type baseType = inferExprType(rec.recExp);
            if (!(baseType instanceof TypeRec)) {
                throw new RuntimeException("Trying to Field access on a non-record");
            }
            String recordName = ((TypeRec) baseType).id;
            return getFieldType(recordName, rec.fieldName);
        }

        throw new RuntimeException("Invalid expression for type inference: " + e.getClass().getSimpleName());
    }


    /**
     * @param methodId
     * @return lexically valid label for methods
     */

    public static String makeMethodLabel(String methodId) {
        return "$method_" + methodId.substring(1);
    }


        /**
         * Transform an LPL26 identifier into an SSM label.
         *
         * @param id the LPL26 identifier
         * @return id prefixed with "$"
         */
    public static String makeIdLabel(String id) {
        return "$" + id;
    }

    /**
     * Each call to this method will return a fresh label which is
     * guaranteed not to clash with any name returned by makeIdLabel(x),
     * where x is any LPL26 identifier.
     *
     * @param tag a string to include as part of the generated name.
     * @return a fresh name which is prefixed with "$$".
     */
    public String freshLabel(String tag) {
        return "$$" + tag + "_" + (freshNameCounter++);
    }
}