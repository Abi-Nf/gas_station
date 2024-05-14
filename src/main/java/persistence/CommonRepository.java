package persistence;

import persistence.annotations.Column;
import persistence.annotations.Id;
import persistence.annotations.JoinColumn;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommonRepository<Entity, ID> {
  private final Database database = Database.getOneFromInstance();
  private final Class<Entity> entityClass;
  private String entityIdColumn;

  @SuppressWarnings("unchecked")
  public CommonRepository(){
    Class<?> clazz = this.getClass();
    ParameterizedType d = (ParameterizedType) clazz.getGenericSuperclass();
    this.entityClass = (Class<Entity>) d.getActualTypeArguments()[0];
    assignIdColumn();
  }

  public <S extends Entity> List<Entity> saveAll(List<S> entities) {
    return database.transaction(transactional -> {
      List<Entity> values = new ArrayList<>();
      for (S entity : entities) {
        Insertion insertion = getInsertedValues(entity);
        String sql = String.format(
          "INSERT INTO \"@table\" (%s) VALUES (%s) RETURNING *",
          String.join(", ", insertion.getColumns()),
          String.join(", ", insertion.getFields())
        );
        Entity value = transactional
          .prepare(sql, entityClass)
          .get(entity);
        values.add(value);
      }
      return values;
    });
  }

  public <S extends Entity> Entity save(S entity) {
    try {
      Insertion insertion = getInsertedValues(entity);
      String sql = String.format(
        "INSERT INTO \"@table\" (%s) VALUES (%s) RETURNING *",
        String.join(", ", insertion.getColumns()),
        String.join(", ", insertion.getFields())
      );
      return database
        .prepare(sql, entityClass)
        .get(entity);
    }catch (Exception e){
      throw new RuntimeException(e);
    }
  }

  public List<Entity> findAll() {
    return database
      .prepare("SELECT * FROM \"@table\"", entityClass)
      .all();
  }

  public List<Entity> findAllById(ID id){
    return database
      .prepare("SELECT * FROM \"@table\" WHERE " + entityIdColumn + " = ?", entityClass)
      .all(id);
  }

  public Optional<Entity> findById(ID id) {
    String sql = "SELECT * FROM \"@table\" WHERE " + entityIdColumn + " = ?";
    return database
      .prepare(sql, entityClass)
      .optional(id);
  }

  public Entity deleteById(ID id) {
    return database
      .prepare(
        "DELETE FROM \"@table\" WHERE " + entityIdColumn + " = ? RETURNING *",
        entityClass
      )
      .get(id);
  }

  public List<Entity> deleteAll(){
    return database
      .prepare("DELETE FROM \"@table\" RETURNING *", entityClass)
      .all();
  }

  private void assignIdColumn(){
    for (Field field : entityClass.getDeclaredFields()) {
      field.setAccessible(true);
      if (field.isAnnotationPresent(Id.class)) {
        this.entityIdColumn = getColumnName(field);
        field.setAccessible(false);
        break;
      }
      field.setAccessible(false);
    }
  }

  private String getColumnName(Field field){
    String name = field.getName().trim().toLowerCase();
    if(field.isAnnotationPresent(Column.class)){
      String columnName = field.getDeclaredAnnotation(Column.class).name().trim();
      if(!columnName.isEmpty()){
        return columnName;
      }
    }else if(field.isAnnotationPresent(JoinColumn.class)){
      String columnName = field.getDeclaredAnnotation(JoinColumn.class).name().trim();
      if(!columnName.isEmpty()){
        return columnName;
      }
    }
    return name;
  }

  public Insertion getInsertedValues(Entity value) throws Exception {
    List<String> columnToInsert = new ArrayList<>();
    List<String> fieldNames = new ArrayList<>();
    for (Field field : entityClass.getDeclaredFields()) {
      field.setAccessible(true);
      Object fieldValue = field.get(value);
      if(fieldValue != null){
        fieldNames.add("?" + field.getName());
        columnToInsert.add(getColumnName(field));
      }
      field.setAccessible(false);
    }
    return new Insertion(columnToInsert, fieldNames);
  }
}
