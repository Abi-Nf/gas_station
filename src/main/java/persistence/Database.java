package persistence;

import persistence.core.BatchQueryImpl;
import persistence.core.DatabaseException;
import persistence.core.Datasource;
import persistence.core.PreparedQueryImpl;
import persistence.core.interfaces.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Database implements DatabaseTemplate {
  private static Database DEFAULT_DATABASE;
  private final Datasource datasource;

  public Database(
    String url,
    String user,
    String password
  ){
    this.datasource = new Datasource(url, user, password);
    DEFAULT_DATABASE = this;
  }

  private Database(Datasource datasource){
    this.datasource = datasource;
  }

  @Override
  public <T> PreparedQuery<T> prepare(String sql, Class<T> clazz) {
    return new PreparedQueryImpl<>(datasource, sql, clazz);
  }

  @Override
  public BatchedQuery batch(String sql) {
    return new BatchQueryImpl(datasource, sql);
  }

  @Override
  public void execute(String sql) {
    try {
      Connection connection = datasource.getConnection();
      Statement statement = connection.createStatement();
      statement.execute(sql);
    }catch (Exception e){
      throw new DatabaseException(e);
    }
  }

  @Override
  public void execute(Path path) {
    try {
      String sql = Files.readString(path);
      execute(sql);
    }catch (Exception e){
      throw new DatabaseException(e);
    }
  }

  @Override
  public void execute(File file) {
    execute(file.toPath());
  }

  @Override
  public void transaction(TransactionVoid<DatabaseTemplate> arg1) {
    Database database = new Database(datasource.copy());
    try {
      Connection connection = database.datasource.getConnection();
      connection.setAutoCommit(false);
      arg1.apply(database);
      connection.commit();
    }catch (Exception e){
      throw rollback(database, e);
    }finally {
      closeConnection(database);
    }
  }

  @Override
  public <T> T transaction(TransactionalFn<T, DatabaseTemplate> arg1) {
    Database database = new Database(datasource.copy());
    try {
      Connection connection = database.datasource.getConnection();
      connection.setAutoCommit(false);
      T value = arg1.apply(database);
      connection.commit();
      return value;
    }catch (Exception e){
      throw rollback(database, e);
    }finally {
      closeConnection(database);
    }
  }

  private DatabaseException rollback(Database database, Exception e){
    try {
      database
        .datasource
        .getConnection()
        .rollback();
    }catch (SQLException ex){
      return new DatabaseException(ex);
    }
    return new DatabaseException(e);
  }

  private void closeConnection(Database database){
    try {
      Connection connection = database.datasource.getConnection();
      connection.setAutoCommit(true);
      connection.close();
    }catch (SQLException e){
      throw new DatabaseException(e);
    }
  }

  public static Database getOneFromInstance() {
    if (DEFAULT_DATABASE == null) {
      throw new RuntimeException("There is no instance of Database");
    }
    return DEFAULT_DATABASE;
  }
}
