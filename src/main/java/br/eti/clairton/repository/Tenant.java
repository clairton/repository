package br.eti.clairton.repository;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Annotação Interceptar construção de controllers para adicionar tenant.
 * 
 * @author Clairton Rodrigo Heinzen<clairton.rodrigo@gmail.com>
 */
@Inherited
@Documented
@Qualifier
@Target({ TYPE, CONSTRUCTOR, PARAMETER, FIELD, METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Tenant {
}