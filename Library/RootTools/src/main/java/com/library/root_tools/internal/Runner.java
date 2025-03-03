package com.library.root_tools.internal;


import android.content.Context;
import android.util.Log;

import com.library.root_shell.execution.Command;
import com.library.root_shell.execution.Shell;
import com.library.root_tools.RootTools;

import java.io.IOException;

public class Runner extends Thread {

    private static final String LOG_TAG = "RootTools::Runner";

    Context context;
    String binaryName;
    String parameter;

    public Runner(Context context, String binaryName, String parameter) {
        this.context = context;
        this.binaryName = binaryName;
        this.parameter = parameter;
    }

    public void run() {
        String privateFilesPath = null;
        try {
            privateFilesPath = context.getFilesDir().getCanonicalPath();
        } catch (IOException e) {
            if (RootTools.debugMode) {
                Log.e(LOG_TAG, "Problem occured while trying to locate private files directory!");
            }
            e.printStackTrace();
        }

        if (privateFilesPath != null) {
            try {
                Command command = new Command(0, false, privateFilesPath + "/" + binaryName + " " + parameter);
                Shell.startRootShell().add(command);
                commandWait(command);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void commandWait(Command cmd) {
        synchronized (cmd) {
            try {
                if (!cmd.isFinished()) {
                    cmd.wait(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}