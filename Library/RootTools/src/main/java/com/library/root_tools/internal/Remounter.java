package com.library.root_tools.internal;

import com.library.root_shell.execution.Command;
import com.library.root_shell.execution.Shell;
import com.library.root_tools.Constants;
import com.library.root_tools.RootTools;
import com.library.root_tools.containers.Mount;

import java.io.File;
import java.util.ArrayList;

public final class Remounter {
    public boolean remount(String file, String mountType) {
        //if the path has a trailing slash get rid of it.
        if (file.endsWith("/") && !file.equals("/")) {
            file = file.substring(0, file.lastIndexOf("/"));
        }
        //Make sure that what we are trying to remount is in the mount list.
        boolean foundMount = false;

        while (!foundMount) {
            try {
                for (Mount mount : RootTools.getMounts()) {
                    RootTools.log(mount.getMountPoint().toString());

                    if (file.equals(mount.getMountPoint().toString())) {
                        foundMount = true;
                        break;
                    }
                }
            } catch (Exception e) {
                if (RootTools.debugMode) {
                    e.printStackTrace();
                }
                return false;
            }
            if (!foundMount) {
                try {
                    file = (new File(file).getParent());
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        Mount mountPoint = findMountPointRecursive(file);

        if (mountPoint != null) {

            RootTools.log(Constants.TAG, "Remounting " + mountPoint.getMountPoint().getAbsolutePath() + " as " + mountType.toLowerCase());
            final boolean isMountMode = mountPoint.getFlags().contains(mountType.toLowerCase());

            if (!isMountMode) {
                //grab an instance of the internal class
                try {
                    Command command = new Command(0, true, "busybox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(), "busybox mount -o remount," + mountType.toLowerCase() + " " + file, "busybox mount -o " + mountType.toLowerCase() + ",remount " + mountPoint.getDevice().getAbsolutePath(), "busybox mount -o " + mountType.toLowerCase() + ",remount " + file, "toolbox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(), "toolbox mount -o remount," + mountType.toLowerCase() + " " + file, "toybox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(), "toolbox mount -o " + mountType.toLowerCase() + ",remount " + mountPoint.getDevice().getAbsolutePath(), "toolbox mount -o " + mountType.toLowerCase() + ",remount " + file, "mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(), "mount -o remount," + mountType.toLowerCase() + " " + file, "mount -o " + mountType.toLowerCase() + ",remount " + mountPoint.getDevice().getAbsolutePath(), "mount -o " + mountType.toLowerCase() + ",remount " + file, "toybox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(), "toybox mount -o remount," + mountType.toLowerCase() + " " + file, "toybox mount -o " + mountType.toLowerCase() + ",remount " + mountPoint.getDevice().getAbsolutePath(), "toybox mount -o " + mountType.toLowerCase() + ",remount " + file);
                    Shell.startRootShell().add(command);
                    commandWait(command);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                mountPoint = findMountPointRecursive(file);
            }

            if (mountPoint != null) {
                RootTools.log(Constants.TAG, mountPoint.getFlags() + " AND " + mountType.toLowerCase());
                if (mountPoint.getFlags().contains(mountType.toLowerCase())) {
                    RootTools.log(mountPoint.getFlags().toString());
                    return true;
                } else {
                    RootTools.log(mountPoint.getFlags().toString());
                    return false;
                }
            } else {
                RootTools.log("mount is null, file was: " + file + " mountType was: " + mountType);
            }
        } else {
            RootTools.log("mount is null, file was: " + file + " mountType was: " + mountType);
        }

        return false;
    }

    private Mount findMountPointRecursive(String file) {
        try {
            ArrayList<Mount> mounts = RootTools.getMounts();

            for (File path = new File(file); path != null; ) {
                for (Mount mount : mounts) {
                    if (mount.getMountPoint().equals(path)) {
                        return mount;
                    }
                }
            }

            return null;

        } catch (Exception e) {
            if (RootTools.debugMode) {
                e.printStackTrace();
            }
        }

        return null;
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
