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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.ActionExpression;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.NavigatorContentDescriptor;
import org.eclipse.ui.internal.registry.NavigatorRegistry;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * Provides tree contents for objects that have the IWorkbenchAdapter
 * adapter registered. 
 */
public class NavigatorContentProvider extends WorkbenchContentProvider {
	private Navigator navigator;
	private NavigatorRegistry registry = WorkbenchPlugin.getDefault().getNavigatorRegistry();
	private String partId;
		
public NavigatorContentProvider(Navigator navigator) {
	partId = navigator.getSite().getId();
	this.navigator = navigator;
}
public Object[] getChildren(Object element) {
	NavigatorContentDescriptor[] descriptors = registry.getDescriptors(partId);
	Object[] elements = new Object[0];
	NavigatorContentDescriptor defaultContentDescriptor = null;
	NavigatorContentDescriptor contentDescriptor = null;
	int priority = 0;
	
	for (int i = 0; i < descriptors.length; i++) {
		NavigatorContentDescriptor descriptor = descriptors[i];
		ActionExpression enablement = descriptor.getEnableExpression(); 
		
		if (enablement == null)
			defaultContentDescriptor = descriptor;
		else
		if (enablement.isEnabledFor(element) && descriptor.getPriority() > priority) {
			priority = descriptor.getPriority();
			contentDescriptor = descriptor;
		}
	}
	if (contentDescriptor == null)
		contentDescriptor = defaultContentDescriptor;
		
	if (contentDescriptor != null) {
		elements = contentDescriptor.createContentProvider().getChildren(element);
		setContentName(element, contentDescriptor.getName());
	}
	return elements;	
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
	ITreeContentProvider contentProvider = registry.getRootContentProvider(partId);
	Object[] elements = contentProvider.getElements(element);
	ITreeContentProvider[] subContentProviders = registry.getSubContentProviders(partId);
	for (int i=0; i<subContentProviders.length; i++) {
		ITreeContentProvider subContentProvider = subContentProviders[i];
		// if delete enabled
		// if replace enabled
		// if add enabled
	}
	return elements;
}
public Object getParent(Object element) {
	IWorkbenchAdapter adapter = getAdapter(element);
	if (adapter != null) {
	    return adapter.getParent(element);
	}
	return null;
}
private void setContentName(Object element, String name) {
	IResource resource = getResource(element);
	if (resource != null) {
		try {
			resource.setSessionProperty(new QualifiedName(null, "contentProvider"), name); //$NON-NLS-1$
		}
		catch (CoreException e) {
			// TODO: handle exception
		}
	}
}
}
