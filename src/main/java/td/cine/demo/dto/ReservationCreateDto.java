package td.cine.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record ReservationCreateDto(@NotNull UUID projectionId, @NotEmpty Set<UUID> seatIds) {}
