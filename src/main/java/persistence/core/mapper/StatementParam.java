package persistence.core.mapper;

public class StatementParam {
  public enum Type {
    QUESTION_MARK, EXPLICIT_INDEX, FIELD_NAME;
  }

  private final long index;
  private final Object value;
  private final Type type;

  public static StatementParam parse(long index, String value) {
    if(value.equals("?")){
      return new StatementParam(index, "?", Type.QUESTION_MARK);
    }else if(value.matches("\\d+")){
      return new StatementParam(index, getInteger(value), Type.EXPLICIT_INDEX);
    }else if(value.matches("\\w+")){
      return new StatementParam(index, value, Type.FIELD_NAME);
    }
    throw new IllegalArgumentException("Invalid statement");
  }

  private StatementParam(long index, Object value, Type type){
    this.index = index;
    this.value = value;
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public Type getType(){
    return type;
  }

  public long getIndex() {
    return index;
  }

  private static Integer getInteger(String value){
    try {
      return Integer.valueOf(value);
    }catch (NumberFormatException ignored){
      return null;
    }
  }
}
