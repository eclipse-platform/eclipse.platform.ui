/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
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
 * An action which shows the current selection in the Navigator view. For each
 * element in the selection, if it is an <code>IResource</code> it uses it
 * directly, otherwise if it is an <code>IMarker</code> it uses the marker's
 * resource, otherwise if it is an <code>IAdaptable</code>, it tries to get the
 * <code>IResource.class</code> adapter.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 *
 *              Planned to be deleted, please see Bug
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=549953
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
@Deprecated(forRemoval = true)
public class ShowInNavigatorAction extends SelectionProviderAction {
	private IWorkbenchPage page;

	/**
	 * Create a new instance of this class.
	 *
	 * @param page   the page
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
	List<IResource> getResources(IStructuredSelection selection) {
		List<IResource> v = new ArrayList<>();
		for (Object o : selection) {

			IResource resource = Adapters.adapt(o, IResource.class);
			if (resource != null) {
				v.add(resource);
			} else if (o instanceof IMarker) {
				resource = ((IMarker) o).getResource();
				v.add(resource);
			}
		}
		return v;
	}

	/**
	 * Shows the Navigator view and sets its selection to the resources selected in
	 * this action's selection provider.
	 */
	@Override
	public void run() {
		List<IResource> v = getResources(getStructuredSelection());
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
					ResourceNavigatorMessages.ShowInNavigator_errorMessage, e.getMessage(), e.getStatus());
		}
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(!getResources(selection).isEmpty());
	}
}
