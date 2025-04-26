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
package org.eclipse.ui.wizards.newresource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * Abstract base implementation of the standard workbench wizards
 * that create new resources in the workspace.
 * <p>
 * This class is not intended to be subclassed outside this package.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class BasicNewResourceWizard extends Wizard implements
		INewWizard {

	/**
	 * The workbench.
	 */
	private IWorkbench workbench;

	/**
	 * The current selection.
	 */
	protected IStructuredSelection selection;

	/**
	 * Creates an empty wizard for creating a new resource in the workspace.
	 */
	protected BasicNewResourceWizard() {
		super();
	}

	/**
	 * Returns the selection which was passed to <code>init</code>.
	 *
	 * @return the selection
	 */
	public IStructuredSelection getSelection() {
		return selection;
	}

	/**
	 * Returns the workbench which was passed to <code>init</code>.
	 *
	 * @return the workbench
	 */
	public IWorkbench getWorkbench() {
		return workbench;
	}

	/**
	 * The <code>BasicNewResourceWizard</code> implementation of this
	 * <code>IWorkbenchWizard</code> method records the given workbench and
	 * selection, and initializes the default banner image for the pages
	 * by calling <code>initializeDefaultPageImageDescriptor</code>.
	 * Subclasses may extend.
	 */
	@Override
	public void init(IWorkbench theWorkbench, IStructuredSelection currentSelection) {
		this.workbench = theWorkbench;
		this.selection = currentSelection;

		initializeDefaultPageImageDescriptor();
	}

	/**
	 * Initializes the default page image descriptor to an appropriate banner.
	 * By calling <code>setDefaultPageImageDescriptor</code>.
	 * The default implementation of this method uses a generic new wizard image.
	 * <p>
	 * Subclasses may reimplement.
	 * </p>
	 */
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/new_wiz.svg");//$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	/**
	 * Selects and reveals the newly added resource in all parts
	 * of the active workbench window's active page.
	 *
	 * @see ISetSelectionTarget
	 */
	protected void selectAndReveal(IResource newResource) {
		selectAndReveal(newResource, getWorkbench().getActiveWorkbenchWindow());
	}

	/**
	 * Attempts to select and reveal the specified resource in all
	 * parts within the supplied workbench window's active page.
	 * <p>
	 * Checks all parts in the active page to see if they implement <code>ISetSelectionTarget</code>,
	 * either directly or as an adapter. If so, tells the part to select and reveal the
	 * specified resource.
	 * </p>
	 *
	 * @param resource the resource to be selected and revealed
	 * @param window the workbench window to select and reveal the resource
	 *
	 * @see ISetSelectionTarget
	 */
	public static void selectAndReveal(IResource resource,
			IWorkbenchWindow window) {
		// validate the input
		if (window == null || resource == null) {
			return;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}

		// get all the view and editor parts
		List<IWorkbenchPart> parts = new ArrayList<>();
		for (IWorkbenchPartReference ref : page.getViewReferences()) {
			IWorkbenchPart part = ref.getPart(false);
			if (part != null) {
				parts.add(part);
			}
		}
		for (IWorkbenchPartReference ref : page.getEditorReferences()) {
			if (ref.getPart(false) != null) {
				parts.add(ref.getPart(false));
			}
		}

		final ISelection selection = new StructuredSelection(resource);
		Iterator<?> itr = parts.iterator();
		while (itr.hasNext()) {
			IWorkbenchPart part = (IWorkbenchPart) itr.next();

			// get the part's ISetSelectionTarget implementation
			ISetSelectionTarget target = Adapters.adapt(part, ISetSelectionTarget.class);

			if (target != null) {
				// select and reveal resource
				final ISetSelectionTarget finalTarget = target;
				window.getShell().getDisplay().asyncExec(() -> finalTarget.selectReveal(selection));
			}
		}
	}
}
