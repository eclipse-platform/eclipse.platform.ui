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
package org.eclipse.ui.views.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IInputProvider;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.NavigatorDescriptor;
import org.eclipse.ui.internal.registry.NavigatorRegistry;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * Provides tree contents for objects that have the IWorkbenchAdapter
 * adapter registered. 
 */
public class NavigatorContentProvider implements ITreeContentProvider, IResourceChangeListener {
	private Navigator navigator;
	private NavigatorRegistry registry = WorkbenchPlugin.getDefault().getNavigatorRegistry();
	private String partId;
		
public NavigatorContentProvider(Navigator navigator) {
	partId = navigator.getSite().getId();
	this.navigator = navigator;
}
/**
 * Note: This method is for internal use only. Clients should not call this method.
 */
protected static Object[] concatenate(Object[] a1, Object[] a2) {
	int a1Len= a1.length;
	int a2Len= a2.length;
	Object[] res= new Object[a1Len + a2Len];
	System.arraycopy(a1, 0, res, 0, a1Len);
	System.arraycopy(a2, 0, res, a1Len, a2Len); 
	return res;
}

/* (non-Javadoc)
 * Method declared on IContentProvider.
 */
public void dispose() {
	if (navigator.getViewer() != null) {
		Object obj = navigator.getViewer().getInput();
		if (obj instanceof IWorkspace) {
			IWorkspace workspace = (IWorkspace) obj;
			workspace.removeResourceChangeListener(this);
		} else
			if (obj instanceof IContainer) {
				IWorkspace workspace = ((IContainer) obj).getWorkspace();
				workspace.removeResourceChangeListener(this);
			}
	}
}
/**
 * Returns the implementation of IWorkbenchAdapter for the given
 * object.  Returns null if the adapter is not defined or the
 * object is not adaptable.
 */
protected IWorkbenchAdapter getAdapter(Object o) {
	if (!(o instanceof IAdaptable)) {
		return null;
	}
	return (IWorkbenchAdapter)((IAdaptable)o).getAdapter(IWorkbenchAdapter.class);
}
/* (non-Javadoc)
 * Method declared on ITreeContentProvider.
 */
public Object[] getChildren(Object element) {
	ITreeContentProvider contentProvider = getContentProvider(element);
	
	if (contentProvider != null)
		return contentProvider.getChildren(element);
	return new Object[0];	
}
ITreeContentProvider getContentProvider(IProject project) {
	String[] natures;
	
	try {
		natures = project.getDescription().getNatureIds();
	}
	catch (CoreException exception) {
		//project is closed
		return null;
	}
	return registry.getContentProvider(partId, natures);
}
ITreeContentProvider getContentProvider(Object element) {
	IResource resource = getResource(element);
	IProject project;
	
	if (resource == null)
		return null;

	project = resource.getProject();
	if (project == null)
		return null;

	return getContentProvider(project);
}
private IProject getProject(Object element) {
	IProject project = null;
	if (element instanceof IProject) {
		project = (IProject) element;
	}
	else 
	if (element instanceof IAdaptable) {
		project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
	}	
	return project;
}
private IResource getResource(Object element) {
	IResource resource = null;
	if (element instanceof IResource) {
		resource = (IResource) element;
	}
	else 
	if (element instanceof IAdaptable) {
		resource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
	}	
	return resource;
}
/* (non-Javadoc)
 * Method declared on IStructuredContentProvider.
 */
public Object[] getElements(Object element) {
	NavigatorDescriptor[] descriptors = registry.getDescriptors(partId);
	Object[] elements = new Object[0];
	
	for (int i = 0; i < descriptors.length; i++) {
		NavigatorDescriptor descriptor = descriptors[i];
		ITreeContentProvider contentProvider = descriptor.getContentProvider();
		IInputProvider inputProvider = descriptor.getInputProvider();
		Object input = element;
		Object[] newElements;
		
		if (inputProvider != null)
			input = inputProvider.getInput(element);
		
		newElements = contentProvider.getElements(input);
		setContentName(newElements, descriptor.getContentDescriptor().getName());
		elements = concatenate(elements, newElements);
	}
	Object[] workbenchElements = (new WorkbenchContentProvider()).getElements(element);
	List workbenchProjects = new ArrayList();
		
	for (int i = 0; i < workbenchElements.length; i++) {
		IProject project = getProject(workbenchElements[i]);
		if (project != null)
			workbenchProjects.add(project);		
	}	
	for (int i = 0; i < elements.length; i++) {
		IProject project = getProject(elements[i]);
		if (project != null)
			workbenchProjects.remove(project);
	}
	return concatenate(elements, workbenchProjects.toArray(new Object[workbenchProjects.size()]));
}
/* (non-Javadoc)
 * Method declared on ITreeContentProvider.
 */
public Object getParent(Object element) {
	IWorkbenchAdapter adapter = getAdapter(element);
	if (adapter != null) {
	    return adapter.getParent(element);
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on ITreeContentProvider.
 */
public boolean hasChildren(Object element) {
	return getChildren(element).length > 0;
}
/* (non-Javadoc)
 * Method declared on IContentProvider.
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	if (navigator.getViewer() != viewer)
		return;
	
	IWorkspace oldWorkspace = null;
	IWorkspace newWorkspace = null;
	if (oldInput instanceof IWorkspace) {
		oldWorkspace = (IWorkspace) oldInput;
	}
	else if (oldInput instanceof IContainer) {
		oldWorkspace = ((IContainer) oldInput).getWorkspace();
	}
	if (newInput instanceof IWorkspace) {
		newWorkspace = (IWorkspace) newInput;
	} else if (newInput instanceof IContainer) {
		newWorkspace = ((IContainer) newInput).getWorkspace();
	}
	if (oldWorkspace != newWorkspace) {
		if (oldWorkspace != null) {
			oldWorkspace.removeResourceChangeListener(this);
		}
		if (newWorkspace != null) {
			newWorkspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		}
	}
}
/**
 * Process a resource delta.  
 */
protected void processDelta(IResourceDelta delta) {
	TreeViewer viewer = navigator.getViewer();
	// This method runs inside a syncExec.  The widget may have been destroyed
	// by the time this is run.  Check for this and do nothing if so.
	Control ctrl = viewer.getControl();
	if (ctrl == null || ctrl.isDisposed())
		return;

	// Get the affected resource
	IResource resource = delta.getResource();

	// If any children have changed type, just do a full refresh of this parent,
	// since a simple update on such children won't work, 
	// and trying to map the change to a remove and add is too dicey.
	// The case is: folder A renamed to existing file B, answering yes to overwrite B.
	IResourceDelta[] affectedChildren =
		delta.getAffectedChildren(IResourceDelta.CHANGED);
	for (int i = 0; i < affectedChildren.length; i++) {
		if ((affectedChildren[i].getFlags() & IResourceDelta.TYPE) != 0) {
			viewer.refresh(resource);
			return;
		}
	}

	// Check the flags for changes the Navigator cares about.
	// See ResourceLabelProvider for the aspects it cares about.
	// Notice we don't care about F_CONTENT or F_MARKERS currently.
	int changeFlags = delta.getFlags();
	if ((changeFlags
		& (IResourceDelta.OPEN | IResourceDelta.SYNC))
		!= 0) {
		viewer.update(resource, null);
	}
	// Replacing a resource may affect its label and its children
	if ((changeFlags & IResourceDelta.REPLACED) != 0) {
		viewer.refresh(resource, true);
		return;
	}

	// Handle changed children .
	for (int i = 0; i < affectedChildren.length; i++) {
		processDelta(affectedChildren[i]);
	}

	// Process removals before additions, to avoid multiple equal elements in the viewer.

	// Handle removed children. Issue one update for all removals.
	affectedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED);
	if (affectedChildren.length > 0) {
		Object[] affected = new Object[affectedChildren.length];
		for (int i = 0; i < affectedChildren.length; i++)
			affected[i] = affectedChildren[i].getResource();
		viewer.remove(affected);
	}

	// Handle added children. Issue one update for all insertions.
	affectedChildren = delta.getAffectedChildren(IResourceDelta.ADDED);
	if (affectedChildren.length > 0) {
		Object[] affected = new Object[affectedChildren.length];
		for (int i = 0; i < affectedChildren.length; i++)
			affected[i] = affectedChildren[i].getResource();
		viewer.add(resource, affected);
	}
}
/**
 * The workbench has changed.  Process the delta and issue updates to the viewer,
 * inside the UI thread.
 *
 * @see IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 */
public void resourceChanged(final IResourceChangeEvent event) {
	final IResourceDelta delta = event.getDelta();
	Control ctrl = navigator.getViewer().getControl();
	if (ctrl != null && !ctrl.isDisposed()) {
		// Do a sync exec, not an async exec, since the resource delta
		// must be traversed in this method.  It is destroyed
		// when this method returns.
		ctrl.getDisplay().syncExec(new Runnable() {
			public void run() {
				processDelta(delta);
			}
		});
	}
}
private void setContentName(Object[] elements, String name) {
	for (int i = 0; i < elements.length; i++) {
		IResource resource = getResource(elements[i]);
		try {
			resource.setSessionProperty(new QualifiedName(null, "contentProvider"), name);
		}
		catch (CoreException e) {
			// TODO: handle exception
		}
	}
}
}
