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
package org.eclipse.core.internal.expressions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.ExpressionInfo;

public class IterateExpression extends CompositeExpression {
	
	private static class IteratePool implements IEvaluationContext {
		
		private Iterator fIterator;
		private Object fDefaultVariable;
		private IEvaluationContext fParent;
		
		public IteratePool(IEvaluationContext parent, Iterator iterator) {
			Assert.isNotNull(parent);
			Assert.isNotNull(iterator);
			fParent= parent;
			fIterator= iterator;
		}
		public IEvaluationContext getParent() {
			return fParent;
		}
		public IEvaluationContext getRoot() {
			return fParent.getRoot();
		}
		public Object getDefaultVariable() {
			return fDefaultVariable;
		}
		public void addVariable(String name, Object value) {
			fParent.addVariable(name, value);
		}
		public Object removeVariable(String name) {
			return fParent.removeVariable(name);
		}
		public Object getVariable(String name) {
			return fParent.getVariable(name);
		}
		public Object resolveVariable(String name, Object[] args) throws CoreException {
			return fParent.resolveVariable(name, args);
		}
		public Object next() {
			fDefaultVariable= fIterator.next();
			return fDefaultVariable;
		}
		public boolean hasNext() {
			return fIterator.hasNext();
		}
	}
	
	private static final String ATT_OPERATOR= "operator"; //$NON-NLS-1$
	private static final int OR= 1;
	private static final int AND= 2;
	
	private int fOperator;
	
	public IterateExpression(IConfigurationElement configElement) throws CoreException {
		String opValue= configElement.getAttribute(ATT_OPERATOR);
		initializeOperatorValue(opValue);
	}
	
	public IterateExpression(String opValue) throws CoreException {
		initializeOperatorValue(opValue);
	}
	
	private void initializeOperatorValue(String opValue) throws CoreException {
		if (opValue == null) {
			fOperator= AND;
		} else {
			Expressions.checkAttribute(ATT_OPERATOR, opValue, new String[] {"and", "or"});  //$NON-NLS-1$//$NON-NLS-2$
			if ("and".equals(opValue)) { //$NON-NLS-1$
				fOperator= AND;
			} else {
				fOperator= OR;
			}
		}
	}

	/* (non-Javadoc)
	 * @see Expression#evaluate(IVariablePool)
	 */
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Object var= context.getDefaultVariable();
		Expressions.checkCollection(var, this);
		Collection col= (Collection)var;
		switch (col.size()) {
			case 0:
				return fOperator == AND ? EvaluationResult.TRUE : EvaluationResult.FALSE;
			case 1:
				if (col instanceof List)
					return evaluateAnd(new DefaultVariable(context, ((List)col).get(0)));
				// fall through
			default:
				IteratePool iter= new IteratePool(context, col.iterator());
				EvaluationResult result= fOperator == AND ? EvaluationResult.TRUE : EvaluationResult.FALSE;
				while (iter.hasNext()) {
					iter.next();
					switch(fOperator) {
						case OR:
							result= result.or(evaluateAnd(iter));
							if (result == EvaluationResult.TRUE)
								return result;
							break;
						case AND:
							result= result.and(evaluateAnd(iter));
							if (result != EvaluationResult.TRUE)
								return result;
							break;
					}
				}
				return result;
		}
	}

	public void collectExpressionInfo(ExpressionInfo info) {
		// Although we access every single variable we only mark the default
		// variable as accessed since we don't have single variables for the
		// elements.
		info.markDefaultVariableAccessed();
		super.collectExpressionInfo(info);
	}
}
