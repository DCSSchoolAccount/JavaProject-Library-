package compile;

import compile.ast.AST;
import compile.ast.Program;
import parse.LPL26Parser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Compile an LPL26 program.  */
public class Compile {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: compile.Compile <source-file>");
            System.exit(1);
        }
        Path sourcePath = Paths.get(args[0]);
        String assemblyFileName = sourcePath.getFileName().toString().replaceFirst("\\.[^.]*$", "") + ".ssma";
        Path parentPath = sourcePath.getParent();
        Path assemblyPath = Paths.get(assemblyFileName);
        if (parentPath != null) assemblyPath = parentPath.resolve(assemblyPath);
        LPL26Parser parser = new LPL26Parser();
        Program p = parser.parse(sourcePath.toString());
        p.compile();
        AST.write(assemblyPath);
        System.out.println("Assembly code written to: " + assemblyPath);
    }
}
