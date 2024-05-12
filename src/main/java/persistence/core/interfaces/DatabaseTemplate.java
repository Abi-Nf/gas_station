package persistence.core.interfaces;

import java.io.File;
import java.nio.file.Path;

public interface DatabaseTemplate {
  <T> PreparedQuery<T> prepare(String sql, Class<T> clazz);

  BatchedQuery batch(String sql);

  void execute(String sql);

  void execute(Path path);

  void execute(File file);

  void transaction(TransactionVoid<DatabaseTemplate> arg1);

  <T> T transaction(TransactionalFn<T, DatabaseTemplate> arg1);
}
