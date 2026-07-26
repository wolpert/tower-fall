package com.codeheadsystems.towerstack.lwjgl3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes the desktop build runnable on macOS.
 *
 * <p>GLFW — which LWJGL3, and so libGDX's desktop backend, sits on — can only own the window on
 * macOS if it runs on the process's first thread. The JVM only does that when started with
 * {@code -XstartOnFirstThread}, and there is no way to set it from inside a running JVM. So on
 * macOS we relaunch ourselves once with the flag and wait for that second JVM to finish. Every
 * other platform is untouched: {@link #relaunchIfNeeded} returns false immediately.
 *
 * <p>Without this, a jar that runs fine on Linux and Windows simply dies on macOS, however it
 * is packaged — so it belongs with the distribution tasks rather than with the game.
 */
public final class StartOnFirstThreadHelper {

    /** Set on the relaunched JVM so it knows not to relaunch again. */
    static final String RELAUNCHED = "towerstack.startOnFirstThread";

    private StartOnFirstThreadHelper() {
        // Static utility.
    }

    /**
     * Relaunch this program on a first-thread JVM if the platform needs it.
     *
     * @param mainClass the class whose {@code main} should be re-entered
     * @param args      the original command-line arguments, forwarded verbatim
     * @return true if we relaunched (and the child has now exited) — the caller should return
     *         without starting the game; false to carry on in this JVM
     */
    public static boolean relaunchIfNeeded(Class<?> mainClass, String[] args) {
        if (!isMac() || Boolean.getBoolean(RELAUNCHED)) {
            return false;
        }

        List<String> command = buildCommand(mainClass, args,
                System.getProperty("java.home"), System.getProperty("java.class.path"));
        try {
            Process child = new ProcessBuilder(command).inheritIO().start();
            System.exit(child.waitFor());
            return true; // not reached; keeps the compiler and the reader happy
        } catch (Exception e) {
            // Better to try the game and let GLFW complain than to refuse to start at all.
            System.err.println("Could not relaunch on the first thread, continuing anyway: " + e);
            return false;
        }
    }

    static boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("mac");
    }

    /**
     * Build the relaunch command. Package-private and free of side effects so it can be
     * exercised without actually spawning a JVM.
     *
     * <p>When the classpath is the single executable jar we shipped, relaunch with {@code -jar}
     * so the child resolves exactly the same artifact; otherwise (a Gradle {@code run}, an IDE)
     * pass the classpath and main class through.
     */
    static List<String> buildCommand(Class<?> mainClass, String[] args,
                                     String javaHome, String classPath) {
        List<String> command = new ArrayList<>();
        command.add(javaHome + File.separator + "bin" + File.separator + "java");
        command.add("-XstartOnFirstThread");
        command.add("-D" + RELAUNCHED + "=true");

        if (isSingleJar(classPath)) {
            command.add("-jar");
            command.add(classPath);
        } else {
            command.add("-cp");
            command.add(classPath);
            command.add(mainClass.getName());
        }
        for (String arg : args) {
            command.add(arg);
        }
        return command;
    }

    private static boolean isSingleJar(String classPath) {
        return classPath != null
                && !classPath.contains(File.pathSeparator)
                && classPath.toLowerCase(java.util.Locale.ROOT).endsWith(".jar");
    }
}
