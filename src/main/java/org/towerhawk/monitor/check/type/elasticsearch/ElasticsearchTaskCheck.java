package org.towerhawk.monitor.check.type.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.type.http.HttpCheck;
import org.towerhawk.serde.resolver.CheckType;

import java.io.IOException;

@Slf4j
@CheckType("elasticTasks")
public class ElasticsearchTaskCheck extends HttpCheck {
	private ObjectMapper mapper = new ObjectMapper();

	@Override
	protected Object processResponse(String response) {
		try {
			JsonNode root = mapper.readTree(response);
			JsonNode nodes = root.get("nodes");
			int[] count = new int[] { 0 };
			nodes.fields().forEachRemaining(node -> {
				JsonNode tasks = node.getValue().get("tasks");
				tasks.fields().forEachRemaining(task -> count[0]++);
			});
			return count[0];
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
