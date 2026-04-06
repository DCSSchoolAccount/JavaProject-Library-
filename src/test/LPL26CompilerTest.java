package test;

// Maven: org.junit.jupiter:junit-jupiter:5.9.3

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;


@Timeout(value = 1000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
class LPL26CompilerTest {

    @ParameterizedTest
    @MethodSource("testFilePathsA")
    void compileA(String testFilePath) throws IOException {
        Utils.compile(testFilePath);
    }

    @ParameterizedTest
    @MethodSource("testFilePathsB")
    void compileB(String testFilePath) throws IOException {
        Utils.compile(testFilePath);
    }

    @ParameterizedTest
    @MethodSource("testFilePathsC")
    void compileC(String testFilePath) throws IOException {
        Utils.compile(testFilePath);
    }

    @ParameterizedTest
    @MethodSource("testFilePathsD")
    void compileD(String testFilePath) throws IOException {
        Utils.compile(testFilePath);
    }

    @ParameterizedTest
    @MethodSource("testFilePathsE")
    void compileE(String testFilePath) throws IOException {
        Utils.compile(testFilePath);
    }

    private static Stream<String> testFilePathsA() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/compiler/A");
    }

    private static Stream<String> testFilePathsB() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/compiler/B");
    }

    private static Stream<String> testFilePathsC() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/compiler/C");
    }

    private static Stream<String> testFilePathsD() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/compiler/D");
    }

    private static Stream<String> testFilePathsE() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/compiler/E");
    }

}