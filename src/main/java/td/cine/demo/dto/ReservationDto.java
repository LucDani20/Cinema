package td.cine.demo.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ReservationDto(
    UUID id, UUID projectionId, UUID userId, Set<UUID> seatIds, Instant createdAt) {}
