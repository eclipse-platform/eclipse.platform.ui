/*******************************************************************************
 * Copyright (c) 2012 Tensilica Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abeer Bagul (Tensilica Inc) - initial API and implementation (Bug 372181)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.expression;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * Factory to restore an expression from a memento.
 * @since 3.9
 */
public class ExpressionFactory implements IElementFactory {
	
	public static final String ID = "org.eclipse.debug.ui.elementfactory.expressions"; //$NON-NLS-1$

	public IAdaptable createElement(IMemento memento) {
		String expressionText = memento.getString(ExpressionPersistableElementAdapter.TAG_EXPRESSION_TEXT);
		if (expressionText != null)
		{
			IExpression[] expressions = DebugPlugin.getDefault().getExpressionManager().getExpressions();
			for (int i=0; i<expressions.length; i++) {
				IExpression expr = expressions[i];
				if (expr.getExpressionText().equals(expressionText))
					return expr;
			}
		}
		
		return null;
	}

}
