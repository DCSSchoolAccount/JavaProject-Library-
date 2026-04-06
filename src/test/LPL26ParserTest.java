package test;

// Maven: org.junit.jupiter:junit-jupiter:5.9.3


import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

@Timeout(value = 500, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
class LPL26ParserTest {

    @ParameterizedTest
    @MethodSource("testFilePathsA")
    void parseA(String testFilePath) throws IOException {
        assertEquals("ok", Utils.parse(testFilePath));
    }

    @ParameterizedTest
    @MethodSource("testFilePathsB")
    void parseB(String testFilePath) throws IOException {
        assertEquals("ok", Utils.parse(testFilePath));
    }

    @ParameterizedTest
    @MethodSource("testFilePathsC")
    void parseC(String testFilePath) throws IOException {
        assertEquals("ok", Utils.parse(testFilePath));
    }

    @ParameterizedTest
    @MethodSource("testFilePathsD")
    void parseD(String testFilePath) throws IOException {
        assertEquals("ok", Utils.parse(testFilePath));
    }

    @ParameterizedTest
    @MethodSource("testFilePathsE")
    void parseE(String testFilePath) throws IOException {
        assertEquals("ok", Utils.parse(testFilePath));
    }

    private static Stream<String> testFilePathsA() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/parser/A");
    }

    private static Stream<String> testFilePathsB() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/parser/B");
    }

    private static Stream<String> testFilePathsC() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/parser/C");
    }

    private static Stream<String> testFilePathsD() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/parser/D");
    }

    private static Stream<String> testFilePathsE() {
        return Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/parser/E");
    }
}