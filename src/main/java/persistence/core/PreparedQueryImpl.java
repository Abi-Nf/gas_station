package persistence.core;

import persistence.annotations.Embed;
import persistence.annotations.Table;
import persistence.annotations.View;
import persistence.core.interfaces.PreparedQuery;
import persistence.core.mapper.ResultSetMapper;
import persistence.core.mapper.StatementMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PreparedQueryImpl<T> implements PreparedQuery<T> {
  private final ResultSetMapper<T> resultSetMapper;
  private final StatementMapper statementMapper;

  public PreparedQueryImpl(
    Datasource datasource,
    String query,
    Class<T> clazz
  ) {
    try {
      query = interceptQuery(query, clazz);
      this.statementMapper = new StatementMapper(datasource, query);
      this.resultSetMapper = new ResultSetMapper<>(clazz);
    }catch (Exception e){
      throw new DatabaseException(e);
    }
  }

  public String interceptQuery(String query, Class<?> clazz) {
    if (query.contains("@table")) {
      query = query.replaceAll("@table", getTableName(clazz));
    }
    if(query.contains("@view")){
      query = query.replaceAll("@view", getViewTableName(clazz));
    }
    if(query.contains("@embed")){
      query = query.replaceAll("@embed", getEmbeddedClass(clazz));
    }
    return query;
  }

  private String getTableName(Class<?> clazz){
    String tableName = clazz.getSimpleName().toLowerCase();
    Table table = clazz.getDeclaredAnnotation(Table.class);
    return oneOfAAndB(table.name(), tableName);
  }

  private String getViewTableName(Class<?> clazz){
    String tableName = clazz.getSimpleName().toLowerCase();
    View viewTable = clazz.getDeclaredAnnotation(View.class);
    return oneOfAAndB(viewTable.name(), tableName);
  }

  private String getEmbeddedClass(Class<?> clazz){
    String tableName = clazz.getSimpleName().toLowerCase();
    Embed embedMapper = clazz.getDeclaredAnnotation(Embed.class);
    return oneOfAAndB(embedMapper.name(), tableName);
  }

  private String oneOfAAndB(String a, String b){
    return ((a != null && !a.trim().isEmpty()) ? a : b).trim();
  }

  @Override
  public T get(Object... args) {
    try(PreparedStatement statement = this.statementMapper.getStatement(args)){
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSetMapper.map(resultSet);
      }
      return null;
    }catch (Exception e){
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<T> all(Object... args) {
    try(PreparedStatement statement = this.statementMapper.getStatement(args)){
      List<T> values = new ArrayList<>();
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        values.add(resultSetMapper.map(resultSet));
      }
      return values;
    }catch (Exception e){
      throw new DatabaseException(e);
    }
  }

  @Override
  public Optional<T> optional(Object... args) {
    try(PreparedStatement statement = this.statementMapper.getStatement(args)){
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return Optional.of(resultSetMapper.map(resultSet));
      }
      return Optional.empty();
    }catch (Exception e){
      throw new DatabaseException(e);
    }
  }

  @Override
  public void run(Object... args) {
    try(PreparedStatement statement = this.statementMapper.getStatement(args)){
      statement.executeUpdate();
    }catch (Exception e){
      throw new DatabaseException(e);
    }
  }

  @Override
  public void clear() {
    this.statementMapper.clear();
  }
}
