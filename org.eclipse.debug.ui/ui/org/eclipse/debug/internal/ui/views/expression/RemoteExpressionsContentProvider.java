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
package org.eclipse.debug.internal.ui.views.expression;

import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.debug.internal.ui.views.variables.RemoteVariablesContentProvider;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.DeferredTreeContentManager;

public class RemoteExpressionsContentProvider extends RemoteVariablesContentProvider {

    /**
     * @param viewer
     * @param site
     * @param view
     */
    public RemoteExpressionsContentProvider(RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
        super(viewer, site, view);
    }
    
    protected DeferredTreeContentManager createContentManager(RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
		return new RemoteExpressionContentManager(this, viewer, site, view);
	}    
    
    public Object[] getChildren(Object parent) {
        if (parent instanceof IExpressionManager) {
            return ((IExpressionManager)parent).getExpressions();
        }
        if (parent instanceof IExpression) {
            return super.getChildren(parent);
        }
        return super.getChildren(parent);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element instanceof IExpressionManager) {
            IExpressionManager manager = (IExpressionManager) element;
            return manager.hasExpressions();
        }
        if (element instanceof IExpression) {
            return true;
        }
        return super.hasChildren(element);
    }
}
