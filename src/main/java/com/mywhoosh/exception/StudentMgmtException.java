package com.mywhoosh.exception;

public class StudentMgmtException extends RuntimeException{
    public StudentMgmtException(String message) {
        super(message);
    }
    public StudentMgmtException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class StudentNotFoundException extends StudentMgmtException {
        public StudentNotFoundException() {
            super(ErrorMsgs.STUDENT_NOT_FOUND);
        }
    }
}
