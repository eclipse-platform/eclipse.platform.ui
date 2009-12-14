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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointContainer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class provides memento for the breakpoint container.
 * 
 * @since 3.6
 */
public class BreakpointContainerMementoProvider extends DebugElementMementoProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementMementoProvider#getElementName(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	protected String getElementName(Object element, IPresentationContext context) throws CoreException {
		if (element instanceof IBreakpointContainer) {
			IBreakpointContainer container = (IBreakpointContainer) element;
			IAdaptable category = container.getCategory();
			if (category != null) {
				IWorkbenchAdapter adapter = (IWorkbenchAdapter) category.getAdapter(IWorkbenchAdapter.class);
				if (adapter != null) {
					return adapter.getLabel(category);
				}
				return container.getOrganizer().getLabel();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementMementoProvider#isEqual(java.lang.Object, org.eclipse.ui.IMemento, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	protected boolean supportsContextId(String id) {
		return IDebugUIConstants.ID_BREAKPOINT_VIEW.equals(id);
	}

}
