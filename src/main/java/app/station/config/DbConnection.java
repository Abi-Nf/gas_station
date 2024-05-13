package app.station.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import persistence.Database;

@Configuration
public class DbConnection {
  @Bean
  public Database database() {
    String url = System.getenv("DB_URL");
    String user = System.getenv("DB_USER");
    String password = System.getenv("DB_PASSWORD");
    return new Database(url, user, password);
  }
}
