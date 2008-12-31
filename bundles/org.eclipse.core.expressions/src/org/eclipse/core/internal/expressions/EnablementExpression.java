/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.w3c.dom.Element;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class EnablementExpression extends CompositeExpression {

	/**
	 * Creates a {@link EnablementExpression}.
	 *
	 * @param configElement the configuration element
	 */
	public EnablementExpression(IConfigurationElement configElement) {
		// config element not used yet.
	}

	/**
	 * Creates a {@link EnablementExpression}.
	 *
	 * @param element the XML element
	 */
	public EnablementExpression(Element element) {
		// element not used yet.
	}

	public boolean equals(final Object object) {
		if (!(object instanceof EnablementExpression))
			return false;

		final EnablementExpression that= (EnablementExpression)object;
		return equals(this.fExpressions, that.fExpressions);
	}

	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		long start= 0;
		if (Expressions.TRACING)
			start= System.currentTimeMillis();
		EvaluationResult result= evaluateAnd(context);
		if (Expressions.TRACING) {
			System.out.println("[Enablement Expression] - evaluation time: " + //$NON-NLS-1$
				(System.currentTimeMillis() - start) + " ms."); //$NON-NLS-1$
		}
		return result;
	}
}
