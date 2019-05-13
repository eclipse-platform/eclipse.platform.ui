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
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * IWorkbenchAdapter adapter for the IWorkspace object.
 */
public class WorkbenchWorkspace extends WorkbenchAdapter {
	@Override
	public Object[] getChildren(Object o) {
		IWorkspace workspace = (IWorkspace) o;
		return workspace.getRoot().getProjects();
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/**
	 * getLabel method comment.
	 */
	@Override
	public String getLabel(Object o) {
		//workspaces don't have a name
		return IDEWorkbenchMessages.Workspace;
	}
}
