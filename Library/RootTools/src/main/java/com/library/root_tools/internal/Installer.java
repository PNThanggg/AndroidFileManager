package com.library.root_tools.internal;


import android.content.Context;
import android.util.Log;

import com.library.root_shell.execution.Command;
import com.library.root_shell.execution.Shell;
import com.library.root_tools.RootTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Installer {
    static final String LOG_TAG = "RootTools::Installer";

    static final String BOGUS_FILE_NAME = "bogus";

    Context context;
    String filesPath;

    public Installer(Context context) throws IOException {

        this.context = context;
        this.filesPath = context.getFilesDir().getCanonicalPath();
    }

    /**
     * This method can be used to unpack a binary from the raw resources folder and store it in
     * /data/data/app.package/files/
     * This is typically useful if you provide your own C- or C++-based binary.
     * This binary can then be executed using sendShell() and its full path.
     *
     * @param sourceId resource id; typically <code>R.raw.id</code>
     * @param destName destination file name; appended to /data/data/app.package/files/
     * @param mode     chmod value for this file
     * @return a <code>boolean</code> which indicates whether or not we were
     * able to create the new file.
     */
    protected boolean installBinary(int sourceId, String destName, String mode) {
        File mf = new File(filesPath + File.separator + destName);
        if (!mf.exists() || !getFileSignature(mf).equals(getStreamSignature(context.getResources().openRawResource(sourceId)))) {
            Log.e(LOG_TAG, "Installing a new version of binary: " + destName);
            // First, does our files/ directory even exist?
            // We cannot wait for android to lazily create it as we will soon
            // need it.
            try {
                FileInputStream fis = context.openFileInput(BOGUS_FILE_NAME);
                fis.close();
            } catch (FileNotFoundException e) {
                FileOutputStream fos = null;
                try {
                    fos = context.openFileOutput("bogus", Context.MODE_PRIVATE);
                    fos.write("justcreatedfilesdirectory".getBytes());
                } catch (Exception ex) {
                    if (RootTools.debugMode) {
                        Log.e(LOG_TAG, ex.toString());
                    }
                    return false;
                } finally {
                    if (null != fos) {
                        try {
                            fos.close();
                            context.deleteFile(BOGUS_FILE_NAME);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            } catch (IOException ex) {
                if (RootTools.debugMode) {
                    Log.e(LOG_TAG, ex.toString());
                }
                return false;
            }

            // Only now can we start creating our actual file
            InputStream iss = context.getResources().openRawResource(sourceId);
            FileOutputStream oss = null;
            try {
                oss = new FileOutputStream(mf);
                FileChannel ofc = oss.getChannel();
                long pos = 0;
                try {
                    long size = iss.available();
                } catch (IOException ex) {
                    if (RootTools.debugMode) {
                        Log.e(LOG_TAG, ex.toString());
                    }
                    return false;
                }
            } catch (FileNotFoundException ex) {
                if (RootTools.debugMode) {
                    Log.e(LOG_TAG, ex.toString());
                }
                return false;
            } finally {
                if (oss != null) {
                    try {
                        oss.flush();
                        oss.getFD().sync();
                        oss.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                iss.close();
            } catch (IOException ex) {
                if (RootTools.debugMode) {
                    Log.e(LOG_TAG, ex.toString());
                }
                return false;
            }

            try {
                Command command = new Command(0, false, "chmod " + mode + " " + filesPath + File.separator + destName);
                Shell.startRootShell().add(command);
                commandWait(command);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    protected boolean isBinaryInstalled(String destName) {
        boolean installed = false;
        File mf = new File(filesPath + File.separator + destName);
        if (mf.exists()) {
            installed = true;
        }
        return installed;
    }

    protected String getFileSignature(File f) {
        String signature = "";
        try {
            signature = getStreamSignature(new FileInputStream(f));
        } catch (FileNotFoundException ex) {
            Log.e(LOG_TAG, ex.toString());
        }
        return signature;
    }

    /*
     * Note: this method will close any string passed to it
     */
    protected String getStreamSignature(InputStream is) {
        String signature = "";
        try (is) {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();

            for (byte b : digest) {
                sb.append(Integer.toHexString(b & 0xFF));
            }

            signature = sb.toString();
        } catch (IOException | NoSuchAlgorithmException ex) {
            Log.e(LOG_TAG, ex.toString());
        }
        return signature;
    }

    private void commandWait(Command cmd) {
        synchronized (cmd) {
            try {
                if (!cmd.isFinished()) {
                    cmd.wait(2000);
                }
            } catch (InterruptedException ex) {
                Log.e(LOG_TAG, ex.toString());
            }
        }
    }
}