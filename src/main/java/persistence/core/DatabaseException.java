package persistence.core;

public class DatabaseException extends RuntimeException {
  public DatabaseException(Exception e) {
    super(e);
  }

  public DatabaseException(String message) {
    super(message);
  }
}
