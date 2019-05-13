/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.menus;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.tests.TestPlugin;
import org.osgi.framework.FrameworkUtil;

/**
 * @since 3.1
 */
public class RemoveMarkersAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;


	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			IMarker[] markers = root.findMarkers(AddMarkersAction.CATEGORY_TEST_MARKER, false, IResource.DEPTH_ZERO);

			for (IMarker marker : markers) {
				String message = (String) marker.getAttribute(IMarker.MESSAGE);

				if (message != null && message.startsWith("this is a test")) {
					marker.delete();
				}
			}
		} catch (CoreException e) {
			openError(e);
		}
	}

	private void openError(Exception e) {
		String msg = e.getMessage();
		if (msg == null) {
			msg = e.getClass().getName();
		}

		e.printStackTrace();
		String bundleId = FrameworkUtil.getBundle(RemoveMarkersAction.class).getSymbolicName();

		IStatus status = new Status(IStatus.ERROR, bundleId, 0, msg, e);

		TestPlugin.getDefault().getLog().log(status);

		ErrorDialog.openError(window.getShell(), "Error", msg, status);
	}


	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
