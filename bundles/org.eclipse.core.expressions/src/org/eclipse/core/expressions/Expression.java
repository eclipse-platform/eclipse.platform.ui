/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.expressions;

import org.eclipse.core.runtime.CoreException;

/**
 * Abstract base class for all expressions provided by the common
 * expression language.
 * <p>
 * An expression is evaluated by calling {@link #evaluate(IEvaluationContext)}.
 * </p>
 * <p>
 * This class may be subclassed to provide specific expressions.
 * </p>
 * 
 * @since 3.0
 */
public abstract class Expression {
	
	/**
	 * Name of the value attribute of an expression (value is 
	 * <code>value</code>).
	 */ 
	protected static final String ATT_VALUE= "value"; //$NON-NLS-1$
	
	/**
	 * The expression corresponding to {@link EvaluationResult#TRUE}.
	 */
	public static final Expression TRUE= new Expression() {
		public EvaluationResult evaluate(IEvaluationContext context) {
			return EvaluationResult.TRUE;
		}	
	};
	
	/**
	 * The expression corresponding to {@link EvaluationResult#FALSE}.
	 */
	public static final Expression FALSE= new Expression() {
		public EvaluationResult evaluate(IEvaluationContext context) {
			return EvaluationResult.FALSE;
		}	
	};
	
	/**
	 * Evaluates this expression. 
	 * 
	 * @param context an evaluation context providing information like variable,
	 *  name spaces, etc. necessary to evaluate this expression
	 * 
	 * @return the result of the expression evaluation
	 * 
	 * @throws CoreException if the evaluation failed. The concrete reason is 
	 *  defined by the subclass implementing this method
	 */
	public abstract EvaluationResult evaluate(IEvaluationContext context) throws CoreException;
	
}
