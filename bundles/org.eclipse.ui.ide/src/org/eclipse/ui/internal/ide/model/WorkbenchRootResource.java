/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public Object[] getChildren(Object o) {
        IWorkspaceRoot root = (IWorkspaceRoot) o;
        return root.getProjects();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
    	return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJ_ELEMENT);
    }

    /**
     * Returns the name of this element.  This will typically
     * be used to assign a label to this object when displayed
     * in the UI.
     */
    public String getLabel(Object o) {
        //root resource has no name
        return IDEWorkbenchMessages.Workspace;
    }
}
