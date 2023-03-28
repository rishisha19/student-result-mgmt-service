package com.mywhoosh.service;

import com.mywhoosh.exception.StudentMgmtException;
import com.mywhoosh.rest.model.StudentDTO;

public interface ValidationService {
    void validateForAddStudent(StudentDTO student) throws StudentMgmtException.StudentValidationException;
}
