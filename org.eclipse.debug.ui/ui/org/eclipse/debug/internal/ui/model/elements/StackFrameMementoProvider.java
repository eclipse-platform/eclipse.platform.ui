/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Memento provider for stack frames.
 * 
 * @since 3.4
 */
public class StackFrameMementoProvider extends DebugElementMementoProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementMementoProvider#getElementName(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	protected String getElementName(Object element, IPresentationContext context) throws CoreException {
		if (element instanceof IStackFrame) {
			IStackFrame frame = (IStackFrame) element;
			if (IDebugUIConstants.ID_REGISTER_VIEW.equals(context.getId())) {
				// for registers view attempt to maintain expansion for target rather than each frame
				return frame.getModelIdentifier();
			} else {
				return frame.getName();
			}
		}
		return null;
	}

}
