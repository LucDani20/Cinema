package td.cine.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Duration;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import td.cine.demo.model.enums.Genre;

@Entity
@Getter
@Setter
public class Movie {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String title;

  @Enumerated(EnumType.STRING)
  private Genre genre;

  private String description;

  private Duration duration;
}
