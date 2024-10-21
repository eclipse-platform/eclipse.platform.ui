package org.eclipse.ui.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.condition.Condition;

/**
 * @since 3.5
 *
 */
@Component(service = Condition.class, property = "osgi.condition.id=commandAndContext")
public class CommandAndContextCondition implements Condition {

	@Reference
	CommandToModelProcessor commandToModelProcessor;

	@Reference
	ContextToModelProcessor contextToModelProcessor;
}
