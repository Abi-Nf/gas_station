package persistence.core.interfaces;

public interface BatchedQuery {
  void add(Object... args);
  void run();
  void clear();
}
