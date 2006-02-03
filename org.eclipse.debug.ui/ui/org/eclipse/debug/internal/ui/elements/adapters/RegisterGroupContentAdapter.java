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
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

public class RegisterGroupContentAdapter extends AsynchronousContentAdapter {

	protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
		return ((IRegisterGroup)parent).getRegisters();
	}

	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
		return ((IRegisterGroup)element).hasRegisters();
	}

	protected boolean supportsPartId(String id) {
		return id.equals(IDebugUIConstants.ID_REGISTER_VIEW);
	}   
}
