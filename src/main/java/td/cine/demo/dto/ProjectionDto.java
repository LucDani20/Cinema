package td.cine.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProjectionDto(
    UUID id,
    @NotNull UUID movieId,
    @NotNull UUID roomId,
    @NotNull Instant datetime,
    @Positive BigDecimal seatPrice) {}
