package td.cine.demo.dto;

import java.util.UUID;

public record SeatDto(UUID id, String number, UUID roomId) {}
