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

import org.eclipse.core.internal.resources.WorkspaceRoot;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.INavigatorTreeContentProvider;
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
protected IWorkbenchAdapter getAdapter(Object o) {
	if (!(o instanceof IAdaptable)) {
		return null;
	}
	return (IWorkbenchAdapter)((IAdaptable)o).getAdapter(IWorkbenchAdapter.class);
}
protected INavigatorTreeContentProvider getContentProvider(Object element) {
	NavigatorContentDescriptor[] descriptors = registry.getDescriptors(partId);
	for (int i = 0; i < descriptors.length; i++) {
		NavigatorContentDescriptor descriptor = descriptors[i];
		ActionExpression enablement = descriptor.getEnableExpression(); 
		if (enablement != null) {
			if (enablement.isEnabledFor(element)) {
				NavigatorContentDescriptor contentDescriptor = descriptor;
				return contentDescriptor.createContentProvider();
			}
		}
	}
	Object parentElement = getParent(element);
	if (parentElement instanceof WorkspaceRoot) {
		return new ProjectContentProvider();
	} else {
		return getContentProvider(parentElement);
	}
}
public Object[] getChildren(Object element) {
	INavigatorTreeContentProvider contentProvider = getContentProvider(element);
	return contentProvider.getChildren(element);
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
	INavigatorTreeContentProvider contentProvider = registry.getRootContentProvider(partId);
	Object[] elements = contentProvider.getElements(element);

	Object[] newElements = new Object[elements.length];
	NavigatorContentDescriptor[] descriptors = registry.getDescriptors(partId);
	for (int i=0; i<elements.length; i++) {
		Object childElement = elements[i];
		setContentName(childElement, registry.getRootContentDescriptor(partId).getName());
		newElements[i] = childElement;
		for (int j = 0; j < descriptors.length; j++) {
			NavigatorContentDescriptor descriptor = descriptors[j];
			ActionExpression enablement = descriptor.getEnableExpression(); 
			if (enablement != null) {
				if (enablement.isEnabledFor(childElement)) {
					NavigatorContentDescriptor contentDescriptor = descriptor;
					Object replacementElement = contentDescriptor.createContentProvider().getReplacementElement(element, childElement);
					if (replacementElement != null) {
						setContentName(replacementElement, contentDescriptor.getName());
						newElements[i]=replacementElement;
					}
				}
			}
		}
	}

	return newElements;
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
