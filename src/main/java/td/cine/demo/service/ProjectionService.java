package td.cine.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import td.cine.demo.dto.ProjectionDto;
import td.cine.demo.exception.NotFoundException;
import td.cine.demo.model.Movie;
import td.cine.demo.model.Projection;
import td.cine.demo.model.Room;
import td.cine.demo.repository.MovieRepository;
import td.cine.demo.repository.ProjectionRepository;
import td.cine.demo.repository.RoomRepository;

/**
 * GET /projections est accessible a tout le monde (regle: "should return 200 for everyone"). PUT
 * /projections est reserve au MANAGER (regle appliquee dans le controller via @PreAuthorize).
 */
@Service
@RequiredArgsConstructor
public class ProjectionService {

  private final ProjectionRepository projectionRepository;
  private final MovieRepository movieRepository;
  private final RoomRepository roomRepository;

  public List<ProjectionDto> findAll() {
    return projectionRepository.findAll().stream().map(this::toDto).toList();
  }

  public ProjectionDto findById(UUID id) {
    return toDto(getOrThrow(id));
  }

  public ProjectionDto save(ProjectionDto dto) {
    Movie movie =
        movieRepository
            .findById(dto.movieId())
            .orElseThrow(() -> new NotFoundException("Movie not found: " + dto.movieId()));
    Room room =
        roomRepository
            .findById(dto.roomId())
            .orElseThrow(() -> new NotFoundException("Room not found: " + dto.roomId()));

    Projection projection = dto.id() != null ? getOrThrow(dto.id()) : new Projection();
    projection.setMovie(movie);
    projection.setRoom(room);
    projection.setDatetime(dto.datetime());
    projection.setSeatPrice(dto.seatPrice());
    projectionRepository.save(projection);
    return toDto(projection);
  }

  private Projection getOrThrow(UUID id) {
    return projectionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Projection not found: " + id));
  }

  private ProjectionDto toDto(Projection projection) {
    return new ProjectionDto(
        projection.getId(),
        projection.getMovie().getId(),
        projection.getRoom().getId(),
        projection.getDatetime(),
        projection.getSeatPrice());
  }
}
