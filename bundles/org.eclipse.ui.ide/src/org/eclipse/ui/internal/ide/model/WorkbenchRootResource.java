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
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * An IWorkbenchAdapter implementation for IWorkspaceRoot objects.
 */
public class WorkbenchRootResource extends WorkbenchAdapter {
	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(Object)
	 * Returns the children of the root resource.
	 */
	@Override
	public Object[] getChildren(Object o) {
		IWorkspaceRoot root = (IWorkspaceRoot) o;
		return root.getProjects();
	}

	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJ_ELEMENT);
	}

	/**
	 * Returns the name of this element.  This will typically
	 * be used to assign a label to this object when displayed
	 * in the UI.
	 */
	@Override
	public String getLabel(Object o) {
		//root resource has no name
		return IDEWorkbenchMessages.Workspace;
	}
}
