package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IExpression;

/**
 * The expression manager manages the collection of registered
 * expressions in the workspace. An expression is a snippet of code
 * that can be evaluated to produce a value. Expression creation
 * and evaluation are client responsibilities.
 * <p>
 * Clients interested in expression change notification may
 * register with the expression manager - see
 * <code>IExpressionListener</code>.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IExpression
 * @see IExpressionListener
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
	
}


