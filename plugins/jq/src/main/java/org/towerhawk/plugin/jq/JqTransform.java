package org.towerhawk.plugin.jq;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.monitor.check.evaluation.transform.Transform;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.List;

@Getter
@Slf4j
@Extension
@TowerhawkType("jq")
public class JqTransform implements Transform<Object> {

	protected transient JqMultiTransform jqMultiTransform = new JqMultiTransform();

	public void setQuery(String query) {
		jqMultiTransform.setQuery(query);
	}

	@Override
	public Object transform(Object value) throws Exception {
		List<JsonNode> result = jqMultiTransform.transform(value);
		for (JsonNode node : result) {
			Object val = jqMultiTransform.getObjectMapper().treeToValue(node, Object.class);
			if (val != null) {
				return val;
			}
		}
		return null;
	}
}
