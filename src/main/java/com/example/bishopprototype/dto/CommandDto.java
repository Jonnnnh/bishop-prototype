package com.example.bishopprototype.dto;

import com.example.synthetichumancore.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandDto {
    @NotBlank @Size(max = 1000)
    private String description;

    @NotNull
    private Priority priority;

    @NotBlank @Size(max = 100)
    private String author;

    @NotBlank
    private String time;
}
