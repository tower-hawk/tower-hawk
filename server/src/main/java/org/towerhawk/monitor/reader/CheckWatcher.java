package org.towerhawk.monitor.reader;

import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.MonitorService;
import org.towerhawk.spring.config.Configuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
@Named
public class CheckWatcher {

	private WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private final MonitorService service;
	private final Configuration configuration;
	private ExecutorService executor;
	private boolean keepWatching;
	private Future<?> runningTask;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	@Inject
	public CheckWatcher(MonitorService service, Configuration configuration) {
		this.keys = new HashMap<WatchKey, Path>();
		this.service = service;
		this.configuration = configuration;
		this.watcher = null;
		try {
			this.watcher = FileSystems.getDefault().newWatchService();
			Path path = Paths.get(configuration.getCheckDefinitionDir());
			WatchKey key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			keys.put(key, path);
			if (configuration.isAutomaticallyWatchFiles()) {
				start();
			}
		} catch (IOException e) {
			log.error("Not starting watcher service due to error registering watches", e);
			stop();
		}
	}

	public synchronized void start() {
		if (!running()) {
			log.debug("Starting CheckWatcher");
			keepWatching = true;
			executor = Executors.newSingleThreadExecutor();
			runningTask = executor.submit(this::processEvents);
		}
	}

	public synchronized void stop() {
		if (running()) {
			log.debug("Stopping CheckWatcher");
			keepWatching = false;
			runningTask.cancel(true);
			executor.shutdownNow();
			executor = null;
		}
	}

	public boolean running() {
		return keepWatching && executor != null && !executor.isShutdown() && !runningTask.isDone();
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	private void processEvents() {
		Thread.currentThread().setName(configuration.getWatcherThreadName());
		log.info("Starting CheckWatcher");
		while (keepWatching && !Thread.interrupted()) {

			// wait for key to be signalled
			WatchKey key;
			boolean shouldRefresh = false;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				log.info("Stopping CheckWatcher");
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				log.warn("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				if (CheckRefresher.validFile(child.toFile())) {
					shouldRefresh = true;
				}
				log.info("Detected {} on {}", event.kind().name(), child);
			}

			if (shouldRefresh) {
				log.info("Automatically calling refresh");
				service.refreshDefinitions();
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}
}