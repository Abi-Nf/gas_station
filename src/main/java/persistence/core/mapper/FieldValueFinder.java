package persistence.core.mapper;

import persistence.annotations.*;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FieldValueFinder {
  private int index;
  private Field field;
  private Object value;

  public FieldValueFinder(String fieldName, Object[] objects) throws Exception {
    for (int i = 0; i < objects.length; i++) {
      if(hasField(fieldName, objects[i])){
        this.index = i;
        this.value = getFieldValue(objects[i]);
        break;
      }
    }
  }

  private Object getFieldValue(Object object) throws Exception {
    field.setAccessible(true);
    Object value = field.get(object);
    field.setAccessible(false);
    return value;
  }

  private Object getIdForeignValue(Object object) {
    for (Field foreignField : field
      .getType()
      .getDeclaredFields()
    ) {
      if (foreignField.isAnnotationPresent(Id.class)){
        foreignField.setAccessible(true);
        try {
          return foreignField.get(object);
        }catch (Exception ignored){}
        finally {
          foreignField.setAccessible(false);
        }
      }
    }
    return null;
  }

  private Object castObject(Object object) throws Exception {
    if(field.getType().isAnnotationPresent(Table.class)){
      Object v = getIdForeignValue(value);
      if(v != null){
        value = v;
      }
    }
    if (field.isAnnotationPresent(Enumerated.class) && value instanceof Enum<?>) {
      EnumType enumType = field.getDeclaredAnnotation(Enumerated.class).value();
      if (enumType == EnumType.ORDINAL)
        return ((Enum<?>) value).ordinal();
      else
        return ((Enum<?>) value).name();
    }
    if(value instanceof java.util.Date)
      value = ((java.util.Date) value).toInstant();
    if(value instanceof Instant){
      return Timestamp.from((Instant) value);
    }else if(value instanceof LocalDate){
      return Date.valueOf((LocalDate) value);
    }else if(value instanceof LocalDateTime){
      return Timestamp.valueOf((LocalDateTime) value);
    }
    return value;
  }

  private boolean hasField(String fieldName, Object object){
    try {
      this.field = object
        .getClass()
        .getDeclaredField(fieldName);
      return true;
    }catch (Exception e){
      return false;
    }
  }

  public Object getValue() {
    return value;
  }

  public int getIndex() {
    return index;
  }

  public boolean find(){
    return field != null;
  }
}
