package td.cine.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RegisterRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull LocalDate birthdate,
    @Email @NotBlank String email,
    @NotBlank String password,
    String phone) {}
