package app.station.config;

import app.station.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class Job {
  private final ProductRepository productRepository;

  // every 10 minutes
  @Scheduled(fixedRate = 1000 * 60 * 10)
  public void scheduledJob() {
    productRepository.checkAndUpdateEvaporation();
  }
}
