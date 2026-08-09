package td.cine.demo.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import td.cine.demo.dto.RoomDto;
import td.cine.demo.exception.NotFoundException;
import td.cine.demo.model.Room;
import td.cine.demo.repository.RoomRepository;
import td.cine.demo.service.RoomService;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

  @Mock private RoomRepository roomRepository;

  @InjectMocks private RoomService roomService;

  @Test
  void findById_throwsNotFoundWhenMissing() {
    UUID id = UUID.randomUUID();
    when(roomRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> roomService.findById(id)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void save_createsNewRoom() {
    RoomDto dto = new RoomDto(null, "B12", 80);
    when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

    RoomDto saved = roomService.save(dto);

    assertThat(saved.number()).isEqualTo("B12");
    assertThat(saved.capacity()).isEqualTo(80);
  }

  @Test
  void save_updatesExistingRoom() {
    UUID id = UUID.randomUUID();
    Room existing = new Room();
    existing.setId(id);
    existing.setNumber("A1");
    existing.setCapacity(40);

    RoomDto dto = new RoomDto(id, "A1-bis", 60);

    when(roomRepository.findById(id)).thenReturn(Optional.of(existing));
    when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

    RoomDto saved = roomService.save(dto);

    assertThat(saved.number()).isEqualTo("A1-bis");
    assertThat(saved.capacity()).isEqualTo(60);
  }
}
