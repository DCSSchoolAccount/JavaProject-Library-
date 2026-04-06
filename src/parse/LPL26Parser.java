package parse;

import compile.ast.*;
import sbnf.ParseException;
import sbnf.lex.Lexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Parse an LPL26 program and build its AST.  */
public class LPL26Parser {

    /**
     * Path to an SBNF file containing the relevant token definitions.
     */
    public static final String SBNF_FILE = "data/LPL26.sbnf";

    private Lexer lex;

    /**
     * Initialise a new LPL26 parser.
     */
    public LPL26Parser() {
        lex = new Lexer(SBNF_FILE);
    }

    public Program parse(String sourcePath) throws IOException {
        lex.readFile(sourcePath);
        lex.next();
        Program prog = Program();
        if (!lex.tok().isType("EOF")) {
            throw new ParseException(lex.tok(), "EOF");
        }
        return prog;
    }

    /**
     Program -> RecordDef* BEGIN Stm* END MethodDef*
     */
    public Program Program() {

        List<RecDef> records = new ArrayList<>();
        while (lex.tok().isType("RECDEF")){
            records.add(RecordDef());
        }

        lex.eat("BEGIN");

        List<Stm> body = new ArrayList<>();
        while (!lex.tok().isType("END")) {
            body.add(Stm());
        }

        lex.eat("END");

        List<MethodDef> methods = new ArrayList<>();
        while (lex.tok().isType("FUN") || lex.tok().isType("PROC")) {
            methods.add(MethodDef());
        }

        return new Program(records, body, methods);
    }

    /**
     Stm -> PRINT LBR Exp RBR SEMIC
     Stm -> PRINTLN LBR OptionalExp RBR SEMIC
     Stm -> PRINTCH LBR Exp RBR SEMIC
     Stm -> WHILE LBR Exp RBR Stm
     Stm -> IF LBR Exp RBR Stm ELSE Stm
     Stm -> LCBR Stm* RCBR
     Stm -> METHOD_ID LBR OptionalParamList RBR SEMIC
     Stm -> VarDecl
     Stm -> FREE LBR Exp RBR SEMIC
     Stm -> LExpr ASSIGN Exp SEMIC
     */
    private Stm Stm() {
        switch (lex.tok().type) {
            case "PRINT": {
                lex.next();
                lex.eat("LBR");
                Exp e = Exp();
                lex.eat("RBR");
                lex.eat("SEMIC");
                return new StmPrint(e);
            }
            case "PRINTLN": {
                lex.next();
                lex.eat("LBR");
                Exp e = OptionalExp();
                lex.eat("RBR");
                lex.eat("SEMIC");
                return new StmPrintln(e);
            }
            case "PRINTCH": {
                lex.next();
                lex.eat("LBR");
                Exp e =Exp();
                lex.eat("RBR");
                lex.eat("SEMIC");
                return new StmPrintCh(e);
            }
            case "WHILE": {
                lex.next();
                lex.eat("LBR");
                Exp cond = Exp();
                lex.eat("RBR");
                Stm body = Stm();
                return new StmWhile(cond, body);
            }
            case "IF": {
                lex.next();
                lex.eat("LBR");
                Exp cond = Exp();
                lex.eat("RBR");
                Stm trueBranch = Stm();
                lex.eat("ELSE");
                Stm falseBranch = Stm();
                return new StmIf(cond, trueBranch, falseBranch);
            }
            case "LCBR": {
                lex.next();
                List<Stm> stms = new ArrayList<>();
                while (!lex.tok().isType("RCBR")) {
                    stms.add(Stm());
                }
                lex.next();
                return new StmBlock(stms);
            }
            case "METHOD_ID": {
                String methodName = lex.eat("METHOD_ID");
                lex.eat("LBR");
                List<Exp> paramList = OptionalParameterList();
                lex.eat("RBR");
                lex.eat("SEMIC");
                return new StmMethodCall(methodName,paramList);
            }
            case "INT_TYPE":
            case "REC": {
                return VarDecl();
            }
            case "FREE": {
                lex.eat("FREE");
                lex.eat("LBR");
                Exp e = Exp();
                lex.eat("RBR");
                lex.eat("SEMIC");
                return new StmFree(e);
            }
            case "ID": {
                Exp lhs = LExpr();
                lex.eat("ASSIGN");
                Exp rhs = Exp();
                lex.eat("SEMIC");
                return new StmAssign(lhs, rhs);
            }

            default:
                throw new ParseException(lex.tok(), "PRINT","PRINTLN","PRINTCH","WHILE","IF","LCBR","METHOD_ID","FREE","ID");
        }
    }

    /**
     VarDecl -> Type ID OptionalInitialiser SEMIC
     */
    private VarDecl VarDecl() {
        Type t = Type();
        String id = lex.eat("ID");
        Exp init = OptionalInitialiser();
        lex.eat("SEMIC");
        return new VarDecl(t, id,init);
    }

    /**
     OptionalExp -> Exp
     OptionalExp ->
     */
    private Exp OptionalExp() {
        switch (lex.tok().type) {
            case "ID":
            case "INTLIT":
            case "NULL":
            case "NOT":
            case "METHOD_ID":
            case "NEW":
            case "LENGTH":
            case "LBR":
                return Exp();
            default:
                return null;
        }
    }


    /**
     ParamList -> Exp AnotherParam*
     AnotherParam -> COMMA Exp
     */
    private List<Exp> ParamList(){
        List<Exp> params = new ArrayList<>();
        params.add(Exp());

        while (lex.tok().isType("COMMA")){
            lex.eat("COMMA");
            params.add(Exp());
        }
        return params;
    }

    /**
     OptionalParamList -> ParamList
     OptionalParamList ->
     */
    private List<Exp> OptionalParameterList(){
        switch (lex.tok().type) {
            case "ID":
            case "INTLIT":
            case "NULL":
            case "NOT":
            case "METHOD_ID":
            case "NEW":
            case "LENGTH":
            case "LBR":
                return ParamList();
            default:
                return new ArrayList<>();
        }
    }

    /**
     OptionalInitialiser -> ASSIGN Exp
     OptionalInitialiser ->
     */
    private Exp OptionalInitialiser() {
        if (lex.tok().isType("ASSIGN")) {
            lex.next();
            return Exp();
        } else {
            return null;
        }
    }

    /**
      Exp -> SimpleExp OperatorClause
     */
    private Exp Exp() {
        Exp e1 = SimpleExp();
        return OperatorClause(e1);
    }

    /**
      LExpr -> ID Accessor*
      Accessor -> LSQBR Exp RSQBR
      Accessor -> DOT ID
     */
    private Exp LExpr() {
        String id = lex.eat("ID");
        Exp e = new ExpVar(id);
        while (lex.tok().isType("LSQBR")||lex.tok().isType("DOT")) {

                if (lex.tok().isType("LSQBR")) {
                    while (lex.tok().isType("LSQBR")) {
                        lex.next();
                        Exp index = Exp();
                        lex.eat("RSQBR");
                        e = new ExpArrayAccess(e, index);
                    }
                }
                else if (lex.tok().isType("DOT")) {
                    while (lex.tok().isType("DOT")) {
                    lex.next();
                    String recId = lex.eat("ID");
                    e = new ExpRecAccess(e, recId);
                }
            }
        }
        return e;
    }

    /**
      SimpleExp -> LExpr
      SimpleExp -> INTLIT
      SimpleExp -> NULL
      SimpleExp -> NOT SimpleExp
      SimpleExp -> METHOD_ID LBR OptionalParamList RBR
      SimpleExp -> NEW ObjectInit
      SimpleExp -> LENGTH LBR Exp RBR
      SimpleExp -> LBR Exp RBR
     */
    private Exp SimpleExp() {
        switch (lex.tok().type) {
            case "ID": {
                return LExpr();
            }

            case "INTLIT": {
                int n = Integer.parseInt(lex.eat("INTLIT"));
                return new ExpInt(n);
            }

            case "NULL":{
                lex.next();
                return new ExpNull();
            }

            case "NOT": {
                lex.eat("NOT");
                Exp e = SimpleExp();
                return new ExpUnaryOp("!", e);
            }

            case "METHOD_ID": {
                String id = lex.eat("METHOD_ID");
                lex.eat("LBR");
                List<Exp> params = OptionalParameterList();
                lex.eat("RBR");
                return new ExpMethodCall(id,params);
            }

            case "NEW": {
                lex.next();
                return ObjectInit();
            }

            case "LENGTH": {
                lex.next();
                lex.eat("LBR");
                Exp e = Exp();
                lex.eat("RBR");
                return new ExpLength(e);
            }

            case "LBR": {
                lex.next();
                Exp e = Exp();
                lex.eat("RBR");
                return e;
            }

            default:
                throw new ParseException(lex.tok(), "ID", "INTLIT", "NULL", "NOT", "METHOD_ID", "NEW", "LENGTH", "LBR");
        }
    }

    /**
      ObjectInit -> ID LBR ParamList RBR
      ObjectInit -> LBR Type RBR LSQBR Exp RSQBR
     */
    private Exp ObjectInit() {
        switch (lex.tok().type) {
            case "ID": {
                String id = lex.eat("ID");
                lex.eat("LBR");
                List<Exp> paramList = ParamList();
                lex.eat("RBR");
                return new RecInit(id,paramList);
            }
            case "LBR": {
                lex.next();
                Type t = Type();
                lex.eat("RBR");
                lex.eat("LSQBR");
                Exp size = Exp();
                lex.eat("RSQBR");
                return new ExpNewArray(t, size);
            }
            default:
                throw new ParseException(lex.tok(), "ID", "LBR");
        }


    }

    /**
      OperatorClause -> BINARY_OP SimpleExp
      OperatorClause -> QM Exp COLON Exp
      OperatorClause ->
     */
    private Exp OperatorClause(Exp e) {
        switch (lex.tok().type) {
            case "BINARY_OP": {
                String operator = lex.eat("BINARY_OP");
                return new ExpBinaryOp(operator, e, SimpleExp());
            }
            case "QM": {
                lex.eat("QM");
                Exp thenExp = Exp();
                lex.eat("COLON");
                Exp elseExp = Exp();
                return new ExpConditional(e, thenExp, elseExp);
            }
            default:
                return e;
        }
    }

    /**
      MethodDef -> FUN Type METHOD_ID LBR Formal RBR LCBR VarDecl* Stm* RETURN Exp SEMIC RCBR
      MethodDef -> PROC METHOD_ID LBR Formal RBR LCBR VarDecl* Stm* RCBR
     */
    private MethodDef MethodDef(){

        switch (lex.tok().type){
            case "FUN":
                lex.next();
                Type returnType =  Type();
                String funcName = lex.eat("METHOD_ID");
                lex.eat("LBR");
                List<Formal> funcFormals = Formals();
                lex.eat("RBR");
                lex.eat("LCBR");
                List<Stm> funcStms = new ArrayList<>();
                while (!lex.tok().isType("RETURN")){
                    funcStms.add(Stm());
                }
                lex.eat("RETURN");
                Exp rtn = Exp();
                lex.eat("SEMIC");
                lex.eat("RCBR");
                return new MethodDefFun(returnType, funcName, funcFormals, funcStms,rtn);
            case "PROC":
                lex.next();
                String procName = lex.eat("METHOD_ID");
                lex.eat("LBR");
                List<Formal> procFormals = Formals();
                lex.eat("RBR");
                lex.eat("LCBR");
                List<Stm> procStms = new ArrayList<>();
                while (!lex.tok().isType("RCBR")){
                    procStms.add(Stm());
                }
                lex.eat("RCBR");
                return new MethodDefProc(procName,procFormals,procStms);
            default:
                throw new ParseException(lex.tok(), "FUN", "PROC");
        }
    }

    /**
      Formal -> Type ID AnotherFormal*
      Formal ->
     AnotherFormal -> COMMA Type ID
     */
    private List<Formal> Formals(){
        List<Formal> formals = new ArrayList<>();

        if (IsType()) {
            Type type = Type();
            String id = lex.eat("ID");
            formals.add(new Formal(type,id));

            while (lex.tok().isType("COMMA")) {
                lex.eat("COMMA");
                Type anotherType = Type();
                String anotherId = lex.eat("ID");
                formals.add(new Formal(anotherType,anotherId));
            }
        }
        return formals;
    }


    /**
      Type -> INT_TYPE ArrayBrackets*
      Type -> REC LBR ID RBR ArrayBrackets*
     ArrayBrackets -> LSQBR RSQBR

     */
    private Type Type() {
        switch (lex.tok().type) {
            case "INT_TYPE": {
                lex.next();
                Type t = new TypeInt();

                while (lex.tok().isType("LSQBR")) {
                    lex.eat("LSQBR");
                    lex.eat("RSQBR");
                    t = new TypeArray(t);
                }
                return t;
            }
            case "REC": {
                lex.next();
                lex.eat("LBR");
                Type t = new TypeRec(lex.eat("ID"));
                lex.eat("RBR");
                while (lex.tok().isType("LSQBR")) {
                    lex.eat("LSQBR");
                    lex.eat("RSQBR");
                    t = new TypeArray(t);
                }
                return t;

            }
            default: throw new ParseException(lex.tok(), "INT_TYPE","REC");
        }
    }

    /**
      RecordDef -> RECDEF ID LBR FieldDecls RBR SEMIC
     */

    private RecDef RecordDef(){
        lex.eat("RECDEF");
        String recId = lex.eat("ID");
        lex.eat("LBR");
        List<FieldDecl> fieldDecls = FieldDecls();
        lex.eat("RBR");
        lex.eat("SEMIC");
        return new RecDef(recId,fieldDecls);
    }

    /**
      FieldDecls -> Type ID AnotherField*
      AnotherField -> COMMA Type ID
     */

    private List<FieldDecl> FieldDecls(){
        List<FieldDecl> fieldDecls = new ArrayList<>();
        Type type = Type();
        String id = lex.eat("ID");
        fieldDecls.add(new FieldDecl(type,id));
        while (lex.tok().isType("COMMA")){
            lex.eat("COMMA");
            Type anotherType = Type();
            String anotherId = lex.eat("ID");
            fieldDecls.add(new FieldDecl(anotherType, anotherId));
        }
        return fieldDecls;
    }

    private Boolean IsType(){
        return lex.tok().isType("INT_TYPE")||lex.tok().isType("REC");
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: parse.LPL26Parser <source-file>");
            System.exit(1);
        }
        System.out.println("Lexing with token defs from file " + SBNF_FILE);
        parse.LPL26Parser parser = new parse.LPL26Parser();
        System.out.println("Parsing source file " + args[0]);
        parser.parse(args[0]);
        System.out.println("... parse succeeded.");
    }
}
