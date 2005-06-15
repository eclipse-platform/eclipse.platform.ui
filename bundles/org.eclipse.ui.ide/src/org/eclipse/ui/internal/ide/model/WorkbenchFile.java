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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * An IWorkbenchAdapter that represents IFiles.
 */
public class WorkbenchFile extends WorkbenchResource {
    /**
     *	Answer the appropriate base image to use for the passed resource, optionally
     *	considering the passed open status as well iff appropriate for the type of
     *	passed resource
     */
    protected ImageDescriptor getBaseImage(IResource resource) {
		IContentType contentType = null;
		// do we need to worry about checking here?
		if (resource instanceof IFile) {
			contentType = IDE.getContentType((IFile)resource);
		}
        // @issue move IDE specific images
        ImageDescriptor image = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(resource.getName(), contentType);
        if (image == null)
            image = PlatformUI.getWorkbench().getSharedImages()
                    .getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
        return image;
    }
}
