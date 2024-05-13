package persistence.core.mapper;

import persistence.annotations.*;

import java.lang.reflect.Field;

public class FieldProperty {
  private final String tableOwner;
  private final Field field;

  public FieldProperty(String tableOwner, Field field){
    this.tableOwner = tableOwner;
    this.field = field;
  }

  public Field getField() {
    return field;
  }

  private String oneOfAAndB(String a, String b){
    return ((a != null && !a.trim().isEmpty()) ? a : b).trim();
  }

  public String getColumnName(){
    String fieldName = field.getName();
    if(field.isAnnotationPresent(Column.class)){
      String name = field.getAnnotation(Column.class).name();
      return oneOfAAndB(name, fieldName);
    }else if(field.isAnnotationPresent(JoinColumn.class)){
      String name = field.getAnnotation(JoinColumn.class).name();
      return oneOfAAndB(name, fieldName);
    }else if(field.isAnnotationPresent(Attribute.class)){
      String name = field.getAnnotation(Attribute.class).name();
      return oneOfAAndB(name, fieldName);
    }
    return fieldName;
  }

  public Class<?> getReturnType(){
    return field.getType();
  }

  public boolean isReferencing(){
    Class<?> type = getReturnType();
    return type.isAnnotationPresent(Table.class) ||
      type.isAnnotationPresent(View.class) ||
      type.isAnnotationPresent(Embed.class);
  }

  public boolean matchTable(String table){
    if(table == null) return false;
    return table.isEmpty() || tableOwner.equalsIgnoreCase(table);
  }
}
