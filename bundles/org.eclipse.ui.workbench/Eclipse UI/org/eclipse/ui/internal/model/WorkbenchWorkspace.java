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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * IWorkbenchAdapter adapter for the IWorkspace object.
 */
public class WorkbenchWorkspace extends WorkbenchAdapter {
/**
 * @see IWorkbenchAdapter#getChildren
 * Returns the children of the workspace.
 */
public Object[] getChildren(Object o) {
	IWorkspace workspace = (IWorkspace) o;
	return workspace.getRoot().getProjects();
}
/**
 * @see IWorkbenchAdapter#getImageDescriptor
 */
public ImageDescriptor getImageDescriptor(Object object) {
	return null;
}
/**
 * getLabel method comment.
 */
public String getLabel(Object o) {
	//workspaces don't have a name
	return WorkbenchMessages.getString("Workspace"); //$NON-NLS-1$
}
}
