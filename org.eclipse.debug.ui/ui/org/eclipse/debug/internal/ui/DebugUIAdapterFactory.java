/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

 
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainerWorkbenchAdapter;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointPersistableElementAdapter;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

public class DebugUIAdapterFactory implements IAdapterFactory {

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(Object, Class)
	 */
	public Object getAdapter(Object obj, Class adapterType) {
		if (adapterType.isInstance(obj)) {
			return obj;
		}
		
		if (adapterType == IPersistableElement.class) {
			if (obj instanceof IBreakpoint) {
			    return new BreakpointPersistableElementAdapter((IBreakpoint)obj);
            }
		}
        
        if (adapterType == IWorkbenchAdapter.class) {
            if (obj instanceof IBreakpointContainer) {
                return new BreakpointContainerWorkbenchAdapter();
            }
        }
		
        if (adapterType == IWorkbenchAdapter2.class) {
            if (obj instanceof IBreakpointContainer) {
                return new BreakpointContainerWorkbenchAdapter();
            }
        }
        
		return null;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] {IPersistableElement.class, IWorkbenchAdapter.class, IWorkbenchAdapter2.class};
	}

}
