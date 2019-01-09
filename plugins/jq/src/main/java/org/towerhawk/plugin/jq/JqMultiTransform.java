package org.towerhawk.plugin.jq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import org.pf4j.Extension;
import org.towerhawk.monitor.check.evaluation.transform.Transform;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.List;

@Slf4j
@Extension
@TowerhawkType("jq-multi")
public class JqMultiTransform implements Transform<List<JsonNode>> {

	protected transient ObjectMapper mapper = new ObjectMapper();
	@Getter
	protected transient JsonQuery jsonQuery;
	@Getter
	protected String query;

	public void setQuery(String query) {
		this.query = query;
		try {
			jsonQuery = JsonQuery.compile(query);
		} catch (JsonQueryException e) {
			log.error("Unable to compile jq query '{}'", query, e);
			throw new RuntimeException("Unable to compile jq query " + query, e);
		}
	}

	@JsonIgnore //Required to get Jackson to not throw an exception
	public ObjectMapper getObjectMapper() {
		return mapper;
	}

	@Override
	public List<JsonNode> transform(Object value) throws Exception {
		JsonNode in = mapper.readTree(value.toString());
		List<JsonNode> result = jsonQuery.apply(in);
		return result;
	}
}
