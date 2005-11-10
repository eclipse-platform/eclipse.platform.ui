/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		public void collectExpressionInfo(ExpressionInfo info) {
		}
	};
	
	/**
	 * The expression corresponding to {@link EvaluationResult#FALSE}.
	 */
	public static final Expression FALSE= new Expression() {
		public EvaluationResult evaluate(IEvaluationContext context) {
			return EvaluationResult.FALSE;
		}
		public void collectExpressionInfo(ExpressionInfo info) {
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
	
	/**
	 * Computes the expression information for the given expression tree.
	 * <p>
	 * This is a convenience method for collecting the expression information
	 * using {@link Expression#collectExpressionInfo(ExpressionInfo)}.
	 * </p>
	 * 
	 * @return the expression information 
	 * @since 3.2
	 */
	public final ExpressionInfo computeExpressionInfo() {
		ExpressionInfo result= new ExpressionInfo();
		collectExpressionInfo(result);
		return result;
	}
	
	/**
	 * Collects information about this expression tree. This default
	 * implementation add the expression's type to the set of misbehaving
	 * expression types.
	 * 
	 * @param info the expression information object used
	 *  to collect the information
	 * @since 3.2
	 */
	public void collectExpressionInfo(ExpressionInfo info) {
		info.addMisBehavingExpressionType(getClass());
	}
}