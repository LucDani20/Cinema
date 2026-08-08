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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import td.cine.demo.dto.ProjectionDto;
import td.cine.demo.exception.NotFoundException;
import td.cine.demo.model.Movie;
import td.cine.demo.model.Projection;
import td.cine.demo.model.Room;
import td.cine.demo.model.enums.Genre;
import td.cine.demo.repository.MovieRepository;
import td.cine.demo.repository.ProjectionRepository;
import td.cine.demo.repository.RoomRepository;
import td.cine.demo.service.ProjectionService;

@ExtendWith(MockitoExtension.class)
class ProjectionServiceTest {

  @Mock private ProjectionRepository projectionRepository;
  @Mock private MovieRepository movieRepository;
  @Mock private RoomRepository roomRepository;

  @InjectMocks private ProjectionService projectionService;

  private Movie buildMovie() {
    Movie movie = new Movie();
    movie.setId(UUID.randomUUID());
    movie.setTitle("Dune");
    movie.setGenre(Genre.SCI_FI);
    movie.setDescription("desc");
    movie.setDuration(Duration.ofMinutes(155));
    return movie;
  }

  private Room buildRoom() {
    Room room = new Room();
    room.setId(UUID.randomUUID());
    room.setNumber("A");
    room.setCapacity(50);
    return room;
  }

  @Test
  void findAll_returnsEveryProjection() {
    Movie movie = buildMovie();
    Room room = buildRoom();
    Projection projection = new Projection();
    projection.setId(UUID.randomUUID());
    projection.setMovie(movie);
    projection.setRoom(room);
    projection.setDatetime(Instant.now());
    projection.setSeatPrice(BigDecimal.TEN);

    when(projectionRepository.findAll()).thenReturn(List.of(projection));

    List<ProjectionDto> result = projectionService.findAll();

    assertThat(result).hasSize(1);
  }

  @Test
  void save_throwsNotFoundWhenMovieMissing() {
    UUID movieId = UUID.randomUUID();
    UUID roomId = UUID.randomUUID();
    ProjectionDto dto = new ProjectionDto(null, movieId, roomId, Instant.now(), BigDecimal.TEN);

    when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> projectionService.save(dto)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void save_throwsNotFoundWhenRoomMissing() {
    Movie movie = buildMovie();
    UUID roomId = UUID.randomUUID();
    ProjectionDto dto =
        new ProjectionDto(null, movie.getId(), roomId, Instant.now(), BigDecimal.TEN);

    when(movieRepository.findById(movie.getId())).thenReturn(Optional.of(movie));
    when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> projectionService.save(dto)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void save_createsNewProjectionSuccessfully() {
    Movie movie = buildMovie();
    Room room = buildRoom();
    Instant datetime = Instant.now();
    ProjectionDto dto =
        new ProjectionDto(null, movie.getId(), room.getId(), datetime, BigDecimal.valueOf(15));

    when(movieRepository.findById(movie.getId())).thenReturn(Optional.of(movie));
    when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
    when(projectionRepository.save(any(Projection.class))).thenAnswer(inv -> inv.getArgument(0));

    ProjectionDto saved = projectionService.save(dto);

    assertThat(saved.seatPrice()).isEqualTo(BigDecimal.valueOf(15));
    assertThat(saved.movieId()).isEqualTo(movie.getId());
    assertThat(saved.roomId()).isEqualTo(room.getId());
    verify(projectionRepository, times(1)).save(any(Projection.class));
  }
}
