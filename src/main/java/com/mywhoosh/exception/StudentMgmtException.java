package com.mywhoosh.exception;

import org.slf4j.helpers.MessageFormatter;

public class StudentMgmtException extends RuntimeException{
    public StudentMgmtException(String message) {
        super(message);
    }
    public StudentMgmtException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class InvalidRequestException extends StudentMgmtException {
        public InvalidRequestException(String message) {
            super(message);
        }
    }

    public static class StudentValidationException extends StudentMgmtException{
        public StudentValidationException(String message) {
            super(message);
        }
    }

    public static class StudentNotFoundException extends StudentMgmtException {
        public StudentNotFoundException(int rollNumber) {
            super(MessageFormatter.format(ErrorMsgs.STUDENT_NOT_FOUND, rollNumber).getMessage());
        }
    }

    public static class NoResultFoundForStudent extends StudentMgmtException {
        public NoResultFoundForStudent(int rollNumber) {
            super(MessageFormatter.format(ErrorMsgs.NO_RESULT_FOUND_FOR_STUDENT, rollNumber).getMessage());
        }
    }

    public static class DuplicateRollNumberException extends StudentMgmtException {
        public DuplicateRollNumberException() {
            super(ErrorMsgs.DUPLICATE_ROLL_NUMBER);
        }
    }
}
