package td.cine.demo.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import td.cine.demo.dto.MovieDto;
import td.cine.demo.service.MovieService;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

  private final MovieService movieService;

  @GetMapping
  public ResponseEntity<List<MovieDto>> findAll() {
    return ResponseEntity.ok(movieService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<MovieDto> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(movieService.findById(id));
  }

  @PutMapping
  @PreAuthorize("hasRole('MANAGER')")
  public ResponseEntity<MovieDto> save(@Valid @RequestBody MovieDto dto) {
    return ResponseEntity.ok(movieService.save(dto));
  }
}
