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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * An action which shows the current selection in the Navigator view.
 * For each element in the selection, if it is an <code>IResource</code>
 * it uses it directly, otherwise if it is an <code>IMarker</code> it uses the marker's resource,
 * otherwise if it is an <code>IAdaptable</code>, it tries to get the <code>IResource.class</code> adapter.
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public class ShowInNavigatorAction extends SelectionProviderAction {
    private IWorkbenchPage page;

    /**
     * Create a new instance of this class.
     * 
     * @param page the page
     * @param viewer the viewer
     */
    public ShowInNavigatorAction(IWorkbenchPage page, ISelectionProvider viewer) {
        super(viewer, ResourceNavigatorMessages.ShowInNavigator_text);
        Assert.isNotNull(page);
        this.page = page;
        setDescription(ResourceNavigatorMessages.ShowInNavigator_toolTip);
        page.getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp(this,
				INavigatorHelpContextIds.SHOW_IN_NAVIGATOR_ACTION);
    }

    /**
     * Returns the resources in the given selection.
     *
     * @return a list of <code>IResource</code>
     */
    List getResources(IStructuredSelection selection) {
        List v = new ArrayList();
        for (Iterator i = selection.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof IResource) {
                v.add(o);
            } else if (o instanceof IMarker) {
                IResource resource = ((IMarker) o).getResource();
                v.add(resource);
            } else if (o instanceof IAdaptable) {
                IResource resource = (IResource) ((IAdaptable) o)
                        .getAdapter(IResource.class);
                if (resource != null) {
                    v.add(resource);
                }
            }
        }
        return v;
    }

    /*
     * (non-Javadoc)
     * Method declared on IAction.
     */
    /**
     * Shows the Navigator view and sets its selection to the resources
     * selected in this action's selection provider.
     */
    public void run() {
        List v = getResources(getStructuredSelection());
        if (v.isEmpty()) {
			return;
		}
        try {
            IViewPart view = page.showView(IPageLayout.ID_RES_NAV);
            if (view instanceof ISetSelectionTarget) {
                ISelection selection = new StructuredSelection(v);
                ((ISetSelectionTarget) view).selectReveal(selection);
            }
        } catch (PartInitException e) {
            ErrorDialog.openError(page.getWorkbenchWindow().getShell(),
                    ResourceNavigatorMessages.ShowInNavigator_errorMessage,
                    e.getMessage(), e.getStatus());
        }
    }

    /*
     * (non-Javadoc)
     * Method declared on SelectionProviderAction.
     */
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(!getResources(selection).isEmpty());
    }
}
