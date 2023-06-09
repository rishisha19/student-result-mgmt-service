package com.mywhoosh.rest.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mywhoosh.common.Status;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;


@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentDTO {

    private String id;

    @JsonProperty("Name")
    String name;
    @Min(value = 1, message = "Roll number should be greater than 0 & less than 100")
    @Max(value = 100, message = "Roll number should be greater than 0 & less than 100")
    @JsonProperty("RollNumber")
    Integer rollNumber;
    @JsonProperty("FathersName")
    String fathersName;
    @Min(value = 1, message = "Grade should be greater than 0 and less than 10")
    @Max(value = 10, message = "Grade should be greater than 0 and less than 10")
    @JsonProperty("Grade")
    Integer grade;
    Status status;
    @JsonProperty("CreatedOn")
    public LocalDateTime createdOn;
    @JsonProperty("UpdatedOn")
    public LocalDateTime updatedOn;
}
