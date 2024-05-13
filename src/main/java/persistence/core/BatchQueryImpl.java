package persistence.core;

import persistence.core.interfaces.BatchedQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BatchQueryImpl implements BatchedQuery {
  private final PreparedStatement statement;

  public BatchQueryImpl(
    Datasource datasource,
    String sql
  ) {
    try {
      Connection connection = datasource.getConnection();
      this.statement = connection.prepareStatement(sql);
    }catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void add(Object... args) {
    try {
      for (int i = 0; i < args.length; i++) {
        statement.setObject(i + 1, args[i]);
      }
      statement.addBatch();
    }catch (SQLException e){
      throw new DatabaseException(e);
    }
  }

  @Override
  public void run() {
    try {
      statement.executeLargeBatch();
    }catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void clear() {
    try {
      statement.clearBatch();
    }catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
