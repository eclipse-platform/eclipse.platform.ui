package org.eclipse.ui.internal.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.*;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An IWorkbenchAdapter that represents IProject.
 */
public class WorkbenchProject extends WorkbenchResource
	implements IProjectActionFilter
{
/**
 *	Answer the appropriate base image to use for the passed resource, optionally
 *	considering the passed open status as well iff appropriate for the type of
 *	passed resource
 */
protected ImageDescriptor getBaseImage(IResource resource) {
	IProject project = (IProject) resource;
	String key = project.isOpen() ? ISharedImages.IMG_OBJ_PROJECT : ISharedImages.IMG_OBJ_PROJECT_CLOSED;
	return WorkbenchImages.getImageDescriptor(key);
}
/**
 * Returns the children of this container.
 */
public Object[] getChildren(Object o) {
	IProject project = (IProject) o;
	if (project.isOpen()) {
		try {
			return project.members();
		} catch (CoreException e) {
			//don't get the children if there are problems with the project
		}
	}
	return NO_CHILDREN;
}
/**
 * Returns whether the specific attribute matches the state of the target
 * object.
 *
 * @param target the target object
 * @param name the attribute name
 * @param value the attriute value
 * @return <code>true</code> if the attribute matches; <code>false</code> otherwise
 */
public boolean testAttribute(Object target, String name, String value) {
	IProject proj = (IProject) target;
	if (name.equals(NATURE)) {
		try {
			return proj.hasNature(value);
		} catch (CoreException e) {
			return false;		
		}
	} else if (name.equals(OPEN)) {
		value = value.toLowerCase();
		return (proj.isOpen() == value.equals("true"));
	}
	return super.testAttribute(target, name, value);
}
}
