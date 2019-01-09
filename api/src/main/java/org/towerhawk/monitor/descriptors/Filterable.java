package org.towerhawk.monitor.descriptors;

import java.util.Set;

public interface Filterable {

	/**
	 * The id for this check. This should match the dictionary key in the config yaml.
	 * This must be unique within an App.
	 *
	 * @return the Id of this check
	 */
	String getId();

	String getFullName();

	/**
	 * An alias for this check. When looking up checks by id, this method should also be
	 * consulted which allows for migration. This should be unique within an App.
	 *
	 * @return
	 */
	String getAlias();

	/**
	 * The type of the check as defined in the yaml config. This is available so
	 * that all checks of a type can be run together.
	 *
	 * @return The type defined in the config yaml.
	 */
	String getType();

	/**
	 * Returns a set of tags that are used to be able to run a subset of checks.
	 *
	 * @return The tags defined in the config yaml
	 */
	Set<String> getTags();
}
