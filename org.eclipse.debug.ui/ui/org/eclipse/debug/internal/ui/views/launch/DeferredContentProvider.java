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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.DeferredTreeContentManager;

/**
 * Provides deferred content for the debug view.
 * @since 3.1
 */
public class DeferredContentProvider extends DebugViewContentProvider {
    
    private DeferredTreeContentManager fManager;
    
    public DeferredContentProvider(AbstractTreeViewer tree, IWorkbenchPartSite site) {
        fManager = new DeferredTreeContentManager(this, tree, site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        Object[] children = fManager.getChildren(parentElement);
        if (children == null) {
            children = super.getChildren(parentElement);
        }
        return children;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        fManager.cancel(DebugPlugin.getDefault().getLaunchManager());
    }

}
