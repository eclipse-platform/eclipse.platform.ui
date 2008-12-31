/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * An evaluation context that can be used to add a new default variable
 * to a hierarchy of evaluation contexts.
 *
 * @since 3.0
 */
public final class DefaultVariable implements IEvaluationContext {

	private Object fDefaultVariable;
	private IEvaluationContext fParent;
	private IEvaluationContext fManagedPool;

	/**
	 * Constructs a new variable pool for a single default variable.
	 *
	 * @param parent the parent context for the default variable. Must not
	 *  be <code>null</code>.
	 * @param defaultVariable the default variable
	 */
	public DefaultVariable(IEvaluationContext parent, Object defaultVariable) {
		Assert.isNotNull(parent);
		Assert.isNotNull(defaultVariable);
		fParent= parent;
		while (parent instanceof DefaultVariable) {
			parent= parent.getParent();
		}
		fManagedPool= parent;
		fDefaultVariable= defaultVariable;
	}

	/**
	 * {@inheritDoc}
	 */
	public IEvaluationContext getParent() {
		return fParent;
	}

	/**
	 * {@inheritDoc}
	 */
	public IEvaluationContext getRoot() {
		return fParent.getRoot();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getDefaultVariable() {
		return fDefaultVariable;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAllowPluginActivation(boolean value) {
		fParent.setAllowPluginActivation(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getAllowPluginActivation() {
		return fParent.getAllowPluginActivation();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addVariable(String name, Object value) {
		fManagedPool.addVariable(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object removeVariable(String name) {
		return fManagedPool.removeVariable(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getVariable(String name) {
		return fManagedPool.getVariable(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object resolveVariable(String name, Object[] args) throws CoreException {
		return fManagedPool.resolveVariable(name, args);
	}
}
