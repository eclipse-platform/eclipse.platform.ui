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

import org.eclipse.core.resources.*;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.ISharedImages;
import org.eclipse.jface.resource.ImageDescriptor;
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
	ImageDescriptor image = WorkbenchPlugin.getDefault().getEditorRegistry().getImageDescriptor((IFile) resource);
	if (image == null)
		image = WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
	return image;
}
}
