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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.preferences.DebugWorkInProgressPreferencePage;
import org.eclipse.debug.internal.ui.views.RemoteTreeContentManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * Provides deferred content for the debug view.
 * @since 3.1
 */
public class DebugViewContentProvider extends BaseWorkbenchContentProvider implements IPropertyChangeListener {
    
    private DeferredTreeContentManager fManager;
    
    // TODO: work in progress - to be removed
    private boolean fUseDeferredContent = false;
    
    public DebugViewContentProvider(LaunchViewer tree, IWorkbenchPartSite site) {
        fManager = new RemoteTreeContentManager(this, tree, site);
        // TODO: remove work in progress
        IPreferenceStore preferenceStore = DebugUITools.getPreferenceStore();
		fUseDeferredContent = preferenceStore.getBoolean(DebugWorkInProgressPreferencePage.WIP_PREF_USE_BACKGROUND_CONTENT);
		preferenceStore.addPropertyChangeListener(this);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        Object[] children = null;
        if (fUseDeferredContent) {
        	children = fManager.getChildren(parentElement);
        }
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
        fManager.cancel(DebugPlugin.getDefault().getLaunchManager());
        // TODO: remove work in progress
        DebugUITools.getPreferenceStore().removePropertyChangeListener(this);
        super.dispose();
    }

	/* (non-Javadoc)
	 * 
	 * TODO: remove work in progress
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(DebugWorkInProgressPreferencePage.WIP_PREF_USE_BACKGROUND_CONTENT)) {
			fUseDeferredContent = DebugUITools.getPreferenceStore().getBoolean(DebugWorkInProgressPreferencePage.WIP_PREF_USE_BACKGROUND_CONTENT);
		}
		
	}

}
