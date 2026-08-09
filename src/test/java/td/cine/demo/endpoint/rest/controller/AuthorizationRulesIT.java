package td.cine.demo.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import td.cine.demo.conf.FacadeIT;
import td.cine.demo.model.Movie;
import td.cine.demo.model.Projection;
import td.cine.demo.model.Reservation;
import td.cine.demo.model.Room;
import td.cine.demo.model.Seat;
import td.cine.demo.model.User;
import td.cine.demo.model.enums.Genre;
import td.cine.demo.model.enums.UserRole;
import td.cine.demo.repository.MovieRepository;
import td.cine.demo.repository.ProjectionRepository;
import td.cine.demo.repository.ReservationRepository;
import td.cine.demo.repository.RoomRepository;
import td.cine.demo.repository.SeatRepository;
import td.cine.demo.repository.UserRepository;
import td.cine.demo.security.JwtService;

class AuthorizationRulesIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtService jwtService;
  @Autowired private RoomRepository roomRepository;
  @Autowired private MovieRepository movieRepository;
  @Autowired private ProjectionRepository projectionRepository;
  @Autowired private ReservationRepository reservationRepository;
  @Autowired private SeatRepository seatRepository;

  private String clientToken;
  private String otherClientToken;
  private String employeeToken;
  private String managerToken;
  private Projection projection;
  private Reservation clientReservation;
  private Seat seat;

  @BeforeEach
  void setUp() {
    User client = persistUser(UserRole.CLIENT);
    User otherClient = persistUser(UserRole.CLIENT);
    User employee = persistUser(UserRole.EMPLOYEE);
    User manager = persistUser(UserRole.MANAGER);

    clientToken = jwtService.generateToken(client);
    otherClientToken = jwtService.generateToken(otherClient);
    employeeToken = jwtService.generateToken(employee);
    managerToken = jwtService.generateToken(manager);

    Room room = new Room();
    room.setNumber("A1");
    room.setCapacity(50);
    roomRepository.save(room);

    seat = new Seat();
    seat.setNumber("A1");
    seat.setRoom(room);
    seatRepository.save(seat);

    Movie movie = new Movie();
    movie.setTitle("Dune");
    movie.setGenre(Genre.SCI_FI);
    movie.setDescription("desc");
    movie.setDuration(Duration.ofMinutes(150));
    movieRepository.save(movie);

    projection = new Projection();
    projection.setMovie(movie);
    projection.setRoom(room);
    projection.setDatetime(Instant.now());
    projection.setSeatPrice(BigDecimal.TEN);
    projectionRepository.save(projection);

    clientReservation = new Reservation();
    clientReservation.setUser(client);
    clientReservation.setProjection(projection);
    clientReservation.setSeats(Set.of());
    clientReservation.setCreatedAt(Instant.now());
    reservationRepository.save(clientReservation);
  }

  private User persistUser(UserRole role) {
    User user = new User();
    user.setFirstName("First");
    user.setLastName("Last");
    user.setBirthdate(LocalDate.of(2000, 1, 1));
    user.setEmail(role.name().toLowerCase() + "-" + UUID.randomUUID() + "@test.com");
    user.setPassword(passwordEncoder.encode("password123"));
    user.setPhone("0340000000");
    user.setRole(role);
    return userRepository.save(user);
  }

  private HttpEntity<Void> authEntity(String token) {
    HttpHeaders headers = new HttpHeaders();
    if (token != null) {
      headers.setBearerAuth(token);
    }
    return new HttpEntity<>(headers);
  }

  @Test
  void putMovies_forbiddenForClient() {
    ResponseEntity<String> response =
        restTemplate.exchange("/movies", HttpMethod.PUT, movieBody(clientToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void putMovies_forbiddenForEmployee() {
    ResponseEntity<String> response =
        restTemplate.exchange("/movies", HttpMethod.PUT, movieBody(employeeToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void putMovies_okForManager() {
    ResponseEntity<String> response =
        restTemplate.exchange("/movies", HttpMethod.PUT, movieBody(managerToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private HttpEntity<String> movieBody(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body =
        "{\"title\":\"Dune 2\",\"genre\":\"SCI_FI\",\"description\":\"d\",\"durationMinutes\":140}";
    return new HttpEntity<>(body, headers);
  }

  @Test
  void getProjections_okForAnonymous() {
    ResponseEntity<String> response =
        restTemplate.exchange("/projections", HttpMethod.GET, authEntity(null), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void putProjections_forbiddenForClient() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/projections", HttpMethod.PUT, projectionBody(clientToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void putProjections_forbiddenForEmployee() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/projections", HttpMethod.PUT, projectionBody(employeeToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void putProjections_okForManager() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/projections", HttpMethod.PUT, projectionBody(managerToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private HttpEntity<String> projectionBody(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body =
        String.format(
            "{\"movieId\":\"%s\",\"roomId\":\"%s\",\"datetime\":\"%s\",\"seatPrice\":15}",
            projection.getMovie().getId(), projection.getRoom().getId(), Instant.now());
    return new HttpEntity<>(body, headers);
  }

  @Test
  void getReservations_forbiddenForClient() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations", HttpMethod.GET, authEntity(clientToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void getReservations_okForEmployee() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations", HttpMethod.GET, authEntity(employeeToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getReservations_okForManager() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations", HttpMethod.GET, authEntity(managerToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getReservationById_okWhenClientIsOwner() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId(),
            HttpMethod.GET,
            authEntity(clientToken),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getReservationById_forbiddenWhenClientIsNotOwner() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId(),
            HttpMethod.GET,
            authEntity(otherClientToken),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void getReservationById_okForEmployeeRegardlessOfOwner() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId(),
            HttpMethod.GET,
            authEntity(employeeToken),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getReservationById_okForManagerRegardlessOfOwner() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId(),
            HttpMethod.GET,
            authEntity(managerToken),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void downloadTicket_okWhenClientIsOwner() {
    ResponseEntity<byte[]> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId() + "/ticket",
            HttpMethod.GET,
            authEntity(clientToken),
            byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    assertThat(response.getBody()).isNotNull();
    assertThat(new String(response.getBody(), 0, 5, java.nio.charset.StandardCharsets.US_ASCII))
        .isEqualTo("%PDF-");
  }

  @Test
  void downloadTicket_forbiddenWhenClientIsNotOwner() {
    ResponseEntity<byte[]> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId() + "/ticket",
            HttpMethod.GET,
            authEntity(otherClientToken),
            byte[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void downloadTicket_okForEmployeeRegardlessOfOwner() {
    ResponseEntity<byte[]> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId() + "/ticket",
            HttpMethod.GET,
            authEntity(employeeToken),
            byte[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void downloadTicket_okForManagerRegardlessOfOwner() {
    ResponseEntity<byte[]> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId() + "/ticket",
            HttpMethod.GET,
            authEntity(managerToken),
            byte[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void downloadTicket_unauthorizedForAnonymous() {
    ResponseEntity<byte[]> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId() + "/ticket",
            HttpMethod.GET,
            authEntity(null),
            byte[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void putReservation_forbiddenForClient() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId(),
            HttpMethod.PUT,
            reservationUpdateBody(clientToken),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void putReservation_okForEmployee() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId(),
            HttpMethod.PUT,
            reservationUpdateBody(employeeToken),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void putReservation_okForManager() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations/" + clientReservation.getId(),
            HttpMethod.PUT,
            reservationUpdateBody(managerToken),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private HttpEntity<String> reservationUpdateBody(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body =
        String.format(
            "{\"projectionId\":\"%s\",\"seatIds\":[\"%s\"]}", projection.getId(), seat.getId());
    return new HttpEntity<>(body, headers);
  }

  @Test
  void postReservation_forbiddenForEmployee() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations", HttpMethod.POST, reservationUpdateBody(employeeToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void postReservation_okForClient() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/reservations", HttpMethod.POST, reservationUpdateBody(clientToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }
}
