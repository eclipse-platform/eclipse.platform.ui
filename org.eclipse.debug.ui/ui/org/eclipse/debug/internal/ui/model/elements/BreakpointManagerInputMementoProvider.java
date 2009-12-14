/*****************************************************************
 * Copyright (c) 2009 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 238956)
 *****************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Breakpoint manager input memento provider.
 * 
 * @since 3.6
 */
public class BreakpointManagerInputMementoProvider extends DebugElementMementoProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementMementoProvider#getElementName(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	protected String getElementName(Object element, IPresentationContext context) throws CoreException {
		return  "BreakpointInputMemento"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementMementoProvider#isEqual(java.lang.Object, org.eclipse.ui.IMemento, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	protected boolean supportsContextId(String id) {
		return IDebugUIConstants.ID_BREAKPOINT_VIEW.equals(id);
	}
}
