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

import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * This adapter saves an expression to a memento. 
 * It also returns ID of ExpressionFactory which is used to create a factory object, 
 * which in turn will restore the expression from the memento 
 * @since 3.9
 */
public class ExpressionPersistableElementAdapter implements IPersistableElement {
	
	public static final String TAG_EXPRESSION_TEXT = "TAG_EXPRESSION_TEXT"; //$NON-NLS-1$
	
	private IExpression fExpression;
	
	public ExpressionPersistableElementAdapter(IExpression expression) {
		this.fExpression = expression;
	}

	public void saveState(IMemento memento) {
		memento.putString(TAG_EXPRESSION_TEXT, fExpression.getExpressionText());
	}

	public String getFactoryId() {
		return ExpressionFactory.ID;
	}

}
