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
 * A delegate which computes the value of a watch expression
 * when provided a context. 
 * 
 * @since 3.0
 */
public interface IWatchExpressionDelegate {
	
	/**
	 * Evaluates the given expression in the given context asynchronously and
	 * notifies the given listener when the evaluation finishes.
	 * 
	 * @param expression the expression to evaluate 
	 * @param context the context of the evaluation
	 * @param listener the listener to notify when the evaluation completes
	 */
	public void evaluateExpression(String expression, IDebugElement context, IWatchExpressionListener listener);

}
