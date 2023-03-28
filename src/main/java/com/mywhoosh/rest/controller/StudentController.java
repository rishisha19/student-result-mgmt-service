package com.mywhoosh.rest.controller;

import com.mywhoosh.exception.StudentMgmtException;
import com.mywhoosh.rest.model.ResultDTO;
import com.mywhoosh.rest.model.StudentDTO;
import com.mywhoosh.service.ResultService;
import com.mywhoosh.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Validated
public class StudentController {

    private final StudentService studentService;
    private final ResultService resultService;

    @PostMapping
    @Operation(summary = "Add a new student")
    public Mono<ResponseEntity<StudentDTO>> addStudent(@Valid @RequestBody StudentDTO student) {
        return studentService.saveStudent(student)
                .map(savedStudent -> ResponseEntity.created(URI.create("/students" + savedStudent.getRollNumber()))
                        .body(savedStudent))
                .onErrorResume(StudentMgmtException.DuplicateRollNumberException.class, Mono::error);
    }

    @DeleteMapping
    public Mono<ResponseEntity<Void>> deleteStudent( @Valid @RequestBody StudentDTO student) {
        return studentService.deleteStudent(student).map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Get all students results")
    public Flux<ResultDTO> getAllStudentResults() {
        return resultService.getAllStudentResults();
    }



    @GetMapping("/result/{rollNumber}")
    @Operation(summary = "Get student result by roll number")
    public Mono<ResultDTO> getStudentResultByRollNumber(@PathVariable int rollNumber) {
        return resultService.getStudentResultByRollNumber(rollNumber);
    }
}
