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
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.RemoteTreeContentManager;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * Remote content manager for variables. Creates an appropriate adapter for
 * logical structures.
 */
public class RemoteVariableContentManager extends RemoteTreeContentManager {

    private VariablesView fView;
    
    /**
     * Constructs a remote content manager for a variables view.
     */
    public RemoteVariableContentManager(ITreeContentProvider provider, RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
        super(provider, viewer, site);
        fView = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.progress.DeferredTreeContentManager#getAdapter(java.lang.Object)
     */
    protected IDeferredWorkbenchAdapter getAdapter(Object element) {
        if (element instanceof IVariable && fView !=null && fView.isShowLogicalStructure()) {
            return new DeferredLogicalStructure();
        }
        if (element instanceof IRegisterGroup) {
            return new DeferredRegisterGroup();
        }
        return super.getAdapter(element);
    }
}
