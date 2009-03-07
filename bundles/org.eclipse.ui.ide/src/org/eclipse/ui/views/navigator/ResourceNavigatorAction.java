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
package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;

/**
 * Superclass of all actions provided by the resource navigator.
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public abstract class ResourceNavigatorAction extends SelectionProviderAction {

    private IResourceNavigator navigator;

    /**
     * Creates a new instance of the class.
     */
    public ResourceNavigatorAction(IResourceNavigator navigator, String label) {
        super(navigator.getViewer(), label);
        this.navigator = navigator;
    }

    /**
     * Returns the resource navigator for which this action was created.
     */
    public IResourceNavigator getNavigator() {
        return navigator;
    }

    /**
     * Returns the resource viewer
     */
    protected Viewer getViewer() {
        return getNavigator().getViewer();
    }

    /**
     * Returns the shell to use within actions.
     */
    protected Shell getShell() {
        return getNavigator().getSite().getShell();
    }

    /**
     * Returns the workbench.
     */
    protected IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }

    /**
     * Returns the workbench window.
     */
    protected IWorkbenchWindow getWorkbenchWindow() {
        return getNavigator().getSite().getWorkbenchWindow();
    }
}
