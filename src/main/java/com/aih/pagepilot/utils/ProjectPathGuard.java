package com.aih.pagepilot.utils;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves a user-supplied relative path and rejects anything outside {@code root}.
 */
public final class ProjectPathGuard {

    private ProjectPathGuard() {
    }

    /**
     * Resolve {@code userPath} under {@code root}.
     *
     * @throws IllegalArgumentException if the path is blank, absolute, a Windows/UNC prefix,
     *                                  or normalizes outside {@code root}
     */
    public static Path resolveInside(Path root, String userPath) {
        if (root == null) {
            throw new IllegalArgumentException("root is required");
        }
        if (userPath == null || userPath.isBlank()) {
            throw new IllegalArgumentException("path is blank");
        }
        if (userPath.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("path contains NUL");
        }
        if (hasForbiddenPrefix(userPath)) {
            throw new IllegalArgumentException("absolute or prefixed path rejected");
        }
        try {
            Path user = Paths.get(userPath);
            if (user.isAbsolute() || hasForbiddenPrefix(user.toString())) {
                throw new IllegalArgumentException("absolute path rejected");
            }
            Path rootAbs = root.toAbsolutePath().normalize();
            Path resolved = rootAbs.resolve(user).normalize();
            if (!resolved.startsWith(rootAbs)) {
                throw new IllegalArgumentException("path escapes root");
            }
            return resolved;
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("invalid path", e);
        }
    }

    /**
     * Leading slashes, UNC, and {@code C:} prefixes can escape the root on Windows
     * even when {@link Path#isAbsolute()} is false.
     */
    private static boolean hasForbiddenPrefix(String userPath) {
        if (userPath.startsWith("/") || userPath.startsWith("\\")) {
            return true;
        }
        return userPath.length() >= 2
                && Character.isLetter(userPath.charAt(0))
                && userPath.charAt(1) == ':';
    }
}
