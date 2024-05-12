package persistence.core.interfaces;

public interface TransactionVoid<T> {
  void apply(T arg1) throws Exception;
}
