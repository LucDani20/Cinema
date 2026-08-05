package td.cine.demo.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import td.cine.demo.dto.RoomDto;
import td.cine.demo.exception.NotFoundException;
import td.cine.demo.model.Room;
import td.cine.demo.repository.RoomRepository;

@Service
@RequiredArgsConstructor
public class RoomService {

  private final RoomRepository roomRepository;

  public List<RoomDto> findAll() {
    return roomRepository.findAll().stream().map(this::toDto).toList();
  }

  public RoomDto findById(UUID id) {
    return toDto(getOrThrow(id));
  }

  public RoomDto save(RoomDto dto) {
    Room room = dto.id() != null ? getOrThrow(dto.id()) : new Room();
    room.setNumber(dto.number());
    room.setCapacity(dto.capacity());
    roomRepository.save(room);
    return toDto(room);
  }

  private Room getOrThrow(UUID id) {
    return roomRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Room not found: " + id));
  }

  private RoomDto toDto(Room room) {
    return new RoomDto(room.getId(), room.getNumber(), room.getCapacity());
  }
}
