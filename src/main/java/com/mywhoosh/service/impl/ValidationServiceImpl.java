package com.mywhoosh.service.impl;

import com.mywhoosh.exception.ErrorMsgs;
import com.mywhoosh.exception.StudentMgmtException;
import com.mywhoosh.rest.model.StudentDTO;
import com.mywhoosh.service.ValidationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Override
    public void validateForAddStudent(StudentDTO student) throws StudentMgmtException.StudentValidationException {
        if(StringUtils.isAllBlank(student.getName()))
            throw new StudentMgmtException.StudentValidationException(ErrorMsgs.STUDENT_NAME_REQUIRED);

        if(StringUtils.isAllBlank(student.getFathersName()))
            throw new StudentMgmtException.StudentValidationException(ErrorMsgs.STUDENT_FATHER_NAME_REQUIRED);


    }
}
