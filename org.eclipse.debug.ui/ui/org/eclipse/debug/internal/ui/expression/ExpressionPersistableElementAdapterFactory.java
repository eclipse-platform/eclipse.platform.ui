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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.IPersistableElement;

/**
 * Adapts an expression to IPersistableElement.
 * The adapter will save the expression to a memento.
 * 
 * @see ExpressionPersistableElementAdapter
 * @see ExpressionFactory
 * @since 3.9
 */
public class ExpressionPersistableElementAdapterFactory implements
		IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IPersistableElement.class)
			if (adaptableObject instanceof IExpression)
				return new ExpressionPersistableElementAdapter((IExpression) adaptableObject);
		
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] {IPersistableElement.class};
	}

}
