package td.cine.demo.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import td.cine.demo.dto.MovieDto;
import td.cine.demo.exception.NotFoundException;
import td.cine.demo.model.Movie;
import td.cine.demo.model.enums.Genre;
import td.cine.demo.repository.MovieRepository;
import td.cine.demo.service.MovieService;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

  @Mock private MovieRepository movieRepository;

  @InjectMocks private MovieService movieService;

  @Test
  void findAll_returnsAllMoviesAsDto() {
    Movie movie = new Movie();
    movie.setId(UUID.randomUUID());
    movie.setTitle("Dune");
    movie.setGenre(Genre.SCI_FI);
    movie.setDescription("desc");
    movie.setDuration(Duration.ofMinutes(155));

    when(movieRepository.findAll()).thenReturn(List.of(movie));

    List<MovieDto> result = movieService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).title()).isEqualTo("Dune");
    assertThat(result.get(0).genre()).isEqualTo(Genre.SCI_FI);
  }

  @Test
  void findById_throwsNotFoundWhenMissing() {
    UUID id = UUID.randomUUID();
    when(movieRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> movieService.findById(id)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void save_createsNewMovieWhenIdIsNull() {
    MovieDto dto = new MovieDto(null, "Interstellar", Genre.SCI_FI, "desc", 169);
    when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

    MovieDto saved = movieService.save(dto);

    assertThat(saved.title()).isEqualTo("Interstellar");
    assertThat(saved.genre()).isEqualTo(Genre.SCI_FI);
    assertThat(saved.durationMinutes()).isEqualTo(169);
    verify(movieRepository, times(1)).save(any(Movie.class));
  }

  @Test
  void save_updatesExistingMovie() {
    UUID id = UUID.randomUUID();
    Movie existing = new Movie();
    existing.setId(id);
    existing.setTitle("Old title");
    existing.setGenre(Genre.ACTION);
    existing.setDescription("old");
    existing.setDuration(Duration.ofMinutes(100));

    MovieDto dto = new MovieDto(id, "New title", Genre.COMEDY, "new", 110);

    when(movieRepository.findById(id)).thenReturn(Optional.of(existing));
    when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

    MovieDto saved = movieService.save(dto);

    assertThat(saved.title()).isEqualTo("New title");
    assertThat(saved.genre()).isEqualTo(Genre.COMEDY);
    assertThat(saved.durationMinutes()).isEqualTo(110);
  }

  @Test
  void save_throwsNotFoundWhenUpdatingMissingMovie() {
    UUID id = UUID.randomUUID();
    MovieDto dto = new MovieDto(id, "Ghost", Genre.DRAMA, "desc", 100);
    when(movieRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> movieService.save(dto)).isInstanceOf(NotFoundException.class);
  }
}
