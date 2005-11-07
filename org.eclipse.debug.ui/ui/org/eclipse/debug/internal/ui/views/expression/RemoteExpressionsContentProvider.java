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

import org.eclipse.debug.internal.ui.views.RemoteTreeContentManager;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.debug.internal.ui.views.variables.RemoteVariablesContentProvider;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.ui.IWorkbenchPartSite;

public class RemoteExpressionsContentProvider extends RemoteVariablesContentProvider {

    /**
     * @param viewer
     * @param site
     * @param view
     */
    public RemoteExpressionsContentProvider(RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
        super(viewer, site, view);
    }
    
    protected RemoteTreeContentManager createContentManager(RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
		return new RemoteExpressionContentManager(this, viewer, site, view);
	}    
    
}
