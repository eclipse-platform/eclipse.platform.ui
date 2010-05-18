package org.eclipse.e4.ui.internal.services;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;

public class ActiveContextsFunction extends ContextFunction {

	@Override
	public Object compute(IEclipseContext context) {
		// 1) get active child
		IEclipseContext current = context;
		IEclipseContext child = (IEclipseContext) current.getLocal(IContextConstants.ACTIVE_CHILD);
		while (child != null) {
			current = child;
			child = (IEclipseContext) current.getLocal(IContextConstants.ACTIVE_CHILD);
		}
		//2 form an answer going up
		Set<String> rc = new HashSet<String>();
		while (current != null) {
			Set<String> locals = (Set<String>) current.getLocal(ContextContextService.LOCAL_CONTEXTS);
			if (locals != null)
				rc.addAll(locals);
			current = current.getParent();
		}
		return rc;
	}

}
