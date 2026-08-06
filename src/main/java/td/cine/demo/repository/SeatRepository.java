package td.cine.demo.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import td.cine.demo.model.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

  List<Seat> findAllByRoomId(UUID roomId);
}
