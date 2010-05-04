package org.eclipse.e4.ui.internal.services;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;

public class ActiveContextsFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context, Object[] arguments) {
		IEclipseContext childContext = (IEclipseContext) context
				.getLocal(IContextConstants.ACTIVE_CHILD);
		if (childContext != null && arguments.length == 0) {
			return childContext.get(IServiceConstants.ACTIVE_CONTEXTS);
		}
		Set<String> rc = null;
		if (arguments.length == 0) {
			rc = new HashSet<String>();
		} else {
			rc = (Set<String>) arguments[0];
		}
		Set<String> locals = (Set<String>) context
				.getLocal(ContextContextService.LOCAL_CONTEXTS);
		if (locals != null) {
			rc.addAll(locals);
		}
		IEclipseContext parent = context.getParent();
		if (parent != null) {
			parent.get(IServiceConstants.ACTIVE_CONTEXTS,
					new Object[] { rc });
		}
		return rc;
	}

}
