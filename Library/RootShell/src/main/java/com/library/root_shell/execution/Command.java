package com.library.root_shell.execution;

import android.os.Handler;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;

import com.library.root_shell.RootShell;

import java.io.IOException;

public class Command {
    protected boolean javaCommand = false;

    protected Context context = null;

    public int totalOutput = 0;

    public int totalOutputProcessed = 0;

    ExecutionMonitor executionMonitor = null;

    Handler mHandler = null;

    //Has this command already been used?
    protected boolean used = false;

    boolean executing = false;

    String[] command;

    boolean finished = false;

    boolean terminated = false;

    boolean handlerEnabled = true;

    int exitCode = -1;

    int id;

    int timeout = RootShell.defaultCommandTimeout;

    /**
     * Constructor for executing a normal shell command
     *
     * @param id      the id of the command being executed
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, String... command) {
        this.command = command;
        this.id = id;

        createHandler(RootShell.handlerEnabled);
    }

    /**
     * Constructor for executing a normal shell command
     *
     * @param id             the id of the command being executed
     * @param handlerEnabled when true the handler will be used to call the
     *                       callback methods if possible.
     * @param command        the command, or commands, to be executed.
     */
    public Command(int id, boolean handlerEnabled, String... command) {
        this.command = command;
        this.id = id;

        createHandler(handlerEnabled);
    }

    /**
     * Constructor for executing a normal shell command
     *
     * @param id      the id of the command being executed
     * @param timeout the time allowed before the shell will give up executing the command
     *                and throw a TimeoutException.
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, int timeout, String... command) {
        this.command = command;
        this.id = id;
        this.timeout = timeout;

        createHandler(RootShell.handlerEnabled);
    }

    //If you override this you MUST make a final call
    //to the super method. The super call should be the last line of this method.
    public void commandOutput(int id, String line) {
        RootShell.log("Command", "ID: " + id + ", " + line);
        totalOutputProcessed++;
    }

    public void commandTerminated(int id, String reason) {
        //pass
    }

    public void commandCompleted(int id, int exitcode) {
        //pass
    }

    protected final void commandFinished() {
        if (!terminated) {
            synchronized (this) {
                if (mHandler != null && handlerEnabled) {
                    Message msg = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_COMPLETED);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } else {
                    commandCompleted(id, exitCode);
                }

                RootShell.log("Command " + id + " finished.");
                finishCommand();
            }
        }
    }

    private void createHandler(boolean handlerEnabled) {

        this.handlerEnabled = handlerEnabled;

        if (Looper.myLooper() != null && handlerEnabled) {
            RootShell.log("CommandHandler created");
            mHandler = new CommandHandler();
        } else {
            RootShell.log("CommandHandler not created");
        }
    }

    public final void finish() {
        RootShell.log("Command finished at users request!");
        commandFinished();
    }

    protected final void finishCommand() {
        this.executing = false;
        this.finished = true;
        this.notifyAll();
    }


    public final String getCommand() {
        StringBuilder sb = new StringBuilder();

        if (javaCommand) {
            String filePath = context.getFilesDir().getPath();

            for (String s : command) {
                /*
                 * TODO Make withFramework optional for applications
                 * that do not require access to the fw. -CFR
                 */
                sb.append("export CLASSPATH=").append(filePath).append("/anbuild.dex;").append(" app_process /system/bin ").append(s);

                sb.append('\n');
            }
        } else {
            for (String s : command) {
                sb.append(s);
                sb.append('\n');
            }
        }

        return sb.toString();
    }

    public final boolean isExecuting() {
        return executing;
    }

    public final boolean isHandlerEnabled() {
        return handlerEnabled;
    }

    public final boolean isFinished() {
        return finished;
    }

    public final int getExitCode() {
        return this.exitCode;
    }

    protected final void setExitCode(int code) {
        synchronized (this) {
            exitCode = code;
        }
    }

    protected final void startExecution() {
        this.used = true;
        executionMonitor = new ExecutionMonitor(this);
        executionMonitor.setPriority(Thread.MIN_PRIORITY);
        executionMonitor.start();
        executing = true;
    }

    public final void terminate() {
        RootShell.log("Terminating command at users request!");
        terminated("Terminated at users request!");
    }

    protected final void terminate(String reason) {
        try {
            Shell.closeAll();
            RootShell.log("Terminating all shells.");
            terminated(reason);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected final void terminated(String reason) {
        synchronized (Command.this) {
            if (mHandler != null && handlerEnabled) {
                Message msg = mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_TERMINATED);
                bundle.putString(CommandHandler.TEXT, reason);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } else {
                commandTerminated(id, reason);
            }

            RootShell.log("Command " + id + " did not finish because it was terminated. Termination reason: " + reason);
            setExitCode(-1);
            terminated = true;
            finishCommand();
        }
    }

    protected final void output(int id, String line) {
        totalOutput++;

        if (mHandler != null && handlerEnabled) {
            Message msg = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_OUTPUT);
            bundle.putString(CommandHandler.TEXT, line);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        } else {
            commandOutput(id, line);
        }
    }

    private class ExecutionMonitor extends Thread {

        private final Command command;

        public ExecutionMonitor(Command command) {
            this.command = command;
        }

        public void run() {

            if (command.timeout > 0) {
                synchronized (command) {
                    try {
                        RootShell.log("Command " + command.id + " is waiting for: " + command.timeout);
                        command.wait(command.timeout);
                    } catch (InterruptedException e) {
                        RootShell.log("Exception: " + e);
                    }

                    if (!command.isFinished()) {
                        RootShell.log("Timeout Exception has occurred for command: " + command.id + ".");
                        terminate("Timeout Exception");
                    }
                }
            }
        }
    }

    private final class CommandHandler extends Handler {

        static final public String ACTION = "action";

        static final public String TEXT = "text";

        static final public int COMMAND_OUTPUT = 0x01;

        static final public int COMMAND_COMPLETED = 0x02;

        static final public int COMMAND_TERMINATED = 0x03;

        public void handleMessage(Message msg) {
            int action = msg.getData().getInt(ACTION);
            String text = msg.getData().getString(TEXT);

            switch (action) {
                case COMMAND_OUTPUT:
                    commandOutput(id, text);
                    break;
                case COMMAND_COMPLETED:
                    commandCompleted(id, exitCode);
                    break;
                case COMMAND_TERMINATED:
                    commandTerminated(id, text);
                    break;
            }
        }
    }
}
