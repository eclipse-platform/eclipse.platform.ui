/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.debug.internal.ui.views.RemoteTreeContentManager;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * Provides deferred content for the debug view.
 * @since 3.1
 */
public class DebugViewContentProvider extends BaseWorkbenchContentProvider {
    
    private RemoteTreeContentManager fManager;
    
    public DebugViewContentProvider(LaunchViewer tree, IWorkbenchPartSite site) {
        fManager = new RemoteTreeContentManager(this, tree, site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IStackFrame) {
            return null;
        }
        Object[] children = fManager.getChildren(parentElement);
        if (children == null) {
            children = super.getChildren(parentElement);
        }
        return children;
    }
    
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        fManager.cancel();
        super.dispose();
    }

}
