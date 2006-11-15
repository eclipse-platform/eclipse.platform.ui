/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.ui.IMemento;

/**
 * Memento provider for expressions view.
 * 
 * @since 3.3
 */
public class ExpressionsViewMementoProvider extends ElementMementoProvider {

	/**
	 * memento attribute
	 */
	private static final String ELEMENT_NAME = "ELEMENT_NAME"; //$NON-NLS-1$
	
	private static final String EXP_MGR = "EXP_MGR"; //$NON-NLS-1$

	protected boolean encodeElement(Object element, IMemento memento, IPresentationContext context) throws CoreException {
		if (element instanceof IExpressionManager) {
			memento.putString(ELEMENT_NAME, EXP_MGR);
		} else if (element instanceof IExpression) {
			memento.putString(ELEMENT_NAME, ((IExpression) element).getExpressionText());
		} else if (element instanceof IVariable) {
			memento.putString(ELEMENT_NAME, ((IVariable) element).getName());
		} else {
			return false;
		}
		return true;
	}

	protected boolean isEqual(Object element, IMemento memento, IPresentationContext context) throws CoreException {
		String mementoName = memento.getString(ELEMENT_NAME);
		if (mementoName != null) {
			String elementName = null;
			if (element instanceof IExpressionManager) {
				elementName = EXP_MGR;
			} else if (element instanceof IVariable) {
				elementName = ((IVariable)element).getName();
			} else if (element instanceof IExpression) {
				elementName = ((IExpression)element).getExpressionText();
			}
			if (elementName != null) {
				return elementName.equals(mementoName);
			}
		}
		return false;
	}

}
