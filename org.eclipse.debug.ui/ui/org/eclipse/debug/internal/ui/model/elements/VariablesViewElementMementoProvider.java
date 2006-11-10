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
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 */
public class VariablesViewElementMementoProvider extends ElementMementoProvider {

	/**
	 * memento attribute
	 */
	private static final String ELEMENT_NAME = "ELEMENT_NAME"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementMementoProvider#encodeElement(java.lang.Object, org.eclipse.ui.IMemento)
	 */
	protected boolean encodeElement(Object element, IMemento memento) throws CoreException {
		if (element instanceof IStackFrame) {
			IStackFrame frame = (IStackFrame) element;
			memento.putString(ELEMENT_NAME, frame.getName());
		} else if (element instanceof IVariable) {
			IVariable variable = (IVariable) element;
			memento.putString(ELEMENT_NAME, variable.getName());
		} else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementMementoProvider#isEqual(java.lang.Object, org.eclipse.ui.IMemento)
	 */
	protected boolean isEqual(Object element, IMemento memento) throws CoreException {
		String mementoName = memento.getString(ELEMENT_NAME);
		if (mementoName != null) {
			String elementName = null;
			if (element instanceof IStackFrame) {
				IStackFrame frame = (IStackFrame) element;
				elementName = frame.getName();
			} else if (element instanceof IVariable) {
				IVariable variable = (IVariable) element;
				elementName = variable.getName();
			}
			if (elementName != null) {
				return elementName.equals(mementoName);
			}
		}
		return false;
	}

}
