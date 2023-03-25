package com.mywhoosh.persistence.entity;

import com.mywhoosh.common.Status;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "student")
@Data
@Builder
public class Student {

    @Id
    private String id;

    @NotBlank
    private String name;

    @NotNull
    private Integer rollNumber;

    @NotBlank
    private String fathersName;

    @NotNull
    private Integer grade;
    @Enumerated(EnumType.STRING)
    private Status status;
    @CreatedDate
    @Field("createdOn")
    public LocalDateTime createdOn;
    @Field("updatedOn")
    @LastModifiedDate
    public LocalDateTime updatedOn;

}
