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
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 */
public class VariablesViewElementMementoProvider extends ElementMementoProvider {

	/**
	 * memento attribute
	 */
	private static final String ELEMENT_NAME = "ELEMENT_NAME"; //$NON-NLS-1$

	protected boolean encodeElement(Object element, IMemento memento, IPresentationContext context) throws CoreException {
		if (element instanceof IStackFrame) {
			IStackFrame frame = (IStackFrame) element;
			if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId())) {
				// for registers view attempt to maintain expansion for target rather than each frame
				memento.putString(ELEMENT_NAME, frame.getModelIdentifier());
			} else {
				memento.putString(ELEMENT_NAME, frame.getName());
			}
		} else if (element instanceof IVariable) {
			memento.putString(ELEMENT_NAME, ((IVariable) element).getName());
		} else if (element instanceof IRegisterGroup) {
			memento.putString(ELEMENT_NAME, ((IRegisterGroup) element).getName());
		} else {
			return false;
		}
		return true;
	}

	protected boolean isEqual(Object element, IMemento memento, IPresentationContext context) throws CoreException {
		String mementoName = memento.getString(ELEMENT_NAME);
		if (mementoName != null) {
			String elementName = null;
			if (element instanceof IStackFrame) {
				IStackFrame frame = (IStackFrame) element;
				if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId())) {
					// for registers view attempt to maintain expansion for target rather than each frame
					elementName = frame.getModelIdentifier();
				} else {
					elementName = frame.getName();
				}
			} else if (element instanceof IVariable) {
				elementName = ((IVariable)element).getName();
			} else if (element instanceof IRegisterGroup) {
				elementName = ((IRegisterGroup)element).getName();
			}
			if (elementName != null) {
				return elementName.equals(mementoName);
			}
		}
		return false;
	}

}
