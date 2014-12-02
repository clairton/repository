package br.eti.clairton.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Inherited
public @interface Identificador {
    Type[] value() default {};
    
    enum Type {
        TO_STRING,
        HASHCODE,
        EQUALS
    }
}
