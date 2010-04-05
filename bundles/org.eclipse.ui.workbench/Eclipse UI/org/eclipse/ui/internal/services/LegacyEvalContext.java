package org.eclipse.ui.internal.services;

import org.eclipse.e4.core.contexts.IEclipseContext;

import java.util.Collections;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ISources;

public class LegacyEvalContext implements IEvaluationContext {
	public IEclipseContext eclipseContext = null;
	private boolean allowActivation;

	public LegacyEvalContext(IEclipseContext context) {
		eclipseContext = context;
	}

	public void setAllowPluginActivation(boolean value) {
		allowActivation = value;
	}

	public Object resolveVariable(String name, Object[] args)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object removeVariable(String name) {
		Object o = eclipseContext.get(name);
		eclipseContext.remove(name);
		return o;
	}

	public Object getVariable(String name) {
		final Object obj = eclipseContext.get(name);
		return obj == null ? IEvaluationContext.UNDEFINED_VARIABLE : obj;
	}

	public IEvaluationContext getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	public IEvaluationContext getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getDefaultVariable() {
		final Object obj = eclipseContext
				.get(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		return obj == null ? Collections.EMPTY_LIST : obj;
	}

	public boolean getAllowPluginActivation() {
		return allowActivation;
	}

	public void addVariable(String name, Object value) {
		eclipseContext.set(name, value);
	}
}
