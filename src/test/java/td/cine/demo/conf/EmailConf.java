package td.cine.demo.conf;

import org.springframework.test.context.DynamicPropertyRegistry;
import td.cine.demo.PojaGenerated;

@PojaGenerated
public class EmailConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aws.ses.source", () -> "dummy-ses-source");
  }
}
