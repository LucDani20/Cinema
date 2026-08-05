package td.cine.demo.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import td.cine.demo.dto.ReservationCreateDto;
import td.cine.demo.dto.ReservationDto;
import td.cine.demo.model.User;
import td.cine.demo.service.ReservationService;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;

  @GetMapping
  @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE')")
  public ResponseEntity<List<ReservationDto>> findAll() {
    return ResponseEntity.ok(reservationService.findAll());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE', 'MANAGER')")
  public ResponseEntity<ReservationDto> findById(
      @PathVariable UUID id, @AuthenticationPrincipal User requester) {
    return ResponseEntity.ok(reservationService.findByIdForUser(id, requester));
  }

  @PostMapping
  @PreAuthorize("hasRole('CLIENT')")
  public ResponseEntity<ReservationDto> create(
      @Valid @RequestBody ReservationCreateDto dto, @AuthenticationPrincipal User requester) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(reservationService.create(dto, requester));
  }

  // PUT /reservation : 403 pour CLIENT, 200 pour EMPLOYEE et MANAGER
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
  public ResponseEntity<ReservationDto> update(
      @PathVariable UUID id, @Valid @RequestBody ReservationCreateDto dto) {
    return ResponseEntity.ok(reservationService.update(id, dto));
  }
}
