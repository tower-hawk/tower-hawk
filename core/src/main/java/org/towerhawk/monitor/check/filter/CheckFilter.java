package org.towerhawk.monitor.check.filter;

import lombok.NonNull;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.descriptors.Filterable;
import org.towerhawk.monitor.descriptors.Prioritizable;

import java.util.Collection;
import java.util.Set;

public class CheckFilter {

	private Collection<Integer> priority;
	private Integer priorityLte;
	private Integer priorityGte;
	private Collection<String> tags;
	private Collection<String> notTags;
	private Collection<String> type;
	private Collection<String> notType;
	private Collection<String> id;
	private Collection<String> notId;

	public CheckFilter(Collection<Integer> priority, Integer priorityLte, Integer priorityGte,
										 Collection<String> tags, Collection<String> notTags,
										 Collection<String> type, Collection<String> notType,
										 Collection<String> id, Collection<String> notId
	) {
		this.priority = priority;
		this.priorityLte = priorityLte;
		this.priorityGte = priorityGte;
		this.tags = tags;
		this.notTags = notTags;
		this.type = type;
		this.notType = notType;
		this.id = id;
		this.notId = notId;
	}

	private boolean priorityFilter(Filterable check) {
		boolean priorityFilter = false;
		if (check instanceof Prioritizable) {
			Prioritizable prioritizable = (Prioritizable) check;
			priorityFilter = priority(prioritizable)
					&& priorityLte(prioritizable)
					&& priorityGte(prioritizable);
		}
		return priorityFilter;
	}

	public boolean filter(@NonNull Filterable check) {
		return tags(check)
				&& notTags(check)
				&& type(check)
				&& notType(check)
				&& id(check)
				&& notId(check)
				&& priorityFilter(check);
	}

	private boolean priority(Prioritizable prioritizable) {
		if (priority == null) {
			return true;
		}
		//no lambda for early stopping
		for (int i : priority) {
			if (prioritizable.getPriority() == i) {
				return true;
			}
		}
		return false;
	}

	private boolean priorityLte(Prioritizable prioritizable) {
		return priorityLte == null || prioritizable.getPriority() <= priorityLte;
	}

	private boolean priorityGte(Prioritizable prioritizable) {
		return priorityGte == null || prioritizable.getPriority() >= priorityGte;
	}

	private boolean tags(Filterable check) {
		return evalTags(check, tags, true);
	}

	private boolean notTags(Filterable check) {
		return evalTags(check, notTags, false);
	}

	private boolean type(Filterable check) {
		return evalString(check.getType(), type, true);
	}

	private boolean notType(Filterable check) {
		return evalString(check.getType(), notType, false);
	}

	private boolean id(Filterable check) {
		return evalString(check.getId(), id, true) || evalString(check.getAlias(), id, true);
	}

	private boolean notId(Filterable check) {
		return evalString(check.getId(), notId, false) && evalString(check.getAlias(), notId, false);
	}

	private boolean evalString(String fromCheck, Collection<String> values, boolean onMatch) {
		if (values == null) {
			return true;
		}
		for (String value : values) {
			if (value != null && value.equals(fromCheck)) {
				return onMatch;
			}
		}
		return !onMatch;
	}

	private boolean evalTags(Filterable check, Collection<String> tags, boolean onMatch) {
		if (tags == null) {
			return true;
		}
		Set<String> tagSet = check.getTags();
		for (String tag : tags) {
			if (tagSet.contains(tag)) {
				return onMatch;
			}
		}
		return !onMatch;
	}

}
