package persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinColumn {
  String name() default "";

  String referencedColumnName() default "";

  boolean unique() default false;

  boolean nullable() default true;

  String type() default "";

  String defaultValue() default "";
}
