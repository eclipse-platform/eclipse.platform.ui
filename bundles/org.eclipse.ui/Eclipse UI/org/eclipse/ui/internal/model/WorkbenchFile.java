package org.eclipse.ui.internal.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
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
