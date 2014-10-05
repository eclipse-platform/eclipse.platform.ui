/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.menus;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.views.markers.MarkerViewUtil;

/**
 * @since 3.1
 */
public class AddMarkersAction implements IWorkbenchWindowActionDelegate {

	static final String CATEGORY_TEST_MARKER = "org.eclipse.ui.tests.categoryTestMarker";

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow workbenchWindow) {
	}

	@Override
	public void run(IAction action) {

		Job addJob = new Job("Add Markers") {
			@Override
			protected IStatus run(
					org.eclipse.core.runtime.IProgressMonitor monitor) {
				try {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
							.getRoot();
					Map attribs = new HashMap();
					for (int i = 0; i < 1000; i++) {

						if (i / 2 == 0) {
							attribs.put(MarkerViewUtil.NAME_ATTRIBUTE,
									"Test Name " + i);
							attribs.put(MarkerViewUtil.PATH_ATTRIBUTE,
									"Test Path " + i);
						}

						attribs.put(IMarker.SEVERITY, new Integer(
								IMarker.SEVERITY_ERROR));
						attribs.put(IMarker.MESSAGE, "this is a test " + i);
						attribs.put(IMarker.LOCATION, "Location " + i);
						attribs.put("testAttribute", String.valueOf(i / 2));
						MarkerUtilities.createMarker(root, attribs,
								CATEGORY_TEST_MARKER);
					}
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			};
		};

		addJob.schedule();

	}


	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
