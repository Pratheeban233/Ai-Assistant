package com.ai.prathi.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApprovalDecisionRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 120, message = "must be 120 characters or fewer")
        String reviewerName,
        @Size(max = 500, message = "must be 500 characters or fewer")
        String note) {
}
