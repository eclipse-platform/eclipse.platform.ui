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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * DebugWorkbenchContentProvider
 */
public class DebugViewContentProvider extends BaseWorkbenchContentProvider {
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof IStackFrame) {
			return false;
		}
		if (element instanceof IDebugTarget) {
			try {
				return ((IDebugTarget)element).hasThreads();
			} catch (DebugException e) {
				return false;
			}
		} 
		if (element instanceof IThread) {
			try {
				return ((IThread)element).hasStackFrames();
			} catch (DebugException e) {
				return false;
			}
		}
		if (element instanceof IProcess) {
			return false;
		}
		if (element instanceof ILaunch) {
			return ((ILaunch)element).hasChildren();
		}
		if (element instanceof ILaunchManager) {
			return ((ILaunchManager) element).getLaunches().length > 0;
		}
		if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IDeferredWorkbenchAdapter adapter = (IDeferredWorkbenchAdapter) adaptable.getAdapter(IDeferredWorkbenchAdapter.class);
            if (adapter != null) {
                return adapter.isContainer();
            }
        }
		return super.hasChildren(element);
	}    
}
