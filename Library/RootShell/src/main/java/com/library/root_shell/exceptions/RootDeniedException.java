package com.library.root_shell.exceptions;

public class RootDeniedException extends Exception {

    private static final long serialVersionUID = -8713947214162841310L;

    public RootDeniedException(String error) {
        super(error);
    }
}