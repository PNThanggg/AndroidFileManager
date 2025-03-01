package com.library.icon_loader_lib.util;

/**
 * Extension of closeable which does not throw an exception
 */
public interface SafeCloseable extends AutoCloseable {

    @Override
    void close();
}
