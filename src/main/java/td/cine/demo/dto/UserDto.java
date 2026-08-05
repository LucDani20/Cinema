package td.cine.demo.dto;

import java.time.LocalDate;
import java.util.UUID;
import td.cine.demo.model.enums.UserRole;

public record UserDto(
    UUID id,
    String firstName,
    String lastName,
    LocalDate birthdate,
    String email,
    String phone,
    UserRole role) {}
