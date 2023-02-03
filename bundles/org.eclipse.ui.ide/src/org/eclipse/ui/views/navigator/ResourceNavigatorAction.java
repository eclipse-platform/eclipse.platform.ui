/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;

/**
 * Superclass of all actions provided by the resource navigator.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 *
 *              Planned to be deleted, please see Bug
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=549953
 *
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
@Deprecated(forRemoval = true)
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
