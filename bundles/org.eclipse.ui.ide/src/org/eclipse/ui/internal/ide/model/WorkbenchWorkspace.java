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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * IWorkbenchAdapter adapter for the IWorkspace object.
 */
public class WorkbenchWorkspace extends WorkbenchAdapter {
    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        IWorkspace workspace = (IWorkspace) o;
        return workspace.getRoot().getProjects();
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    /**
     * getLabel method comment.
     */
    public String getLabel(Object o) {
        //workspaces don't have a name
        return IDEWorkbenchMessages.Workspace;
    }
}
