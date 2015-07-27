/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.commands;

import java.util.Collections;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * @noreference
 * @since 1.0
 */
public class ExpressionContext implements IEvaluationContext {
	/**
	 * See org.eclipse.e4.ui.services.IServiceConstants.ACTIVE_SELECTION
	 */
	private static final String ORG_ECLIPSE_UI_SELECTION = "org.eclipse.ui.selection"; //$NON-NLS-1$

	public static final String ALLOW_ACTIVATION = "org.eclipse.e4.core.commands.ExpressionContext.allowActivation"; //$NON-NLS-1$

	public IEclipseContext eclipseContext;
	public static IContextFunction defaultVariableConverter = null;

	public ExpressionContext(IEclipseContext eclipseContext) {
		this.eclipseContext = eclipseContext;
	}

	@Override
	public IEvaluationContext getParent() {
		IEclipseContext parent = eclipseContext.getParent();
		return parent == null ? null : new ExpressionContext(parent);
	}

	@Override
	public IEvaluationContext getRoot() {
		IEclipseContext current = eclipseContext;
		IEclipseContext parent = current.getParent();
		while (parent != null) {
			current = parent;
			parent = current.getParent();
		}
		if (current == eclipseContext) {
			return this;
		}
		return new ExpressionContext(current);
	}

	@Override
	public void setAllowPluginActivation(boolean value) {
		eclipseContext.set(ALLOW_ACTIVATION, Boolean.valueOf(value));
	}

	@Override
	public boolean getAllowPluginActivation() {
		Object obj = eclipseContext.get(ALLOW_ACTIVATION);
		return obj instanceof Boolean ? ((Boolean) obj).booleanValue() : false;
	}

	@Override
	public Object getDefaultVariable() {
		final Object sel;
		if (defaultVariableConverter != null) {
			sel = defaultVariableConverter.compute(eclipseContext, null);
		} else {
			sel = eclipseContext.getActive(ORG_ECLIPSE_UI_SELECTION);
		}
		return sel == null ? Collections.EMPTY_LIST : sel;
	}

	@Override
	public void addVariable(String name, Object value) {
		eclipseContext.set(name, value);
	}

	@Override
	public Object removeVariable(String name) {
		Object obj = eclipseContext.getLocal(name);
		eclipseContext.remove(name);
		return obj;
	}

	@Override
	public Object getVariable(String name) {
		if (IEclipseContext.class.getName().equals(name)) {
			return eclipseContext;
		}
		Object obj = eclipseContext.getActive(name);
		return obj == null ? IEvaluationContext.UNDEFINED_VARIABLE : obj;
	}

	@Override
	public Object resolveVariable(String name, Object[] args) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}
}
