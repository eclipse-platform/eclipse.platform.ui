/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.tags;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A workbench adapter that can be used to view the resources that make up 
 * a tag source. It is used by the TagConfigurationDialog.
 */
public class TagSourceResourceAdapter implements IAdaptable, IWorkbenchAdapter {

    public static Object getViewerInput(TagSource tagSource) {
        return new TagSourceResourceAdapter(tagSource);
    }
    
    TagSource tagSource;

    private TagSourceResourceAdapter(TagSource tagSource) {
        this.tagSource = tagSource;
    }
  
    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        ICVSResource[] children = tagSource.getCVSResources();
        if (children.length == 0) return new Object[0];
        List result = new ArrayList();
        for (int i = 0; i < children.length; i++) {
            ICVSResource resource = children[i];
            if (resource.isFolder()) {
                result.add(new CVSFolderElement((ICVSFolder)resource, false));
            } else {
                result.add(new CVSFileElement((ICVSFile)resource));
            }
        }
        return result.toArray(new Object[result.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        // No image descriptor
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return tagSource.getShortDescription();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        // No parent
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class) {
            return this;
        }
        return null;
    }

}
