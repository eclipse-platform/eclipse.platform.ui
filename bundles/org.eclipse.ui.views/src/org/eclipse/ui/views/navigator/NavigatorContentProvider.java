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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.INavigatorTreeContentProvider;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
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
public INavigatorTreeContentProvider getContentProvider(Object element) {
	NavigatorContentDescriptor descriptor = getContentProviderDescriptor(element);
	if (descriptor != null) return descriptor.createContentProvider();
	else return null;	
}
protected NavigatorContentDescriptor getContentProviderDescriptor(Object element) {
	NavigatorRootContentDescriptor root = registry.getRootContentDescriptor(partId);
	ArrayList descriptors = registry.getChildContentDescriptors(root);
	for (int i = 0; i < descriptors.size(); i++) {
		NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor)descriptors.get(i);
		INavigatorTreeContentProvider contentProvider = descriptor.createContentProvider();
		Object contentProviderElement = getContentProviderElement(contentProvider, element, getElements(navigator.getViewer().getInput()));
		if (contentProviderElement != null) {
			return registry.getContentProviderDescriptor(root, contentProviderElement);
		}
	}
	return null;
}
public Object getContentProviderElement(INavigatorTreeContentProvider provider, Object element, Object[] elements) {
	if (element == null) return element;
	for (int i=0; i<elements.length; i++) {
		if (elements[i].equals(element)) return element;
	}
	return getContentProviderElement(provider, provider.getParent(element), elements);
}
public Object[] getChildren(Object element) {
	Object[] elements = null;
	NavigatorContentDescriptor contentDescriptor = getContentProviderDescriptor(element);
	if (contentDescriptor != null) {
		elements = contentDescriptor.createContentProvider().getChildren(element);
	}
	return elements;	
}
public Object[] getElements(Object element) {
	NavigatorRootContentDescriptor rootDescriptor = registry.getRootContentDescriptor(partId);
	if (rootDescriptor != null) {
		INavigatorTreeContentProvider contentProvider = rootDescriptor.createContentProvider();
		return contentProvider.getElements(element);	
	}	
	return null;
}
public Object getParent(Object element) {
	IWorkbenchAdapter adapter = getAdapter(element);
	if (adapter != null) {
	    return adapter.getParent(element);
	}
	return null;
}
private void setContentName(Object element, String name) {
	IResource resource = null;
	if (element instanceof IResource) {
		resource = (IResource) element;
	}
	else 
	if (element instanceof IAdaptable) {
		resource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
	}	
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
