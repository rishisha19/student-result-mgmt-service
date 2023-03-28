package com.mywhoosh.persistence.entity;

import com.mywhoosh.common.Remarks;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document("results")
@Data
@Builder
public class Result {

    @Id
    private String id;
    @NotNull
    private int totalMarks;
    @NotNull
    private int obtainedMarks;
    @NotNull
    @Min(1)
    @Max(100)
    private int rollNumber;
    @NotNull
    @Min(1)
    @Max(10)
    private int grade;
    private Remarks remarks;
    private int positionInClass;
    @CreatedDate
    @Field("createdOn")
    public LocalDateTime createdOn;
    @Field("updatedOn")
    @LastModifiedDate
    public LocalDateTime updatedOn;

    // getters and setters
}
