package td.cine.demo.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {

  public void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("JWT_SECRET", () -> "dGVzdC1zZWNyZXQtY2luZW1hLWFwcC1mb3ItdW5pdC10ZXN0cy1vbmx5");
    registry.add("JWT_EXPIRATION_MS", () -> "3600000");
  }
}
