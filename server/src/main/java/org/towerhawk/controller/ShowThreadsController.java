package org.towerhawk.controller;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
@ManagedResource(objectName = "org.towerhawk:type=Thread,name=Diagnostics", description = "Convience endpoint to check on thread health")
public class ShowThreadsController {

	enum SortPriority {
		id,
		name,
		lowerName, //name ignore case
		state
	}

	@RequestMapping(value = "/showThreads")
	public ResponseEntity showAllThreads(
			@RequestParam(defaultValue = "id") List<SortPriority> priority,
			@RequestParam(defaultValue = "false") boolean desc,
			@RequestParam(required = false) List<Thread.State> state,
			@RequestParam(required = false) List<String> name,
			@RequestParam(required = false) List<String> lowerName,
			@RequestParam(required = false) List<Long> id
	) {
		//always sort by id as a last resort
		// if it's already added it won't get evalutated twice
		priority.add(SortPriority.id);

		String threadInfo = showThreads(priority, t -> {
			boolean valid = true;
			if (state != null) {
				boolean v = false;
				for (Thread.State s : state) {
					v = v || s == t.getThreadState();
				}
				valid = valid && v;
			}
			if (name != null) {
				boolean v = false;
				for (String s : name) {
					v = v || s.equals(t.getThreadName());
				}
				valid = valid && v;
			}
			if (lowerName != null) {
				boolean v = false;
				for (String s : lowerName) {
					v = v || s.equalsIgnoreCase(t.getThreadName());
				}
				valid = valid && v;
			}
			if (id != null) {
				boolean v = false;
				for (Long l : id) {
					v = v || l == t.getThreadId();
				}
				valid = valid && v;
			}
			return valid;
		});

		return new ResponseEntity(threadInfo, HttpStatus.OK);
	}

	@ManagedOperation(description = "Provide diagnostic view of jvm threads")
	public String showThreads(@NonNull List<SortPriority> priorities, Predicate<? super ThreadInfo> predicate) {
		StringBuilder stringBuilder = new StringBuilder(50_000);

		ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
		List<ThreadInfo> threadInfo = Arrays.asList(mxBean.dumpAllThreads(false, false));

		if (predicate != null) {
			threadInfo = threadInfo.stream().filter(predicate).collect(Collectors.toList());
		}

		threadInfo.sort((c1, c2) -> {
			for (SortPriority sortPriority : priorities) {
				int sortVal = 0;
				if (sortPriority == SortPriority.id) {
					sortVal = Long.compare(c1.getThreadId(), c2.getThreadId());
				} else if (sortPriority == SortPriority.name) {
					sortVal = c1.getThreadName().compareTo(c2.getThreadName());
				} else if (sortPriority == SortPriority.lowerName) {
					sortVal = c1.getThreadName().compareToIgnoreCase(c2.getThreadName());
				} else if (sortPriority == SortPriority.state) {
					sortVal = c1.getThreadState().compareTo(c2.getThreadState());
				}
				if (sortVal != 0) {
					return sortVal;
				}
			}
			return 0;
		});

		for (ThreadInfo t : threadInfo) {
			stringBuilder.append(t.toString());
			//stringBuilder.append("-----------------------------------------------------------------------");
			//stringBuilder.append("-----------------------------------------------------------------------\n\n");
		}


		return stringBuilder.toString();
	}
}
