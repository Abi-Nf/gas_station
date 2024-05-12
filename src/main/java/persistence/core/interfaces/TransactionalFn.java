package persistence.core.interfaces;

public interface TransactionalFn<T, R> {
  T apply(R arg1) throws Exception;
}
