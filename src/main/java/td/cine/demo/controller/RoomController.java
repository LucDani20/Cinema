package td.cine.demo.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import td.cine.demo.dto.RoomDto;
import td.cine.demo.service.RoomService;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;

  @GetMapping
  public ResponseEntity<List<RoomDto>> findAll() {
    return ResponseEntity.ok(roomService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<RoomDto> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(roomService.findById(id));
  }

  @PutMapping
  @PreAuthorize("hasRole('MANAGER')")
  public ResponseEntity<RoomDto> save(@Valid @RequestBody RoomDto dto) {
    return ResponseEntity.ok(roomService.save(dto));
  }
}
