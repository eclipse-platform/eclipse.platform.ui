/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core;



import org.eclipse.debug.core.model.IExpression;

/**
 * An expression listener is notified of expression additions,
 * removals, and changes. Listeners register and unregister with the
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
