package com.aih.pagepilot.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectPathGuardTest {

    @TempDir
    Path tempDir;

    @Test
    void acceptsRelativePathInsideRoot() {
        Path resolved = ProjectPathGuard.resolveInside(tempDir, "src/App.vue");
        Path rootAbs = tempDir.toAbsolutePath().normalize();
        assertTrue(resolved.startsWith(rootAbs));
        assertEquals(rootAbs.resolve("src").resolve("App.vue").normalize(), resolved);
    }

    @Test
    void rejectsParentEscape() {
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(tempDir, "../secret.txt"));
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(tempDir, "foo/../../outside.txt"));
    }

    @Test
    void rejectsAbsoluteUnixPath() {
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(tempDir, "/etc/passwd"));
    }

    @Test
    void rejectsWindowsDriveAndUnc() {
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(tempDir, "C:\\Windows\\win.ini"));
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(tempDir, "C:foo"));
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(tempDir, "\\\\server\\share\\file"));
    }

    @Test
    void rejectsBlankAndNull() {
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(tempDir, "  "));
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(tempDir, ""));
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(tempDir, null));
        assertThrows(IllegalArgumentException.class,
                () -> ProjectPathGuard.resolveInside(null, "a.txt"));
    }
}
