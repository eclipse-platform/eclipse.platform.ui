/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

public class LauchManagerContentAdapter extends AsynchronousContentAdapter {

    protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
		return ((ILaunchManager) parent).getLaunches();
	}

	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
		return ((ILaunchManager)element).getLaunches().length > 0;
	}
    
	protected boolean supportsPartId(String id) {
		return IDebugUIConstants.ID_DEBUG_VIEW.equals(id);
	}    
}
