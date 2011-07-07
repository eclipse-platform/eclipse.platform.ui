package org.eclipse.e4.ui.services;

import javax.annotation.PostConstruct;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.internal.services.ContextContextFunction;

public class ContextServiceAddon {
	@PostConstruct
	public void init(IEclipseContext context) {
		// global context service.
		ContextManager contextManager = new ContextManager();
		context.set(ContextManager.class.getName(), contextManager);
		
		context.set(EContextService.class.getName(), new ContextContextFunction());
		context.set(IServiceConstants.ACTIVE_CONTEXTS, new ActiveContextsFunction());
	}
}
