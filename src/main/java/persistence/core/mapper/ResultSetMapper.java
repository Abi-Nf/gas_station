package persistence.core.mapper;

import persistence.annotations.*;
import persistence.core.DatabaseException;

import java.lang.reflect.Field;
import java.sql.*;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class ResultSetMapper<T> {
  private final Class<T> clazz;
  private boolean primitive = false;
  private final HashMap<String, FieldProperty> fieldMaps = new HashMap<>();

  public ResultSetMapper(Class<T> clazz) {
    this.clazz = clazz;
    if(Utils.isPrimitiveOrWrapper(clazz)){
      this.primitive = true;
    }else {
      String tableName = getTableName(clazz);
      for (Field field : clazz.getDeclaredFields()) {
        field.setAccessible(true);
        FieldProperty fieldProperty = new FieldProperty(tableName, field);
        fieldMaps.put(fieldProperty.getColumnName(), fieldProperty);
        field.setAccessible(false);
      }
    }
  }

  private T newInstance() throws Exception {
    return clazz
      .getDeclaredConstructor()
      .newInstance();
  }

  public T map(ResultSet rs) throws Exception {
    if(primitive)
      return clazz.cast(rs.getObject(1));

    T object = newInstance();
    ResultSetMetaData metadata = rs.getMetaData();

    for (int i = 1; i <= metadata.getColumnCount(); i++) {
      String column = metadata.getColumnName(i);

      if (!fieldMaps.containsKey(column)) continue;
      FieldProperty fieldProperty = fieldMaps.get(column);

      if(!fieldProperty.matchTable(metadata.getTableName(i))) continue;

      Object value = rs.getObject(i);
      Field field = fieldProperty.getField();
      field.setAccessible(true);
      if(fieldProperty.isReferencing()){
        Class<?> refClass = fieldProperty.getReturnType();
        Object refValue = new ResultSetMapper<>(refClass).map(rs);
        field.set(object, refValue);
      }else {
        field.set(object, castObject(fieldProperty, value));
      }
      field.setAccessible(false);
    }
    return object;
  }

  private Object castObject(FieldProperty fieldProperty, Object value) throws Exception {
    Class<?> fieldReturnType = fieldProperty.getReturnType();
    if(value == null) return null;
    else if(
      value instanceof byte[] &&
      byte[].class.equals(fieldReturnType)
    ){
      return value;
    }else if (
      fieldReturnType
        .equals(value.getClass())
    )
      return fieldReturnType.cast(value);
    else if(fieldReturnType.isEnum()){
      return parseEnumValue(fieldProperty.getField(), value);
    } else if (
      value instanceof Array &&
      fieldReturnType.equals(List.class)
    ){
      ResultSet resultSet = ((Array) value).getResultSet();
      List<Object> list = new ArrayList<>();
      while (resultSet.next()) {
        Object v = resultSet.getObject(2);
        list.add(v);
      }
      return list;
    }else if(value instanceof Time){
      if(fieldReturnType.equals(Instant.class)){
        return ((Time) value).toInstant();
      }else if(fieldReturnType.equals(LocalTime.class)){
        return ((Time) value).toLocalTime();
      }else if(fieldReturnType.equals(String.class)){
        return value.toString();
      }
    }else if(value instanceof Timestamp){
      if(fieldReturnType.equals(Instant.class)){
        return ((Timestamp) value).toInstant();
      }else if(fieldReturnType.equals(LocalDateTime.class)){
        return ((Timestamp) value).toLocalDateTime();
      }else if(fieldReturnType.equals(String.class)){
        return value.toString();
      }
    }else if(value instanceof java.sql.Date){
      if (fieldReturnType.equals(Instant.class)){
        return ((java.sql.Date) value).toInstant();
      }else if(fieldReturnType.equals(LocalDate.class)){
        return ((Date) value).toLocalDate();
      }else if(fieldReturnType.equals(String.class)){
        return value.toString();
      }
    }
    throw new DatabaseException("unhandled return type: " + fieldReturnType);
  }

  private String getTableName(Class<?> clazz){
    String className = clazz.getSimpleName().toLowerCase();
    if(clazz.isAnnotationPresent(Table.class)) {
      Table table = clazz.getAnnotation(Table.class);
      return oneOfAAndB(table.name(), className);
    }else if(clazz.isAnnotationPresent(View.class)) {
      View view = clazz.getAnnotation(View.class);
      return oneOfAAndB(view.name(), className);
    }else if(clazz.isAnnotationPresent(Embed.class)) {
      Embed embed = clazz.getAnnotation(Embed.class);
      return oneOfAAndB(embed.name(), className);
    }
    return className;
  }

  private String oneOfAAndB(String a, String b){
    return ((a != null && !a.trim().isEmpty()) ? a : b).trim();
  }

  private Object parseEnumValue(Field field, Object value){
    if (field.isAnnotationPresent(Enumerated.class)) {
      Enumerated enumerated = field.getAnnotation(Enumerated.class);
      if (enumerated.value() == EnumType.STRING) {
        return Enum.valueOf(
          field
            .getType()
            .asSubclass(Enum.class),
          value.toString()
        );
      }
    }
    Class<?> enumObject = field.getType();
    int index = Integer.parseInt(value.toString());
    return enumObject.getEnumConstants()[index];
  }
}
