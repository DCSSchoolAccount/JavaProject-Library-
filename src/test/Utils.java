package test;

// Maven: org.junit.jupiter:junit-jupiter:5.9.3

import compile.ast.AST;
import compile.ast.Program;
import compile.StaticAnalysisException;
import parse.LPL26Parser;
import sbnf.ParseException;
import stackmachine.assembler.Assembler;
import stackmachine.machine.SSM;

import java.io.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Utils {

    public static final String TEST_FILES_ROOT = "data/tests";

    /**
     * Parse, compile, assemble and execute an LPL26 program.
     *
     * @param sout the standard output of the program will be appended here
     * @param eout the error output of the program will be appended here
     * @param sourceFilePath path to the source-code
     * @return the return status of the program when executed
     * @throws IOException
     */
    public static int exec(OutputStream sout, OutputStream eout, String sourceFilePath) throws IOException {
        LPL26Parser parser = new LPL26Parser();
        compile.ast.Program program = parser.parse(sourceFilePath);
        return exec(sout, eout, program);
    }

    /**
     * Compile, assemble and execute an LPL26 program.
     *
     * @param sout the standard output of the program will be appended here
     * @param eout the error output of the program will be appended here
     * @param program the AST of the program to be executed
     * @return the return status of the program when executed
     * @throws IOException
     */
    public static int exec(OutputStream sout, OutputStream eout, Program program) throws IOException {
        File ssmaFile = File.createTempFile("LPL26testing-", ".ssma");
        File ssmFile = File.createTempFile("LPL26testing-", ".ssm");
        try {
            program.compile();
        } finally {
            AST.write(ssmaFile.toPath());
        }
        Assembler ass = new Assembler();
        boolean verbose = false;
        String charsetName = null;
        ass.assemble(ssmaFile.getAbsolutePath(), charsetName, ssmFile.getAbsolutePath(), verbose);
        try (PrintStream psout = new PrintStream(sout); PrintStream peout = new PrintStream(eout);) {
            boolean ranmem = true;
            String ssmFilePath = ssmFile.getAbsolutePath();
            SSM ssm = new SSM(ssmFilePath, new String[]{}, ranmem);
            ssm.setOut(psout);
            ssm.setErr(peout);
            int returnStatus = ssm.run();
            psout.flush();
            peout.flush();
            return returnStatus;
        }
    }

    /**
     * Read the expected return-status from the final comment in an
     * LPL26 source file. Defaults to zero if the final non-blank line
     * in the file is *not* a comment consisting of a single integer literal.
     *
     * @param sourceFilePath a path to the test file
     * @return the expected return status for this test
     * @throws IOException
     */
    public static int readExpectedReturnStatus(String sourceFilePath) throws IOException {
        String finalLine = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath))) {
            String line = reader.readLine();
            while (line != null) {
                if (!line.isBlank()) finalLine = line;
                line = reader.readLine();
            }
        }
        int expectedStatus = 0;
        finalLine = finalLine.strip();
        if (finalLine.startsWith("//")) {
            String comment = finalLine.substring(2).strip();
            try {
                expectedStatus = Integer.parseInt(comment);
            } catch (NumberFormatException e) {}
        }
        return expectedStatus;
    }

    /**
     * Read the expected output from the initial comment block in an
     * LPL26 source file.
     * Note that this assumes line-structured output using native line
     * endings (as produced by SSM syscall OUT_LN). It is not appropriate
     * for testing of programs intended to produce binary output.
     *
     * @param sourceFilePath a path to the test file
     * @return the expected output for this test
     * @throws IOException
     */
    public static String readExpectedOutput(String sourceFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath))) {
            String expected = "";
            String line = reader.readLine();
            if (line != null) {
                if (line.startsWith("//")) {
                    expected += line.substring(2);
                    line = reader.readLine();
                } else {
                    line = null;
                }
            }
            while (line != null) {
                if (line.startsWith("//")) {
                    expected += System.lineSeparator() + line.substring(2);
                    line = reader.readLine();
                } else {
                    break;
                }
            }
            return expected;
        }
    }

    /**
     * Extract the expected result and return status from a test-program file, then
     * compile, assemble and execute the test-program, asserting
     * the expected result.
     * @param testFilePath path to the test-program
     * @throws IOException
     */
    public static void compile(String testFilePath) throws IOException {
        String expectedOutput = readExpectedOutput(testFilePath);
        int expectedReturnStatus = readExpectedReturnStatus(testFilePath);
        try (ByteArrayOutputStream sout = new ByteArrayOutputStream();
             ByteArrayOutputStream eout = new ByteArrayOutputStream();) {
            int actualReturnStatus = exec(sout, eout, testFilePath);
            String actualOutput = sout.toString();
            actualOutput = actualOutput.replaceAll("\\R", System.lineSeparator());
            if (expectedReturnStatus == 0) {
                actualOutput = actualOutput + eout;
            }
            assertEquals(expectedOutput, actualOutput);
            assertEquals("Termination status: " + expectedReturnStatus,
                    "Termination status: " + actualReturnStatus,
                    "Wrong SSM termination status");
        }
    }

    /**
     * Run a parser test. Each test consists of two files, one containing a
     * syntactically valid LPL program, and a "mutant twin" where a syntax
     * error has been deliberately introduced. The mutant twin should
     * have the same file name but with the suffix ".mutant" appended.
     * @param testFilePath a path to a syntactically valid LPL source file
     * @return "ok" if the valid input is accepted by LPLParser and its mutant
     * twin is rejected; returns "mutant file was not rejected" if both files
     * are accepted by LPLParser
     * @throws IOException
     */
    public static String parse(String testFilePath) throws IOException {
        LPL26Parser parser = new LPL26Parser();
        parser.parse(testFilePath);
        parser = new LPL26Parser();
        try {
            parser.parse(testFilePath + ".mutant");
            return "mutant file was not rejected";
        } catch (ParseException e) {
            return "ok";
        }
    }

    /**
     * List the path names of all the "valid input" test files in a directory.
     * @param dir a path to the directory
     * @return a stream of path names for all the files in the given directory,
     * excluding any sub-directories or files with names ending in ".mutant".
     */
    public static Stream<String> testFilePaths(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(f -> !f.isDirectory())
                .filter(f -> !f.getName().endsWith(".mutant"))
                .filter(f -> !f.getName().endsWith(".ssma"))
                .filter(f -> !f.getName().endsWith(".out"))
                .map(f -> dir + "/" + f.getName())
                .sorted();
    }
}
