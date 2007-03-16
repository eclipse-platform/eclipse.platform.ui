/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.adaptable;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class AdaptableResourceWrapper implements IAdaptable {

    IResource resource;

    /*
     * @see IAdaptable#getAdapter(Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IResource.class)
            return resource;
        if (adapter == IWorkbenchAdapter.class)
            return TestAdaptableWorkbenchAdapter.getInstance();
        return null;
    }

    public AdaptableResourceWrapper(IResource wrapped) {
        resource = wrapped;
    }

    public String getLabel() {
        return resource.getName() + " Adapted";
    }

    public AdaptableResourceWrapper getParent() {
        if (resource.getParent() != null)
            return new AdaptableResourceWrapper(resource.getParent());
        return null;
    }

    public AdaptableResourceWrapper[] getChildren() {
        AdaptableResourceWrapper[] wrappers = new AdaptableResourceWrapper[0];

        if (resource instanceof IContainer) {
            IResource[] children;
            try {
                children = ((IContainer) resource).members();
            } catch (CoreException exception) {
                return wrappers;
            }
            wrappers = new AdaptableResourceWrapper[children.length];
            for (int i = 0; i < children.length; i++) {
                wrappers[i] = new AdaptableResourceWrapper(children[i]);
            }
        }
        return wrappers;
    }
}
