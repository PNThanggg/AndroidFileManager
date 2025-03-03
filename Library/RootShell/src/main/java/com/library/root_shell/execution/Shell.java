package com.library.root_shell.execution;


import android.content.Context;

import androidx.annotation.NonNull;

import com.library.root_shell.RootShell;
import com.library.root_shell.exceptions.RootDeniedException;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public final class Shell {
    public enum ShellType {
        NORMAL, ROOT, CUSTOM
    }

    //this is only used with root shells
    public enum ShellContext {
        NORMAL("normal"), //The normal context...
        SHELL("u:r:shell:s0"), //unprivileged shell (such as an adb shell)
        SYSTEM_SERVER("u:r:system_server:s0"), // system_server, u:r:system:s0 on some firmwares
        SYSTEM_APP("u:r:system_app:s0"), // System apps
        PLATFORM_APP("u:r:platform_app:s0"), // System apps
        UNTRUSTED_APP("u:r:untrusted_app:s0"), // Third-party apps
        RECOVERY("u:r:recovery:s0"), //Recovery
        SUPERSU("u:r:supersu:s0"); //SUPER SU default

        private final String value;

        ShellContext(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }

    //Statics -- visible to all
    private static final String token = "F*D^W@#FGF";

    private static Shell rootShell = null;

    private static Shell shell = null;

    private static Shell customShell = null;

    private static final String[] suVersion = new String[]{null, null};

    //the default context for root shells...
    public static ShellContext defaultContext = ShellContext.NORMAL;

    //per shell
    private int shellTimeout = 25000;

    private final ShellType shellType;

    private final ShellContext shellContext;

    private String error = "";

    private final Process proc;

    private final BufferedReader inputStream;

    private final BufferedReader errorStream;

    private final OutputStreamWriter outputStream;

    private final List<Command> commands = new ArrayList<>();

    //indicates whether or not to close the shell
    private boolean close = false;

    private Boolean isSELinuxEnforcing = null;

    public boolean isExecuting = false;

    public boolean isReading = false;

    public boolean isClosed = false;

    private final int maxCommands = 5000;

    private int read = 0;

    private int write = 0;

    private int totalExecuted = 0;

    private int totalRead = 0;

    private Shell(String cmd, ShellType shellType, ShellContext shellContext, int shellTimeout) throws IOException, TimeoutException, RootDeniedException {

        RootShell.log("Starting shell: " + cmd);
        RootShell.log("Context: " + shellContext.getValue());
        RootShell.log("Timeout: " + shellTimeout);

        this.shellType = shellType;
        this.shellTimeout = shellTimeout > 0 ? shellTimeout : this.shellTimeout;
        this.shellContext = shellContext;

        if (this.shellContext != ShellContext.NORMAL) {
            String display = getSuVersion(false);
            String internal = getSuVersion(true);

            //only done for root shell...
            //Right now only SUPERSU supports the --context switch
            if (isSELinuxEnforcing() && (display != null) && (internal != null) && (display.endsWith("SUPERSU")) && (Integer.parseInt(internal) >= 190)) {
                cmd += " --context " + this.shellContext.getValue();
            } else {
                RootShell.log("Su binary --context switch not supported!");
                RootShell.log("Su binary display version: " + display);
                RootShell.log("Su binary internal version: " + internal);
                RootShell.log("SELinuxEnforcing: " + isSELinuxEnforcing());
            }

        }
        this.proc = Runtime.getRuntime().exec(cmd);

        this.inputStream = new BufferedReader(new InputStreamReader(this.proc.getInputStream(), StandardCharsets.UTF_8));
        this.errorStream = new BufferedReader(new InputStreamReader(this.proc.getErrorStream(), StandardCharsets.UTF_8));
        this.outputStream = new OutputStreamWriter(this.proc.getOutputStream(), StandardCharsets.UTF_8);

        Worker worker = new Worker(this);
        worker.start();

        try {
            worker.join(this.shellTimeout);

            if (worker.exit == -911) {
                try {
                    this.proc.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                closeQuietly(this.inputStream);
                closeQuietly(this.errorStream);
                closeQuietly(this.outputStream);

                throw new TimeoutException(this.error);
            } else if (worker.exit == -42) {

                try {
                    this.proc.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                closeQuietly(this.inputStream);
                closeQuietly(this.errorStream);
                closeQuietly(this.outputStream);

                throw new RootDeniedException("Root Access Denied");
            } else {
                Thread si = getThread(shellContext);
                si.start();

                Runnable output = new Runnable() {
                    public void run() {
                        try {
                            Command command = null;

                            //as long as there is something to read, we will keep reading.
                            while (!close || inputStream.ready() || read < commands.size()) {
                                isReading = false;
                                String outputLine = inputStream.readLine();
                                isReading = true;

                                if (outputLine == null) {
                                    break;
                                }

                                if (command == null) {
                                    if (read >= commands.size()) {
                                        if (close) {
                                            break;
                                        }

                                        continue;
                                    }

                                    command = commands.get(read);
                                }

                                int pos = outputLine.indexOf(token);
                                if (pos == -1) {
                                    command.output(command.id, outputLine);
                                } else if (pos > 0) {
                                    RootShell.log("Found token, line: " + outputLine);
                                    command.output(command.id, outputLine.substring(0, pos));
                                }

                                if (pos >= 0) {
                                    outputLine = outputLine.substring(pos);
                                    String[] fields = outputLine.split(" ");

                                    if (fields.length >= 2 && fields[1] != null) {
                                        int id = 0;

                                        try {
                                            id = Integer.parseInt(fields[1]);
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }

                                        int exitCode = -1;

                                        try {
                                            exitCode = Integer.parseInt(fields[2]);
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }

                                        if (id == totalRead) {
                                            processErrors(command);

                                            int iterations = 0;
                                            while (command.totalOutput > command.totalOutputProcessed) {

                                                if (iterations == 0) {
                                                    iterations++;
                                                    RootShell.log("Waiting for output to be processed. " + command.totalOutputProcessed + " Of " + command.totalOutput);
                                                }

                                                try {
                                                    synchronized (this) {
                                                        this.wait(2000);
                                                    }
                                                } catch (Exception e) {
                                                    RootShell.log(e.getMessage());
                                                }
                                            }

                                            RootShell.log("Read all output");

                                            command.setExitCode(exitCode);
                                            command.commandFinished();

                                            command = null;

                                            read++;
                                            totalRead++;
                                        }
                                    }
                                }
                            }

                            try {
                                proc.waitFor();
                                proc.destroy();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            while (read < commands.size()) {
                                if (command == null) {
                                    command = commands.get(read);
                                }

                                if (command.totalOutput < command.totalOutputProcessed) {
                                    command.terminated("All output not processed!");
                                    command.terminated("Did you forget the super.commandOutput call or are you waiting on the command object?");
                                } else {
                                    command.terminated("Unexpected Termination.");
                                }

                                command = null;
                                read++;
                            }

                            read = 0;

                        } catch (IOException e) {
                            RootShell.log(e.getMessage(), RootShell.LogLevel.ERROR, e);
                        } finally {
                            closeQuietly(outputStream);
                            closeQuietly(errorStream);
                            closeQuietly(inputStream);

                            RootShell.log("Shell destroyed");
                            isClosed = true;
                            isReading = false;
                        }
                    }
                };
                Thread so = new Thread(output, "Shell Output");
                so.setPriority(Thread.NORM_PRIORITY);
                so.start();
            }
        } catch (InterruptedException ex) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw new TimeoutException();
        }
    }

    @NonNull
    private Thread getThread(ShellContext shellContext) {
        Runnable input = () -> {
            try {
                while (true) {
                    synchronized (commands) {
                        while (!close && write >= commands.size()) {
                            isExecuting = false;
                            commands.wait();
                        }
                    }

                    if (write >= maxCommands) {
                        while (read != write) {
                            RootShell.log("Waiting for read and write to catch up before cleanup.");
                        }

                        cleanCommands();
                    }

                    if (write < commands.size()) {
                        isExecuting = true;
                        Command cmd1 = commands.get(write);

                        if (null != cmd1) {
                            cmd1.startExecution();
                            RootShell.log("Executing: " + cmd1.getCommand() + " with context: " + shellContext);

                            //write the command
                            outputStream.write(cmd1.getCommand());
                            outputStream.flush();

                            //write the token...
                            String line = "\necho " + token + " " + totalExecuted + " $?\n";
                            outputStream.write(line);
                            outputStream.flush();

                            write++;
                            totalExecuted++;
                        }
                    } else if (close) {
                        isExecuting = false;
                        outputStream.write("\nexit 0\n");
                        outputStream.flush();
                        RootShell.log("Closing shell");
                        return;
                    }
                }
            } catch (IOException | InterruptedException e) {
                RootShell.log(e.getMessage(), RootShell.LogLevel.ERROR, e);
            } finally {
                write = 0;
                closeQuietly(outputStream);
            }
        };
        Thread si = new Thread(input, "Shell Input");
        si.setPriority(Thread.NORM_PRIORITY);
        return si;
    }


    public Command add(Command command) {
        if (this.close) {
            throw new IllegalStateException("Unable to add commands to a closed shell");
        }

        if (command.used) {
            //The command has been used, don't re-use...
            throw new IllegalStateException("This command has already been executed. (Don't re-use command instances.)");
        }

        this.commands.add(command);

        this.notifyThreads();

        return command;
    }

    public final void useCWD(Context context) {
        add(new Command(-1, false, "cd " + context.getApplicationInfo().dataDir));
    }

    private void cleanCommands() {
        int toClean = Math.abs(this.maxCommands - (this.maxCommands / 4));
        RootShell.log("Cleaning up: " + toClean);

        this.commands.subList(0, toClean).clear();

        this.read = this.commands.size() - 1;
        this.write = this.commands.size() - 1;
    }

    private void closeQuietly(final Reader input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeQuietly(final Writer output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        RootShell.log("Request to close shell!");

        int count = 0;
        while (isExecuting) {
            RootShell.log("Waiting on shell to finish executing before closing...");
            count++;

            //fail safe
            if (count > 10000) {
                break;
            }

        }

        synchronized (this.commands) {
            this.close = true;
            this.notifyThreads();
        }

        RootShell.log("Shell Closed!");

        if (this == Shell.rootShell) {
            Shell.rootShell = null;
        } else if (this == Shell.shell) {
            Shell.shell = null;
        } else if (this == Shell.customShell) {
            Shell.customShell = null;
        }
    }

    public static void closeCustomShell() throws IOException {
        RootShell.log("Request to close custom shell!");

        if (Shell.customShell == null) {
            return;
        }

        Shell.customShell.close();
    }

    public static void closeRootShell() {
        RootShell.log("Request to close root shell!");

        if (Shell.rootShell == null) {
            return;
        }
        Shell.rootShell.close();
    }

    public static void closeShell() throws IOException {
        RootShell.log("Request to close normal shell!");

        if (Shell.shell == null) {
            return;
        }
        Shell.shell.close();
    }

    public static void closeAll() throws IOException {
        RootShell.log("Request to close all shells!");

        Shell.closeShell();
        Shell.closeRootShell();
        Shell.closeCustomShell();
    }

    public int getCommandQueuePosition(Command cmd) {
        return this.commands.indexOf(cmd);
    }

    public String getCommandQueuePositionString(Command cmd) {
        return "Command is in position " + getCommandQueuePosition(cmd) + " currently executing command at position " + this.write + " and the number of commands is " + commands.size();
    }

    public static Shell getOpenShell() {
        if (Shell.customShell != null) {
            return Shell.customShell;
        } else if (Shell.rootShell != null) {
            return Shell.rootShell;
        } else {
            return Shell.shell;
        }
    }

    /**
     * From libsuperuser.
     *
     * <p>
     * Detects the version of the su binary installed (if any), if supported
     * by the binary. Most binaries support two different version numbers,
     * the public version that is displayed to users, and an internal
     * version number that is used for version number comparisons. Returns
     * null if su not available or retrieving the version isn't supported.
     * </p>
     * <p>
     * Note that su binary version and GUI (APK) version can be completely
     * different.
     * </p>
     * <p>
     * This function caches its result to improve performance on multiple
     * calls
     * </p>
     *
     * @param internal Request human-readable version or application
     *                 internal version
     * @return String containing the su version or null
     */
    private synchronized String getSuVersion(boolean internal) {
        int idx = internal ? 0 : 1;
        if (suVersion[idx] == null) {
            String version = null;

            // Replace libsuperuser:Shell.run with manual process execution
            Process process;
            try {
                process = Runtime.getRuntime().exec(internal ? "su -V" : "su -v", null);
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return null;
            }

            // From libsuperuser:StreamGobbler
            List<String> stdout = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // make sure our stream is closed and resources will be freed
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            process.destroy();

            for (String line : stdout) {
                if (!internal) {
                    if (line.contains(".")) {
                        version = line;
                        break;
                    }
                } else {
                    try {
                        if (Integer.parseInt(line) > 0) {
                            version = line;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            suVersion[idx] = version;
        }
        return suVersion[idx];
    }

    public static boolean isShellOpen() {
        return Shell.shell == null;
    }

    public static boolean isCustomShellOpen() {
        return Shell.customShell == null;
    }

    public static boolean isRootShellOpen() {
        return Shell.rootShell == null;
    }

    public static boolean isAnyShellOpen() {
        return Shell.shell != null || Shell.rootShell != null || Shell.customShell != null;
    }

    public synchronized boolean isSELinuxEnforcing() {
        if (isSELinuxEnforcing == null) {
            boolean enforcing = false;
            File f = new File("/sys/fs/selinux/enforce");
            if (f.exists()) {
                try {
                    try (InputStream is = new FileInputStream("/sys/fs/selinux/enforce")) {
                        enforcing = is.read() == '1';
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            isSELinuxEnforcing = enforcing;
        }
        return isSELinuxEnforcing;
    }

    protected void notifyThreads() {
        Thread t = new Thread() {
            public void run() {
                synchronized (commands) {
                    commands.notifyAll();
                }
            }
        };

        t.start();
    }

    public void processErrors(Command command) {
        try {
            while (errorStream.ready() && command != null) {
                String line = errorStream.readLine();
                if (line == null) {
                    break;
                }

                command.output(command.id, line);
            }
        } catch (Exception e) {
            RootShell.log(e.getMessage(), RootShell.LogLevel.ERROR, e);
        }
    }

    public static Command runRootCommand(Command command) throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell().add(command);
    }

    public static Command runCommand(Command command) throws IOException, TimeoutException {
        return Shell.startShell().add(command);
    }

    public static Shell startRootShell() throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell(0, 3);
    }

    public static Shell startRootShell(int timeout) throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell(timeout, 3);
    }

    public static Shell startRootShell(int timeout, int retry) throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell(timeout, Shell.defaultContext, retry);
    }

    public static Shell startRootShell(int timeout, ShellContext shellContext, int retry) throws IOException, TimeoutException, RootDeniedException {
        // keep prompting the user until they accept for x amount of times...
        int retries = 0;

        if (Shell.rootShell == null) {

            RootShell.log("Starting Root Shell!");
            String cmd = "su";
            while (Shell.rootShell == null) {
                try {
                    RootShell.log("Trying to open Root Shell, attempt #" + retries);
                    Shell.rootShell = new Shell(cmd, ShellType.ROOT, shellContext, timeout);
                } catch (IOException e) {
                    if (retries++ >= retry) {
                        RootShell.log("IOException, could not start shell");
                        throw e;
                    }
                } catch (RootDeniedException e) {
                    if (retries++ >= retry) {
                        RootShell.log("RootDeniedException, could not start shell");
                        throw e;
                    }
                } catch (TimeoutException e) {
                    if (retries++ >= retry) {
                        RootShell.log("TimeoutException, could not start shell");
                        throw e;
                    }
                }
            }
        } else if (Shell.rootShell.shellContext != shellContext) {
            try {
                RootShell.log("Context is different than open shell, switching context... " + Shell.rootShell.shellContext + " VS " + shellContext);
                Shell.rootShell.switchRootShellContext(shellContext);
            } catch (IOException e) {
                if (retries++ >= retry) {
                    RootShell.log("IOException, could not switch context!");
                    throw e;
                }
            } catch (RootDeniedException e) {
                if (retries++ >= retry) {
                    RootShell.log("RootDeniedException, could not switch context!");
                    throw e;
                }
            } catch (TimeoutException e) {
                if (retries++ >= retry) {
                    RootShell.log("TimeoutException, could not switch context!");
                    throw e;
                }
            }
        } else {
            RootShell.log("Using Existing Root Shell!");
        }

        return Shell.rootShell;
    }

    public static Shell startCustomShell(String shellPath) throws IOException, TimeoutException, RootDeniedException {
        return Shell.startCustomShell(shellPath, 0);
    }

    public static Shell startCustomShell(String shellPath, int timeout) throws IOException, TimeoutException, RootDeniedException {

        if (Shell.customShell == null) {
            RootShell.log("Starting Custom Shell!");
            Shell.customShell = new Shell(shellPath, ShellType.CUSTOM, ShellContext.NORMAL, timeout);
        } else {
            RootShell.log("Using Existing Custom Shell!");
        }

        return Shell.customShell;
    }

    public static Shell startShell() throws IOException, TimeoutException {
        return Shell.startShell(0);
    }

    public static Shell startShell(int timeout) throws IOException, TimeoutException {

        try {
            if (Shell.shell == null) {
                RootShell.log("Starting Shell!");
                Shell.shell = new Shell("/system/bin/sh", ShellType.NORMAL, ShellContext.NORMAL, timeout);
            } else {
                RootShell.log("Using Existing Shell!");
            }
            return Shell.shell;
        } catch (RootDeniedException e) {
            //Root Denied should never be thrown.
            throw new IOException();
        }
    }

    public void switchRootShellContext(ShellContext shellContext) throws IOException, TimeoutException, RootDeniedException {
        if (this.shellType == ShellType.ROOT) {
            try {
                Shell.closeRootShell();
            } catch (Exception e) {
                RootShell.log("Problem closing shell while trying to switch context...");
            }

            Shell.startRootShell(this.shellTimeout, shellContext, 3);
        } else {
            RootShell.log("Can only switch context on a root shell!");
        }
    }

    protected static class Worker extends Thread {

        public int exit = -911;

        public Shell shell;

        private Worker(Shell shell) {
            this.shell = shell;
        }

        public void run() {
            try {
                shell.outputStream.write("echo Started\n");
                shell.outputStream.flush();

                while (true) {
                    String line = shell.inputStream.readLine();

                    if (line == null) {
                        throw new EOFException();
                    } else if (line.isEmpty()) {
                        continue;
                    } else if ("Started".equals(line)) {
                        this.exit = 1;
                        setShellOom();
                        break;
                    }

                    shell.error = "unknown error occurred.";
                }
            } catch (IOException e) {
                exit = -42;
                if (e.getMessage() != null) {
                    shell.error = e.getMessage();
                } else {
                    shell.error = "RootAccess denied?.";
                }
            }

        }

        /*
         * setOom for shell processes (sh and su if root shell) and discard outputs
         * Negative values make the process LESS likely to be killed in an OOM situation
         * Positive values make the process MORE likely to be killed in an OOM situation
         */
        private void setShellOom() {
            try {
                Class<?> processClass = shell.proc.getClass();
                Field field;
                try {
                    field = processClass.getDeclaredField("pid");
                } catch (NoSuchFieldException e) {
                    field = processClass.getDeclaredField("id");
                }
                field.setAccessible(true);
                int pid = (Integer) field.get(shell.proc);
                shell.outputStream.write("(echo -17 > /proc/" + pid + "/oom_adj) &> /dev/null\n");
                shell.outputStream.write("(echo -17 > /proc/$$/oom_adj) &> /dev/null\n");
                shell.outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
