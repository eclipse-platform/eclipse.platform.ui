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
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.model.elements.ViewerInputProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @since 3.4
 */
public class StackFrameViewerInputProvider extends ViewerInputProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ViewerInputProvider#getViewerInput(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected Object getViewerInput(Object source, IPresentationContext context, IViewerUpdate update) throws CoreException {
		return new RegisterGroupProxy((IStackFrame) source);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ViewerInputProvider#supportsContextId(java.lang.String)
	 */
	protected boolean supportsContextId(String id) {
		return IDebugUIConstants.ID_REGISTER_VIEW.equals(id);
	}

}
