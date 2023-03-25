package com.mywhoosh.persistence.entity;

import com.mywhoosh.common.Remarks;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document("results")
@Data
public class Result {

    @Id
    private String id;

    @NotNull
    private Integer totalMarks;

    @NotNull
    private Integer obtainedMarks;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer rollNumber;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer grade;

    @Enumerated(EnumType.STRING)
    private Remarks remarks;

    private Integer positionInClass;

    @CreatedDate
    @Field("createdOn")
    public LocalDateTime createdOn;
    @Field("updatedOn")
    @LastModifiedDate
    public LocalDateTime updatedOn;

    // getters and setters
}
