/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

/**
 * DebugElementAdapterFactory
 */
public class DebugElementAdapterFactory implements IAdapterFactory {
    
    private static DebugElementWorkbenchAdapter fgAdapater;

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType.isInstance(adaptableObject)) {
			return adaptableObject;
		}
        if (adaptableObject instanceof IDebugElement || adaptableObject instanceof ILaunch || adaptableObject instanceof IProcess || adaptableObject instanceof ILaunchManager) {
	        if (adapterType.equals(IWorkbenchAdapter.class) || adapterType.equals(IWorkbenchAdapter2.class)) {
	            return getWorkbenchAdapter();
	        }
        }
        return null;
    }

    /**
     * Returns a workbench adapater for debug elements.
     * 
     * @return a workbench adapater for debug elements
     */
    private Object getWorkbenchAdapter() {
        if (fgAdapater == null) {
            fgAdapater = new DebugElementWorkbenchAdapter();
        }
        return fgAdapater;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return new Class[] {IWorkbenchAdapter.class, IWorkbenchAdapter2.class};
    }

}
