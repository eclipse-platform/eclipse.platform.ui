/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.expressions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.internal.expressions.Assert;
import org.eclipse.core.internal.expressions.ExpressionMessages;
import org.eclipse.core.internal.expressions.ExpressionStatus;

/**
 * A default implementation of an evaluation context.
 * <p>
 * Clients may instantiate this default context. The class is
 * not intended to be subclassed by clients.
 * </p> 
 * 
 * @since 3.0
 */
public class EvaluationContext implements IEvaluationContext {

	private IEvaluationContext fParent;
	private Object fDefaultVariable;
	private Map/*<String, Object>*/ fVariables;
	private IVariableResolver[] fVariableResolvers;
	
	/**
	 * Create a new evaluation context with the given parent and default
	 * variable.
	 * 
	 * @param parent the parent context. Can be <code>null</code>.
	 * @param defaultVariable the default variable
	 */
	public EvaluationContext(IEvaluationContext parent, Object defaultVariable) {
		Assert.isNotNull(defaultVariable);
		fParent= parent;
		fDefaultVariable= defaultVariable;
	}
	
	/**
	 * Create a new evaluation context with the given parent and default
	 * variable.
	 * 
	 * @param parent the parent context. Can be <code>null</code>.
	 * @param defaultVariable the default variable
	 * @param resolvers an array of <code>IVariableResolvers</code> to
	 *  resolve additional variables.
	 * 
	 * @see #resolveVariable(String, Object[])
	 */
	public EvaluationContext(IEvaluationContext parent, Object defaultVariable, IVariableResolver[] resolvers) {
		Assert.isNotNull(defaultVariable);
		Assert.isNotNull(resolvers);
		fParent= parent;
		fDefaultVariable= defaultVariable;
		fVariableResolvers= resolvers;
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
		if (fParent == null)
			return this;
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
	public void addVariable(String name, Object value) {
		Assert.isNotNull(name);
		Assert.isNotNull(value);
		if (fVariables == null)
			fVariables= new HashMap();
		fVariables.put(name, value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object removeVariable(String name) {
		Assert.isNotNull(name);
		if (fVariables == null)
			return null;
		return fVariables.remove(name);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getVariable(String name) {
		Assert.isNotNull(name);
		Object result= null;
		if (fVariables != null) {
			result= fVariables.get(name);
		}
		if (result != null)
			return result;
		if (fParent != null)
			return fParent.getVariable(name);
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object resolveVariable(String name, Object[] args) throws CoreException {
		if (PLUGIN_DESCRIPTOR.equals(name)) {
			if (args == null ||args.length != 1)
				throw new CoreException(new ExpressionStatus(
					ExpressionStatus.VARAIBLE_POOL_WRONG_NUMBER_OF_ARGUMENTS,
					ExpressionMessages.getFormattedString(
						"VariablePool.resolveVariable.arguments.wrong_number",  //$NON-NLS-1$
						new Object[] { "1", args == null ? "0" : ("" + args.length)}))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (!(args[0] instanceof String)) 
				throw new CoreException(new ExpressionStatus(
					ExpressionStatus.VARAIBLE_POOL_ARGUMENT_IS_NOT_A_STRING,
					ExpressionMessages.getString("VariablePool.resolveVariable.arguments.not_a_string"))); //$NON-NLS-1$
			return Platform.getPluginRegistry().getPluginDescriptor((String)(args[0]));
		}
		if (fVariableResolvers != null && fVariableResolvers.length > 0) {
			for (int i= 0; i < fVariableResolvers.length; i++) {
				IVariableResolver resolver= fVariableResolvers[i];
				Object variable= resolver.resolve(name, args);
				if (variable != null)
					return variable;
			}
		}
		if (fParent != null)
			return fParent.resolveVariable(name, args);
		return null;
	}
}
