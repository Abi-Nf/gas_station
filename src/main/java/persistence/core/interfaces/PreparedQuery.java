package persistence.core.interfaces;

import java.util.List;
import java.util.Optional;

public interface PreparedQuery<Result> {
  Result get(Object... args);
  List<Result> all(Object... args);
  Optional<Result> optional(Object... args);
  void run(Object... args);
  void clear();
}
