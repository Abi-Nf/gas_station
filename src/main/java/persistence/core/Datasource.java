package persistence.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Datasource {
  private final String url;
  private final String user;
  private final String password;

  private final Connection connection;

  public Datasource(String url, String user, String password) {
    this.url = url;
    this.user = user;
    this.password = password;
    this.connection = newConnection();
  }

  public Datasource copy(){
    return new Datasource(url, user, password);
  }

  public Connection newConnection() {
    try {
      return DriverManager.getConnection(url, user, password);
    }catch (SQLException e){
      throw new DatabaseException(e);
    }
  }

  public Connection getConnection() throws SQLException {
    return connection;
  }
}
