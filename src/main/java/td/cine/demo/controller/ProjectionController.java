package td.cine.demo.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import td.cine.demo.dto.ProjectionDto;
import td.cine.demo.service.ProjectionService;

@RestController
@RequestMapping("/projections")
@RequiredArgsConstructor
public class ProjectionController {

  private final ProjectionService projectionService;

  @GetMapping
  public ResponseEntity<List<ProjectionDto>> findAll() {
    return ResponseEntity.ok(projectionService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectionDto> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(projectionService.findById(id));
  }

  @PutMapping
  @PreAuthorize("hasRole('MANAGER')")
  public ResponseEntity<ProjectionDto> save(@Valid @RequestBody ProjectionDto dto) {
    return ResponseEntity.ok(projectionService.save(dto));
  }
}
