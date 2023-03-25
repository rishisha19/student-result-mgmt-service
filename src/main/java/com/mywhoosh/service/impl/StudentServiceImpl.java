package com.mywhoosh.service.impl;

import com.mywhoosh.persistence.repository.ResultRepository;
import com.mywhoosh.persistence.repository.StudentRepository;
import com.mywhoosh.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final ResultRepository resultRepository;
}
