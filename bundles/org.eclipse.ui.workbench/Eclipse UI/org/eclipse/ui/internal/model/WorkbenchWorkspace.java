package org.eclipse.ui.internal.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import java.util.*;

/**
 * IWorkbenchAdapter adapter for the IWorkspace object.
 */
public class WorkbenchWorkspace extends WorkbenchAdapter {
/**
 * @see IWorkbenchAdapter#getChildren
 * Returns the children of the workspace.
 */
public Object[] getChildren(Object o) {
	IWorkspace workspace = (IWorkspace) o;
	return workspace.getRoot().getProjects();
}
/**
 * @see IWorkbenchAdapter#getImageDescriptor
 */
public ImageDescriptor getImageDescriptor(Object object) {
	return null;
}
/**
 * getLabel method comment.
 */
public String getLabel(Object o) {
	//workspaces don't have a name
	return WorkbenchMessages.getString("Workspace"); //$NON-NLS-1$
}
}
