package td.cine.demo.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import td.cine.demo.dto.MovieDto;
import td.cine.demo.exception.NotFoundException;
import td.cine.demo.model.Movie;
import td.cine.demo.repository.MovieRepository;

@Service
@RequiredArgsConstructor
public class MovieService {

  private final MovieRepository movieRepository;

  public List<MovieDto> findAll() {
    return movieRepository.findAll().stream().map(this::toDto).toList();
  }

  public MovieDto findById(UUID id) {
    return toDto(getOrThrow(id));
  }

  public MovieDto save(MovieDto dto) {
    Movie movie = dto.id() != null ? getOrThrow(dto.id()) : new Movie();
    movie.setTitle(dto.title());
    movie.setGenre(dto.genre());
    movie.setDescription(dto.description());
    movie.setDuration(Duration.ofMinutes(dto.durationMinutes()));
    movieRepository.save(movie);
    return toDto(movie);
  }

  private Movie getOrThrow(UUID id) {
    return movieRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Movie not found: " + id));
  }

  private MovieDto toDto(Movie movie) {
    return new MovieDto(
        movie.getId(),
        movie.getTitle(),
        movie.getGenre(),
        movie.getDescription(),
        movie.getDuration().toMinutes());
  }
}
