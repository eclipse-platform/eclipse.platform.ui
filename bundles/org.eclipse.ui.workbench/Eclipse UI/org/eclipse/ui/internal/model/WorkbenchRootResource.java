/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.model;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * An IWorkbenchAdapter implementation for IWorkspaceRoot objects.
 */
public class WorkbenchRootResource extends WorkbenchAdapter {
/**
 * @see IWorkbenchAdapter#getChildren
 * Returns the children of the root resource.
 */
public Object[] getChildren(Object o) {
	IWorkspaceRoot root = (IWorkspaceRoot) o;
	return root.getProjects();
}
/**
 * @see IWorkbenchAdapter#getImageDescriptor
 */
public ImageDescriptor getImageDescriptor(Object object) {
	return null;
}
/**
 * Returns the name of this element.  This will typically
 * be used to assign a label to this object when displayed
 * in the UI.
 */
public String getLabel(Object o) {
	//root resource has no name
	return WorkbenchMessages.getString("Workspace"); //$NON-NLS-1$
}
}
