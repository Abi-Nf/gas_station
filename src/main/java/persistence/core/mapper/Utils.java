package persistence.core.mapper;

public class Utils {
  public static boolean isPrimitiveOrWrapper(Class<?> clazz){
    return clazz.isPrimitive() || isWrapper(clazz);
  }

  public static boolean isWrapper(Class<?> clazz){
    return clazz.equals(Integer.class) ||
      clazz.equals(Boolean.class) ||
      clazz.equals(Character.class) ||
      clazz.equals(Byte.class) ||
      clazz.equals(Short.class) ||
      clazz.equals(Long.class) ||
      clazz.equals(Float.class) ||
      clazz.equals(Double.class);
  }
}
