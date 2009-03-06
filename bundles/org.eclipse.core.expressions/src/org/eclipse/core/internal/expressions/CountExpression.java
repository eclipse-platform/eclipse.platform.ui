/*******************************************************************************
 * Copyright (c) 2000, 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation.
 *     Ian Phillips - additional expressions support ( "-N)", "(N-" ).
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import java.util.Collection;

import org.w3c.dom.Element;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.ICountable;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;


public class CountExpression extends Expression {

	private static final int GREATER_THAN = 7; // (N-
	private static final int LESS_THAN    = 6; // -N)
	private static final int ANY_NUMBER   = 5; // *
	private static final int EXACT        = 4; // N
	private static final int ONE_OR_MORE  = 3; // +
	private static final int NONE_OR_ONE  = 2; // ?
	private static final int NONE         = 1; // !
	private static final int UNKNOWN      = 0;

	/**
	 * The seed for the hash code for all count expressions.
	 */
	private static final int HASH_INITIAL= CountExpression.class.getName().hashCode();

	private int fMode;
	private int fSize;

	public CountExpression(IConfigurationElement configElement) {
		String size = configElement.getAttribute(ATT_VALUE);
		initializeSize(size);
	}

	public CountExpression(Element element) {
		String size = element.getAttribute(ATT_VALUE);
		initializeSize(size.length() > 0 ? size : null);
	}

	public CountExpression(String size) {
		initializeSize(size);
	}

	private void initializeSize(String size) {
		if (size == null)
			size= "*"; //$NON-NLS-1$
		if (size.equals("*")) //$NON-NLS-1$
			fMode= ANY_NUMBER;
		else if (size.equals("?")) //$NON-NLS-1$
			fMode= NONE_OR_ONE;
		else if (size.equals("!")) //$NON-NLS-1$
			fMode= NONE;
		else if (size.equals("+")) //$NON-NLS-1$
			fMode= ONE_OR_MORE;
		else if (size.charAt(0) == '-' && size.charAt(size.length() - 1) == ')') {
			try {
				fMode = LESS_THAN;
				fSize = Integer.parseInt(size.substring(1, size.length() - 1));
			} catch (NumberFormatException e) {
				fMode= UNKNOWN;
			}
		} else if (size.charAt(0) == '(' && size.charAt(size.length() - 1) == '-') {
			try {
				fMode = GREATER_THAN;
				fSize = Integer.parseInt(size.substring(1, size.length() - 1));
			} catch (NumberFormatException e) {
				fMode= UNKNOWN;
			}
		} else {
			try {
				fSize= Integer.parseInt(size);
				fMode= EXACT;
			} catch (NumberFormatException e) {
				fMode= UNKNOWN;
			}
		}
	}

	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Object var= context.getDefaultVariable();
		int size;
		if (var instanceof Collection) {
			size= ((Collection)var).size();
		} else {
			ICountable countable= Expressions.getAsICountable(var, this);
			if (countable == null)
				return EvaluationResult.NOT_LOADED;
			size= countable.count();
		}
		switch (fMode) {
			case UNKNOWN:
				return EvaluationResult.FALSE;
			case NONE:
				return EvaluationResult.valueOf(size == 0);
			case NONE_OR_ONE:
				return EvaluationResult.valueOf(size == 0 || size == 1);
			case ONE_OR_MORE:
				return EvaluationResult.valueOf(size >= 1);
			case EXACT:
				return EvaluationResult.valueOf(fSize == size);
			case ANY_NUMBER:
				return EvaluationResult.TRUE;
			case LESS_THAN:
				return EvaluationResult.valueOf(size < fSize);
			case GREATER_THAN:
				return EvaluationResult.valueOf(size > fSize);
		}
		return EvaluationResult.FALSE;
	}

	public void collectExpressionInfo(ExpressionInfo info) {
		info.markDefaultVariableAccessed();
	}

	public boolean equals(final Object object) {
		if (!(object instanceof CountExpression))
			return false;

		final CountExpression that= (CountExpression)object;
		return (this.fMode == that.fMode) && (this.fSize == that.fSize);
	}

	protected int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + fMode
			* HASH_FACTOR + fSize;
	}
}
