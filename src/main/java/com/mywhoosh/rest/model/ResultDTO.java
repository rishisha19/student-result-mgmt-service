package com.mywhoosh.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mywhoosh.common.Remarks;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultDTO {

    @Min(value = 1, message = "Total Marks should be greater than 0 & less than 100")
    @Max(value = 100, message = "Total Marks should be greater than 0 & less than 100")
    @JsonProperty("TotalMarks")
    int totalMarks;

    @Min(value = 1, message = "Obtained Marks should be greater than 0 & less than 100")
    @Max(value = 100, message = "Obtained Marks should be greater than 0 & less than 100")
    @JsonProperty("ObtainedMarks")
    int obtainedMarks;

    @Min(value = 1, message = "Roll number should be greater than 0 & less than 100")
    @Max(value = 100, message = "Roll number should be greater than 0 & less than 100")
    @JsonProperty("RollNumber")
    int rollNumber;

    @Min(value = 1, message = "Grade should be greater than 0 and less than 10")
    @Max(value = 10, message = "Grade should be greater than 0 and less than 10")
    @JsonProperty("Grade")
    int grade;

    @JsonProperty("Remarks")
    Remarks remarks;
    @JsonProperty("PositionInClass")
    int positionInClass;

    @JsonProperty("CreatedOn")
    public LocalDateTime createdOn;
    @JsonProperty("UpdatedOn")
    public LocalDateTime updatedOn;
}
