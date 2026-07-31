package td.cine.demo.conf;

import org.springframework.test.context.DynamicPropertyRegistry;
import td.cine.demo.PojaGenerated;

@PojaGenerated
public class BucketConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aws.s3.bucket", () -> "dummy-bucket");
  }
}
