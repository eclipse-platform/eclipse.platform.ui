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
package org.eclipse.debug.core.model;

/**
 * A watch expression is an expression that is evaluated in the context
 * of a specific stack frame, thread, debug target, process, or launch.
 * Generally, a watch expression is a snippet of code that is evaluated
 * each time a debug target suspends, or when a user provides a context
 * for an evaluation by selecting a debug target or thread. An expression
 * updates its value when it is provided with a context in which it
 * can perform an evaluation. 
 * 
 * @since 3.0
 */
public interface IWatchExpression extends IErrorReportingExpression {
	
	/**
	 * Causes this expression to update its value. When the value update is complete,
	 * a debug change event is fired.
	 */
	public void evaluate();
	/**
	 * Sets the context for this watch expression, or <code>null</code> if none.
	 * If the given context is valid for this expression, this expression may
	 * update its value. When the value update is complete, a debug change event is
	 * fired. When <code>null</code> is specified as a context, this expression
	 * may choose to retain its previous value.
	 * <p>
	 * The context is usually one of (but not limited to):
	 * <ul>
	 * <li>a launch (<code>ILaunch</code>)</li>
	 * <li>a debug target (<code>IDebugTarget</code>)</li>
	 * <li>a thread (<code>IThread</code>)</li>
	 * <li>a stack frame (<code>IStackFrame</code>)</li>
	 * </ul>
	 * </p>
	 *  
	 * @param context context in which to update this expression's value, or
	 * 	<code>null</code> if none
	 */
	public void setExpressionContext(IDebugElement context);
	/**
	 * Sets this watch expression's snippet of code. This method
	 * causes the new snippet to be evaluated immediately in
	 * the expression's last context.
	 * 
	 * @param expressionText the snippet which will be evaluated
	 */
	public void setExpressionText(String expressionText);
	/**
	 * Returns whether the result of this watch expression is pending.
	 * An expression is pending if an evaluation has been requested, but
	 * the value has not yet been returned.
	 * 
	 * @return whether this expression's result is pending
	 */
	public boolean isPending();
	/**
	 * Returns whether this expression is enabled. An enabled expression will
	 * update its value. A disabled expression will not.
	 * 
	 * @return whether this expression is enabled
	 */
	public boolean isEnabled();
	/**
	 * Sets this expression's enabled state. This method
	 * causes the new snippet to be evaluated immediately in
	 * the expression's last context.
	 * 
	 * @param enabled whether this expression should be enabled
	 */
	public void setEnabled(boolean enabled);
	/**
	 * Returns whether this expression's value is obsolete. An expression is
	 * obsolete if it has a value, is disabled, and then has a new
	 * context set.
	 * 
	 * @return whether this expression's value is obsolete
	 */
	public boolean isObsolete();

}
