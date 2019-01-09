package org.towerhawk.serde.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotating a class with this makes the TowerhawkType resolver
 * ignore the specified classes when registering for deserialization.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TowerhawkIgnore {

	/**
	 *
	 * @return List of classes found in ${@link ExtensibleAPI}
	 */
	Class[] value();
}
