package org.eclipse.ui.internal.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.ISharedImages;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An IWorkbenchAdapter that represents IFolders.
 */
public class WorkbenchFolder extends WorkbenchResource {
/**
 *	Answer the appropriate base image to use for the passed resource, optionally
 *	considering the passed open status as well iff appropriate for the type of
 *	passed resource
 */
protected ImageDescriptor getBaseImage(IResource resource) {
	return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
}
/**
 * Returns the children of this container.
 */
public Object[] getChildren(Object o) {
	try {
		return ((IContainer)o).members();
	} catch (CoreException e) {
		return NO_CHILDREN;
	}
}
}
