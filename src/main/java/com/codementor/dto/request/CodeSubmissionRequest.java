package com.codementor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeSubmissionRequest {

    @NotBlank(message = "Code cannot be empty")
    @Size(min = 10, message = "Code is too short to analyze")
    @Size(max = 10000, message = "Code exceeds maximum allowed length of 10000 characters")
    private String code;

    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
}