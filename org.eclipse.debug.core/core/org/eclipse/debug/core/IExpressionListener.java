package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.debug.core.model.IExpression;

/**
 * An expression listener is notified of expression additions,
 * removals, and changes. Listeners register and deregister with the
 * expression manager.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IExpressionManager
 * @since 2.0
 */

public interface IExpressionListener {

	/**
	 * Notifies this listener that the given expression has been added
	 * to the expression manager.
	 *
	 * @param expression the added expression
	 */
	public void expressionAdded(IExpression expression);
	/**
	 * Notifies this listener that the given expression has been removed
	 * from the expression manager.
	 *
	 * @param expression the removed expression
	 */
	public void expressionRemoved(IExpression expression);
	
	/**
	 * Notifies this listener that the given expression has
	 * changed.
	 *
	 * @param expression the changed expression
	 */
	public void expressionChanged(IExpression expression);

}