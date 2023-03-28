package com.mywhoosh.persistence.mapper;

import com.mywhoosh.persistence.entity.Result;
import com.mywhoosh.persistence.entity.Student;
import com.mywhoosh.rest.model.ResultDTO;
import com.mywhoosh.rest.model.StudentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    Student toStudentEntity(StudentDTO studentDto);
    StudentDTO toStudentDto(Student student);
    Result toResultEntity(ResultDTO resultDTO);
    ResultDTO toResultDto(Result result);
}
