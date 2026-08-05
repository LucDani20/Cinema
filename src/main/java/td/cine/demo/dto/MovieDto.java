package td.cine.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import td.cine.demo.model.enums.Genre;

public record MovieDto(
    UUID id,
    @NotBlank String title,
    @NotNull Genre genre,
    String description,
    @Positive long durationMinutes) {}
