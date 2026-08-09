package td.cine.demo.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import td.cine.demo.dto.ReservationCreateDto;
import td.cine.demo.dto.ReservationDto;
import td.cine.demo.exception.ForbiddenException;
import td.cine.demo.exception.NotFoundException;
import td.cine.demo.model.Projection;
import td.cine.demo.model.Reservation;
import td.cine.demo.model.Seat;
import td.cine.demo.model.User;
import td.cine.demo.model.enums.UserRole;
import td.cine.demo.repository.ProjectionRepository;
import td.cine.demo.repository.ReservationRepository;
import td.cine.demo.repository.SeatRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final ProjectionRepository projectionRepository;
  private final SeatRepository seatRepository;

  public List<ReservationDto> findAll() {
    return reservationRepository.findAll().stream().map(this::toDto).toList();
  }

  public ReservationDto findByIdForUser(UUID reservationId, User requester) {
    return toDto(getReservationForUser(reservationId, requester));
  }

  public Reservation getReservationForUser(UUID reservationId, User requester) {
    Reservation reservation = getOrThrow(reservationId);

    boolean isOwner = reservation.getUser().getId().equals(requester.getId());
    boolean isStaff =
        requester.getRole() == UserRole.MANAGER || requester.getRole() == UserRole.EMPLOYEE;

    if (!isOwner && !isStaff) {
      throw new ForbiddenException("You are not allowed to access this reservation");
    }
    return reservation;
  }

  public ReservationDto create(ReservationCreateDto dto, User requester) {
    Projection projection =
        projectionRepository
            .findById(dto.projectionId())
            .orElseThrow(
                () -> new NotFoundException("Projection not found: " + dto.projectionId()));

    Set<Seat> seats =
        dto.seatIds().stream()
            .map(
                seatId ->
                    seatRepository
                        .findById(seatId)
                        .orElseThrow(() -> new NotFoundException("Seat not found: " + seatId)))
            .collect(Collectors.toSet());

    Reservation reservation = new Reservation();
    reservation.setProjection(projection);
    reservation.setUser(requester);
    reservation.setSeats(seats);
    reservation.setCreatedAt(Instant.now());
    reservationRepository.save(reservation);
    return toDto(reservation);
  }

  public ReservationDto update(UUID id, ReservationCreateDto dto) {
    Reservation reservation = getOrThrow(id);

    Projection projection =
        projectionRepository
            .findById(dto.projectionId())
            .orElseThrow(
                () -> new NotFoundException("Projection not found: " + dto.projectionId()));

    Set<Seat> seats =
        dto.seatIds().stream()
            .map(
                seatId ->
                    seatRepository
                        .findById(seatId)
                        .orElseThrow(() -> new NotFoundException("Seat not found: " + seatId)))
            .collect(Collectors.toSet());

    reservation.setProjection(projection);
    reservation.setSeats(seats);
    reservationRepository.save(reservation);
    return toDto(reservation);
  }

  private Reservation getOrThrow(UUID id) {
    return reservationRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Reservation not found: " + id));
  }

  private ReservationDto toDto(Reservation reservation) {
    Set<UUID> seatIds =
        reservation.getSeats().stream().map(Seat::getId).collect(Collectors.toSet());
    return new ReservationDto(
        reservation.getId(),
        reservation.getProjection().getId(),
        reservation.getUser().getId(),
        seatIds,
        reservation.getCreatedAt());
  }
}
