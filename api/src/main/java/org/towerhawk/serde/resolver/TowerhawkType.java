package org.towerhawk.serde.resolver;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Extension
public @interface TowerhawkType {

	String[] value();

	String typeField() default "";
}
