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
	IAdaptable resource;
	IAdaptable[] workingSet;
	String name;
		
public WorkbenchWorkingSet(IAdaptable resource, IAdaptable[] workingSet, String name) {
	this.resource = resource;
	this.workingSet = workingSet;
	this.name = name;
}
public Object getAdapter(Class clazz) {
//	if (clazz == IWorkbenchAdapter.class) {
//		return this;
//	}
	return resource.getAdapter(clazz);
}
/**
 * @see IWorkbenchAdapter#getLabel
 */
public String getLabel(Object o) {
	return o == null ? "" : o.toString();//$NON-NLS-1$
}

private boolean isWorkingSetParent(IAdaptable adaptable) {
	IWorkbenchAdapter workbenchAdapter = (IWorkbenchAdapter) adaptable.getAdapter(IWorkbenchAdapter.class);
	if (workbenchAdapter != null && workbenchAdapter.getChildren(adaptable).length > 0) {
		for (int i = 0; i < workingSet.length; i++) {	
			IWorkbenchAdapter adapter = (IWorkbenchAdapter) workingSet[i].getAdapter(IWorkbenchAdapter.class);
			
			if (adapter != null) {
				Object parent = adapter.getParent(workingSet[i]);
				while (parent != null) {
					if ((parent instanceof IAdaptable) == false) {
						parent = null;
					}
					else
					if (parent.equals(adaptable)) {
						return true;
					}
					else {
						adapter = (IWorkbenchAdapter) ((IAdaptable) parent).getAdapter(IWorkbenchAdapter.class);
						if (adapter != null) {
							parent = adapter.getParent(parent);
						}
						else {
							parent = null;
						}
					}
				}
			}
		}
	}
	return false;
}
private boolean isWorkingSetResource(IAdaptable resource) {
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
	for (int i = 0; i < workingSet.length; i++) {
		System.out.println((IResource) workingSet[i].getAdapter(IResource.class));
	}
	
	if (o instanceof IWorkingSet) {
		// return the projects of all working set items
		return getProjects();
	}
	if (o instanceof WorkbenchWorkingSet) {
		IAdaptable resource = ((WorkbenchWorkingSet) o).getItem();
		IWorkbenchAdapter adapter = (IWorkbenchAdapter) resource.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) {
			Object[] children = adapter.getChildren(resource);
			
			if (isWorkingSetResource(resource)) {
				// return the regular (unwrapped) children if the object 
				// is a working set item
				return children;
			}
			int childCount = 0;		
			for (int i = 0; i < children.length; i++) {
				if ((children[i] instanceof IAdaptable) == false) {
					children[i] = null;
				}
				else {
					IAdaptable adaptableChild = (IAdaptable) children[i];
					if (isWorkingSetResource(adaptableChild) || isWorkingSetParent(adaptableChild)) {
						childCount++;
					}
					else {
						children[i] = null;
					}
				}
			}
			WorkbenchWorkingSet[] workingSetChildren = new WorkbenchWorkingSet[childCount];
			childCount = 0;
			for (int i = 0; i < children.length; i++) {
				if (children[i] != null) {
					workingSetChildren[childCount++] = new WorkbenchWorkingSet((IAdaptable) children[i], workingSet, name);
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

	if (workingSet.length > 0) {	
		IWorkbenchAdapter adapter = (IWorkbenchAdapter) workingSet[0].getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) {
			Object parent = adapter.getParent(workingSet[0]);
			while (parent != null && parent instanceof IAdaptable) {
				adapter = (IWorkbenchAdapter) ((IAdaptable) parent).getAdapter(IWorkbenchAdapter.class);
				
				if (adapter == null || adapter.getParent(parent) == null) {
					break;
				}
				parent = adapter.getParent(parent);
			}
			if (parent != null && parent instanceof IAdaptable) {
				adapter = (IWorkbenchAdapter) ((IAdaptable) parent).getAdapter(IWorkbenchAdapter.class);
				if (adapter != null) {
					Object[] children = adapter.getChildren(parent);
					for (int i = 0; i < children.length; i++) {
						if (children[i] instanceof IAdaptable) {
							IAdaptable adaptableChild = (IAdaptable) children[i];
							if (isWorkingSetResource(adaptableChild) ||
								isWorkingSetParent(adaptableChild)) {
								projects.add(adaptableChild);
							}
						}
					}
				}
			}
		}
	}
	Iterator iterator = projects.iterator();
	WorkbenchWorkingSet[] workbenchAdapters = new WorkbenchWorkingSet[projects.size()];
	int count = 0;
	while (iterator.hasNext()) {
		IAdaptable project = (IAdaptable) iterator.next();
		workbenchAdapters[count++] = new WorkbenchWorkingSet(project, workingSet, name);
	}
	return workbenchAdapters;
}
public IAdaptable getItem() {
	return resource;
}
}
