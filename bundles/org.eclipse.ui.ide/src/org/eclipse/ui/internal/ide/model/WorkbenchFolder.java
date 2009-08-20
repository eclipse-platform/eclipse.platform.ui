/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group Support
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * An IWorkbenchAdapter that represents IFolders.
 */
public class WorkbenchFolder extends WorkbenchResource {

	static private String IMG_OBJ_GROUP = "icons/full/obj16/group_obj.gif"; //$NON-NLS-1$
    /**
     *	Answer the appropriate base image to use for the passed resource, optionally
     *	considering the passed open status as well iff appropriate for the type of
     *	passed resource
     */
    protected ImageDescriptor getBaseImage(IResource resource) {
		if (resource.isGroup())
			return AbstractUIPlugin.imageDescriptorFromPlugin(
					IDEWorkbenchPlugin.IDE_WORKBENCH, IMG_OBJ_GROUP);

        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJ_FOLDER);
    }

    /**
     * Returns the children of this container.
     */
    public Object[] getChildren(Object o) {
        try {
            return ((IContainer) o).members();
        } catch (CoreException e) {
            return NO_CHILDREN;
        }
    }
}
