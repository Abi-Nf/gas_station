package persistence.core.mapper;

import persistence.core.DatabaseException;
import persistence.core.Datasource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatementMapper {
  private static final Pattern PARAM_PATTERN = Pattern.compile("(?<param>(\\?|\\?\\w+))");

  private final List<StatementParam> params = new ArrayList<>();
  private final PreparedStatement statement;

  public StatementMapper(
    Datasource datasource,
    String query
  ) throws SQLException {
    Matcher matcher = PARAM_PATTERN.matcher(query);
    long count = 0;
    while (matcher.find()) {
      count++;
      String paramValue = matcher.group("param");
      query = query.replace(paramValue, "?");
      params.add(StatementParam.parse(
        count,
        paramValue.equals("?")
          ? "?"
          : paramValue.substring(1)
      ));
    }
    this.statement = datasource
      .getConnection()
      .prepareStatement(query);
  }

  public PreparedStatement getStatement(Object[] args) throws Exception {
    for (StatementParam param : params) {
      switch (param.getType()){
        case QUESTION_MARK -> {
          int index = (int) param.getIndex();
          this.statement.setObject(index, args[index - 1]);
        }
        case EXPLICIT_INDEX -> {
          int index = (int) param.getValue();
          this.statement.setObject(index, args[index - 1]);
        }
        case FIELD_NAME -> {
          int index = (int) param.getIndex();
          String fieldName = (String) param.getValue();
          FieldValueFinder finder = new FieldValueFinder(fieldName, args);
          if(finder.find()){
            this.statement.setObject(index, finder.getValue());
          }else {
            throw new DatabaseException("Field " + fieldName + " not found");
          }
        }
      }
    }
    return statement;
  }

  public void clear(){
    try {
      this.statement.clearParameters();
      this.statement.clearBatch();
      this.statement.clearWarnings();
    }catch (SQLException e){
      throw new DatabaseException(e);
    }
  }
}
