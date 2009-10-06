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
package org.eclipse.debug.internal.core;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

/**
 * The ResourceFactory is used to save and recreate an IResource object.
 * As such, it implements the IPersistableElement interface for storage
 * and the IElementFactory interface for recreation.
 */
public class ResourceFactory {

    // These persistence constants are stored in XML.  Do not
    // change them.
    public static final String TAG_PATH = "path";//$NON-NLS-1$

    public static final String TAG_TYPE = "type";//$NON-NLS-1$

    /**
     * Creates and returns an element based on the given memento
     * 
     * @param memento element memento
     * @return associated element
     */
    public static IAdaptable createElement(XMLMemento memento) {
        // Get the file name.
        String fileName = memento.getString(TAG_PATH);
        if (fileName == null) {
			return null;
		}

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        String type = memento.getString(TAG_TYPE);
        IResource res = null;
        if (type == null) {
            // Old format memento. Create an IResource using findMember. 
            // Will return null for resources in closed projects.
            res = root.findMember(new Path(fileName));
        } else {
            int resourceType = Integer.parseInt(type);

            if (resourceType == IResource.ROOT) {
				res = root;
			} else if (resourceType == IResource.PROJECT) {
				res = root.getProject(fileName);
			} else if (resourceType == IResource.FOLDER) {
				res = root.getFolder(new Path(fileName));
			} else if (resourceType == IResource.FILE) {
				res = root.getFile(new Path(fileName));
			}
        }
        return res;
    }

    public static void saveState(XMLMemento memento, IResource res) {
        memento.putString(TAG_PATH, res.getFullPath().toString());
        memento.putString(TAG_TYPE, Integer.toString(res.getType()));
    }
}
