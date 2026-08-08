package td.cine.demo.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import td.cine.demo.dto.ReservationCreateDto;
import td.cine.demo.dto.ReservationDto;
import td.cine.demo.exception.ForbiddenException;
import td.cine.demo.exception.NotFoundException;
import td.cine.demo.model.Movie;
import td.cine.demo.model.Projection;
import td.cine.demo.model.Reservation;
import td.cine.demo.model.Room;
import td.cine.demo.model.Seat;
import td.cine.demo.model.User;
import td.cine.demo.model.enums.Genre;
import td.cine.demo.model.enums.UserRole;
import td.cine.demo.repository.ProjectionRepository;
import td.cine.demo.repository.ReservationRepository;
import td.cine.demo.repository.SeatRepository;
import td.cine.demo.service.ReservationService;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

  @Mock private ReservationRepository reservationRepository;
  @Mock private ProjectionRepository projectionRepository;
  @Mock private SeatRepository seatRepository;

  @InjectMocks private ReservationService reservationService;

  private User owner;
  private User otherClient;
  private User employee;
  private User manager;
  private Reservation reservation;
  private Projection projection;

  @BeforeEach
  void setUp() {
    owner = buildUser(UserRole.CLIENT);
    otherClient = buildUser(UserRole.CLIENT);
    employee = buildUser(UserRole.EMPLOYEE);
    manager = buildUser(UserRole.MANAGER);

    Room room = new Room();
    room.setId(UUID.randomUUID());
    room.setNumber("A");
    room.setCapacity(100);

    Movie movie = new Movie();
    movie.setId(UUID.randomUUID());
    movie.setTitle("Dune");
    movie.setGenre(Genre.SCI_FI);
    movie.setDescription("desc");
    movie.setDuration(Duration.ofMinutes(150));

    projection = new Projection();
    projection.setId(UUID.randomUUID());
    projection.setMovie(movie);
    projection.setRoom(room);
    projection.setDatetime(Instant.now());
    projection.setSeatPrice(BigDecimal.TEN);

    reservation = new Reservation();
    reservation.setId(UUID.randomUUID());
    reservation.setUser(owner);
    reservation.setProjection(projection);
    reservation.setSeats(Set.of());
    reservation.setCreatedAt(Instant.now());
  }

  private User buildUser(UserRole role) {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setFirstName("First");
    user.setLastName("Last");
    user.setBirthdate(LocalDate.of(2000, 1, 1));
    user.setEmail(role.name().toLowerCase() + "-" + UUID.randomUUID() + "@test.com");
    user.setPassword("encoded");
    user.setPhone("0340000000");
    user.setRole(role);
    return user;
  }

  private Seat buildSeat() {
    Seat seat = new Seat();
    seat.setId(UUID.randomUUID());
    seat.setNumber("A1");
    seat.setRoom(projection.getRoom());
    return seat;
  }

  @Test
  void findByIdForUser_ownerCanAccessOwnReservation() {
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

    ReservationDto dto = reservationService.findByIdForUser(reservation.getId(), owner);

    assertThat(dto.id()).isEqualTo(reservation.getId());
  }

  @Test
  void findByIdForUser_anotherClientCannotAccessSomeoneElseReservation() {
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

    assertThatThrownBy(() -> reservationService.findByIdForUser(reservation.getId(), otherClient))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  void findByIdForUser_employeeCanAccessAnyReservation() {
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

    ReservationDto dto = reservationService.findByIdForUser(reservation.getId(), employee);

    assertThat(dto.id()).isEqualTo(reservation.getId());
  }

  @Test
  void findByIdForUser_managerCanAccessAnyReservation() {
    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

    ReservationDto dto = reservationService.findByIdForUser(reservation.getId(), manager);

    assertThat(dto.id()).isEqualTo(reservation.getId());
  }

  @Test
  void findByIdForUser_throwsNotFoundWhenReservationDoesNotExist() {
    UUID missingId = UUID.randomUUID();
    when(reservationRepository.findById(missingId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reservationService.findByIdForUser(missingId, owner))
        .isInstanceOf(NotFoundException.class);
  }

  /**
   * @Test void getReservationForUser_returnsEntityForOwner() {
   * when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
   *
   * <p>Reservation result = reservationService.getReservationForUser(reservation.getId(), owner);
   *
   * <p>assertThat(result.getId()).isEqualTo(reservation.getId()); } @Test void
   * getReservationForUser_forbiddenForAnotherClient() {
   * when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
   *
   * <p>assertThatThrownBy( () -> reservationService.getReservationForUser(reservation.getId(),
   * otherClient)) .isInstanceOf(ForbiddenException.class); } @Test void
   * getReservationForUser_okForStaff() {
   * when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
   *
   * <p>Reservation result = reservationService.getReservationForUser(reservation.getId(), manager);
   *
   * <p>assertThat(result.getId()).isEqualTo(reservation.getId()); }
   */
  @Test
  void create_savesReservationForRequester() {
    Seat seat = buildSeat();
    ReservationCreateDto createDto =
        new ReservationCreateDto(projection.getId(), Set.of(seat.getId()));

    when(projectionRepository.findById(projection.getId())).thenReturn(Optional.of(projection));
    when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
    when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

    ReservationDto dto = reservationService.create(createDto, owner);

    assertThat(dto.userId()).isEqualTo(owner.getId());
    assertThat(dto.seatIds()).containsExactly(seat.getId());
    verify(reservationRepository, times(1)).save(any(Reservation.class));
  }

  @Test
  void create_throwsNotFoundWhenProjectionMissing() {
    UUID missingProjectionId = UUID.randomUUID();
    ReservationCreateDto createDto =
        new ReservationCreateDto(missingProjectionId, Set.of(UUID.randomUUID()));

    when(projectionRepository.findById(missingProjectionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reservationService.create(createDto, owner))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void create_throwsNotFoundWhenSeatMissing() {
    UUID missingSeatId = UUID.randomUUID();
    ReservationCreateDto createDto =
        new ReservationCreateDto(projection.getId(), Set.of(missingSeatId));

    when(projectionRepository.findById(projection.getId())).thenReturn(Optional.of(projection));
    when(seatRepository.findById(missingSeatId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reservationService.create(createDto, owner))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void update_updatesExistingReservation() {
    Seat seat = buildSeat();
    ReservationCreateDto updateDto =
        new ReservationCreateDto(projection.getId(), Set.of(seat.getId()));

    when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
    when(projectionRepository.findById(projection.getId())).thenReturn(Optional.of(projection));
    when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
    when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

    ReservationDto dto = reservationService.update(reservation.getId(), updateDto);

    assertThat(dto.seatIds()).containsExactly(seat.getId());
  }

  @Test
  void findAll_returnsAllReservations() {
    when(reservationRepository.findAll()).thenReturn(List.of(reservation));

    var result = reservationService.findAll();

    assertThat(result).hasSize(1);
  }
}
