package org.eclipse.ui.internal.model;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @version 	1.0
 * @author
 */
public class WorkbenchWorkingSet extends WorkbenchAdapter implements IAdaptable {
	IResource resource;
	IResource[] workingSet;
		
public WorkbenchWorkingSet(IResource resource, IResource[] workingSet) {
	this.resource = resource;
	this.workingSet = workingSet;
}
public Object getAdapter(Class clazz) {
//	if (clazz == IWorkbenchAdapter.class) {
//		return this;
//	}
	return resource.getAdapter(clazz);
}
private boolean isWorkingSetParent(IResource resource) {
	if (resource instanceof IContainer) {
		for (int i = 0; i < workingSet.length; i++) {	
			IContainer parent = workingSet[i].getParent();
			while (parent != null) {
				if (parent.equals(resource)) {
					return true;
				}
				parent = parent.getParent();
			}
		}
	}
	return false;
}
private boolean isWorkingSetResource(IResource resource) {
	for (int i = 0; i < workingSet.length; i++) {	
		if (workingSet[i].equals(resource)) {
			return true;
		}
	}
	return false;
}
/**
 * @see IWorkbenchAdapter#getChildren
 */
public Object[] getChildren(Object o) {
	if (o instanceof IWorkingSet) {
		// return the projects of all working set items
		return getProjects();
	}
	if (o instanceof WorkbenchWorkingSet) {
		IResource resource = ((WorkbenchWorkingSet) o).getResource();
		if (resource instanceof IContainer) {
			IContainer container = (IContainer) resource;
			IResource[] children = new IResource[0];
			
			try {
				children = container.members();
			} catch (CoreException e) {
				WorkbenchPlugin.log("Problem getting members of: " + container.getName(), e.getStatus()); //$NON-NLS-1$	
			}
			if (isWorkingSetResource(container)) {
				// return the regular (unwrapped) children if the object 
				// is a working set item
				return children;
			}
			int childCount = 0;		
			for (int i = 0; i < children.length; i++) {
				if (isWorkingSetResource(children[i]) || isWorkingSetParent(children[i])) {
					childCount++;
				}
				else {
					children[i] = null;
				}
			}
			WorkbenchWorkingSet[] workingSetChildren = new WorkbenchWorkingSet[childCount];
			childCount = 0;
			for (int i = 0; i < children.length; i++) {
				if (children[i] != null) {
					workingSetChildren[childCount++] = new WorkbenchWorkingSet(children[i], workingSet);
				}
			}
			// return the wrapped children that are parents of working set items
			return workingSetChildren;
		}
	}
	return NO_CHILDREN;
}
private WorkbenchWorkingSet[] getProjects() {
	HashSet projects = new HashSet();

	for (int i = 0; i < workingSet.length; i++) {	
		IProject project = workingSet[i].getProject();
		if (project != null) {
			projects.add(project);
		}
	}
	Iterator iterator = projects.iterator();
	WorkbenchWorkingSet[] workbenchAdapters = new WorkbenchWorkingSet[projects.size()];
	int count = 0;
	while (iterator.hasNext()) {
		IProject project = (IProject) iterator.next();
		workbenchAdapters[count++] = new WorkbenchWorkingSet(project, workingSet);
	}
	return workbenchAdapters;
}
public IResource getResource() {
	return resource;
}
}
