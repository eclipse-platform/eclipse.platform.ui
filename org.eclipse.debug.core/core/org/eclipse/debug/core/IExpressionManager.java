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
package org.eclipse.debug.core;


import org.eclipse.debug.core.model.IExpression;

/**
 * The expression manager manages the collection of registered
 * expressions in the workspace. An expression is a snippet of code
 * that can be evaluated to produce a value. Expression creation
 * and evaluation are client responsibilities.
 * <p>
 * Clients interested in expression change notification may
 * register with the expression manager - see
 * <code>IExpressionListener</code> and <code>IExpressionsListener</code>.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @see org.eclipse.debug.core.model.IExpression
 * @see org.eclipse.debug.core.IExpressionListener
 * @see org.eclipse.debug.core.IExpressionsListener
 * @since 2.0
 */
public interface IExpressionManager {
	/**
	 * Adds the given expression to the collection of registered expressions
	 * in the workspace and notifies all registered listeners. This has no effect
	 * if the given expression is already registered.
	 *
	 * @param expression the expression to add
	 */
	public void addExpression(IExpression expression);
	
	/**
	 * Adds the given expressions to the collection of registered expressions
	 * in the workspace and notifies all registered listeners. Has no effect
	 * on expressions already registered.
	 *
	 * @param expressions the expressions to add
	 * @since 2.1
	 */
	public void addExpressions(IExpression[] expressions);	
		
	/**
	 * Returns a collection of all registered expressions, 
	 * possibly empty.
	 *
	 * @return an array of expressions
	 */
	public IExpression[] getExpressions();
	
	/**
	 * Returns whether there are any registered expressions
	 * 
	 * @return whether there are any registered expressions
	 */
	public boolean hasExpressions();
	
	/**
	 * Returns a collection of all expressions registered for the
	 * given debug model,possibly empty.
	 *
	 * @param modelIdentifier identifier of a debug model plug-in
	 * @return an array of expressions
	 */
	public IExpression[] getExpressions(String modelIdentifier);
		
	/**
	 * Removes the given expression from the expression manager,
	 * and notifies all registered listeners. Has no effect if the
	 * given expression is not currently registered.
	 *
	 * @param expression the expression to remove
	 */
	public void removeExpression(IExpression expression);
	
	/**
	 * Removes the given expressions from the collection of registered expressions
	 * in the workspace and notifies all registered listeners. Has no effect
	 * on expressions not already registered.
	 *
	 * @param expressions the expressions to remove
	 * @since 2.1
	 */
	public void removeExpressions(IExpression[] expressions);		

	/**
	 * Adds the given listener to the collection of registered expression listeners.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener to add
	 */
	public void addExpressionListener(IExpressionListener listener);

	/**
	 * Removes the given listener from the collection of registered expression listeners.
	 * Has no effect if an identical listener is not already registered.
	 *
	 * @param listener the listener to remove	
	 */
	public void removeExpressionListener(IExpressionListener listener);
	
	/**
	 * Adds the given listener to the collection of registered expression listeners.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener to add
	 * @since 2.1
	 */
	public void addExpressionListener(IExpressionsListener listener);

	/**
	 * Removes the given listener from the collection of registered expression listeners.
	 * Has no effect if an identical listener is not already registered.
	 *
	 * @param listener the listener to remove	
	 * @since 2.1
	 */
	public void removeExpressionListener(IExpressionsListener listener);	
	
}


