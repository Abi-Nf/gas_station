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
  private boolean primitiveOrWrapper = false;
  private final HashMap<String, FieldProperty> fieldMaps = new HashMap<>();

  public ResultSetMapper(Class<T> clazz) {
    this.clazz = clazz;
    if(Utils.isPrimitiveOrWrapper(clazz)){
      this.primitiveOrWrapper = true;
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
    if(primitiveOrWrapper){
      return rs.getObject(1, clazz);
    }

    T object = newInstance();
    ResultSetMetaData metadata = rs.getMetaData();

    for (int i = 1; i <= metadata.getColumnCount(); i++) {
      String column = metadata.getColumnName(i);

      if (!fieldMaps.containsKey(column)) continue;
      FieldProperty fieldProperty = fieldMaps.get(column);

      if(!fieldProperty.matchTable(metadata.getTableName(i))) continue;

      Object value;
      if (Utils.isPrimitiveOrWrapper(fieldProperty.getReturnType())) {
         value = rs.getObject(i, fieldProperty.getReturnType());
      }else {
         value = rs.getObject(i);
      }

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
    if(value == null) {
      return null;
    }else if(value instanceof byte[] && byte[].class.equals(fieldReturnType)){
      return value;
    }else if (fieldReturnType.equals(value.getClass()))
      return fieldReturnType.cast(value);
    else if(fieldReturnType.isEnum()){
      return parseEnumValue(fieldProperty.getField(), value);
    }else if(value instanceof Time){
      return timeCastType(fieldReturnType, value);
    }else if(value instanceof Timestamp){
      return timestampCastType(fieldReturnType, value);
    }else if(value instanceof java.sql.Date){
      return dateCastType(fieldReturnType, value);
    }else if (value instanceof Array && fieldReturnType.equals(List.class)){
      ResultSet resultSet = ((Array) value).getResultSet();
      List<Object> list = new ArrayList<>();
      while (resultSet.next()) {
        Object v = resultSet.getObject(2);
        list.add(v);
      }
      return list;
    }
    throw throwUnhandledType(fieldReturnType);
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

  private DatabaseException throwUnhandledType(Class<?> clazz){
    return new DatabaseException("unhandled return type: " + clazz);
  }

  private Object timestampCastType(Class<?> fieldReturnType, Object value){
    if(fieldReturnType.equals(Instant.class)){
      return ((Timestamp) value).toInstant();
    }else if(fieldReturnType.equals(LocalDateTime.class)){
      return ((Timestamp) value).toLocalDateTime();
    }else if(fieldReturnType.equals(String.class)){
      return value.toString();
    }
    throw throwUnhandledType(fieldReturnType);
  }

  private Object dateCastType(Class<?> fieldReturnType, Object value){
    if (fieldReturnType.equals(Instant.class)){
      return ((java.sql.Date) value).toInstant();
    }else if(fieldReturnType.equals(LocalDate.class)){
      return ((Date) value).toLocalDate();
    }else if(fieldReturnType.equals(String.class)){
      return value.toString();
    }
    throw throwUnhandledType(fieldReturnType);
  }

  private Object timeCastType(Class<?> fieldReturnType, Object value){
    if(fieldReturnType.equals(Instant.class)){
      return ((Time) value).toInstant();
    }else if(fieldReturnType.equals(LocalTime.class)){
      return ((Time) value).toLocalTime();
    }else if(fieldReturnType.equals(String.class)){
      return value.toString();
    }
    throw throwUnhandledType(fieldReturnType);
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
