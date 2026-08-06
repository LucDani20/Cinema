package td.cine.demo.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import td.cine.demo.model.Projection;

@Repository
public interface ProjectionRepository extends JpaRepository<Projection, UUID> {}