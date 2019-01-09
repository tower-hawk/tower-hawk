package org.towerhawk.monitor.check.evaluation;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.monitor.check.evaluation.threshold.Threshold;
import org.towerhawk.monitor.check.evaluation.threshold.constant.SuccessThreshold;
import org.towerhawk.monitor.check.evaluation.transform.IdentityTransform;
import org.towerhawk.monitor.check.evaluation.transform.Transform;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Setter
@Getter
@Extension
@TowerhawkType("dag")
public class EvaluatorDAG implements Evaluator {

	@NonNull
	protected Transform transform = new IdentityTransform();
	@NonNull
	protected Threshold threshold = new SuccessThreshold();
	protected boolean addContext;
	protected boolean setMessage;
	protected Map<String, EvaluatorDAG> children = Collections.emptyMap();

	@Override
	public void evaluate(CheckRun.Builder builder, String key, ExecutionResult result) throws Exception {
		Object newValue = transform.transform(result.get(key));
		threshold.evaluate(builder, "replaceThisDefaultValue", newValue, setMessage, addContext);
		for (EvaluatorDAG t : children.values()) {
			t.evaluate(builder, key, ExecutionResult.of(key, newValue));
		}
	}
}

