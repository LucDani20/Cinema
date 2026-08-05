package td.cine.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record RoomDto(UUID id, @NotBlank String number, @Positive int capacity) {}
