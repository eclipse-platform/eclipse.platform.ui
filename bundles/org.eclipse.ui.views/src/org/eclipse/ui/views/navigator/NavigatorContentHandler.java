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
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.NavigatorContentDescriptor;
import org.eclipse.ui.internal.registry.NavigatorDelegateDescriptor;
import org.eclipse.ui.internal.registry.NavigatorRegistry;
import org.eclipse.ui.internal.registry.NavigatorRootDescriptor;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 */
public class NavigatorContentHandler extends WorkbenchContentProvider {
	private Navigator navigator;
	private NavigatorRegistry registry = WorkbenchPlugin.getDefault().getNavigatorRegistry();
	private String partId;
	private Hashtable contentProviders = new Hashtable();
	private NavigatorRootDescriptor rootDescriptor;
	private INavigatorContentProvider rootProvider;
		
public NavigatorContentHandler(Navigator navigator) {
	partId = navigator.getSite().getId();
	this.navigator = navigator;
}
public boolean contentProviderDefined(Object element) {
	return registry.getContentDescriptor(getRootDescriptor(), element) != null;
}
public boolean contentProviderExists(String id) {
	return (INavigatorContentProvider)contentProviders.get(id) != null;
}
protected INavigatorContentProvider createContentProvider(String id) {
	ArrayList descriptors = registry.getDelegateDescriptors(getRootDescriptor());
	for (int i = 0; i < descriptors.size(); i++) {
		NavigatorDelegateDescriptor descriptor = (NavigatorDelegateDescriptor)descriptors.get(i);
		if (descriptor.getId().equals(id)) return createContentProvider(descriptor);
	}
	return null;
}
public INavigatorContentProvider createContentProvider(NavigatorContentDescriptor descriptor) {
	INavigatorContentProvider contentProvider = (INavigatorContentProvider)contentProviders.get(descriptor.getId());
	if  (contentProvider != null) return contentProvider;
	try {
		System.out.println("creating content provider " + descriptor.getClassName()); //$NON-NLS-1$
		contentProvider = (INavigatorContentProvider)WorkbenchPlugin.createExtension(descriptor.getConfigurationElement(), NavigatorContentDescriptor.ATT_CLASS);
		contentProvider.init(this,descriptor.getId());
		contentProviders.put(descriptor.getId(),contentProvider);
	} catch (CoreException exception) {
		WorkbenchPlugin.log("Unable to create content provider: " + //$NON-NLS-1$
			descriptor.getClassName(), exception.getStatus());
	}
	return contentProvider;		
}
public INavigatorContentProvider getContentProvider(String id) {
	INavigatorContentProvider contentProvider = (INavigatorContentProvider)contentProviders.get(id);
	if  (contentProvider != null) return contentProvider;
	return createContentProvider(id);
}
protected NavigatorContentDescriptor getContentDescriptor(Object element) {
	if (isRootElement(element)) {
		return registry.getContentDescriptor(getRootDescriptor(), element);
	}
	ArrayList descriptors = registry.getDelegateDescriptors(getRootDescriptor());
	for (int i = 0; i < descriptors.size(); i++) {
		NavigatorDelegateDescriptor descriptor = (NavigatorDelegateDescriptor)descriptors.get(i);
		if (contentProviderExists(descriptor.getId())) {
		// only test the content provider if it has been instantiated
			// need to traverse parent hierarchy for the element in order to match it up
			// with its content provider desriptor
			INavigatorContentProvider contentProvider = getContentProvider(descriptor.getId());
			Object contentProviderElement = getContentProviderElement(contentProvider, element);
			if (contentProviderElement != null) {
				return registry.getContentDescriptor(getRootDescriptor(), contentProviderElement);
			}
		}
	}
	return null;
}
public Object getContentProviderElement(INavigatorContentProvider provider, Object element) {
	if (element == null) return element;
	Object parent = provider.getParent(element);
	if (parent == null) return null;
	if (parent.equals(navigator.getViewer().getInput())) return element;
	return getContentProviderElement(provider, parent);
}
public Object[] getChildren(Object element) {
	NavigatorContentDescriptor descriptor = getContentDescriptor(element);
	if (descriptor != null) {
		INavigatorContentProvider contentProvider = createContentProvider(descriptor);
		return contentProvider.getChildren(element);
	}
	return new Object[0];
}
public Object[] getElements(Object element) {
	return getRootProvider().getElements(element);	
}
public Object getParent(Object element) {
	NavigatorContentDescriptor descriptor = getContentDescriptor(element);
	if (descriptor != null) {
		INavigatorContentProvider contentProvider = createContentProvider(descriptor);
		return contentProvider.getParent(element);
	}
	return new Object[0];
}
public NavigatorRootDescriptor getRootDescriptor() {
	if (rootDescriptor == null) 
		rootDescriptor = registry.getRootDescriptor(partId);
	return rootDescriptor;
}
public INavigatorContentProvider getRootProvider() {
	if (rootProvider == null) 
		rootProvider = createContentProvider(getRootDescriptor());
	return rootProvider;
}
public boolean hasChildren(Object element) {
	// Do not activate a plugin only to see if an element has children.  Assume that
	// if the element has a content descriptor defined, that it has children, but if
	// the content provider for the content descriptor is instantiated, go ahead and
	// look at it (i.e., if the content provider is instantiated, the plugin that defines
	// the provider is already activated).
	if (isRootElement(element)) {
		NavigatorContentDescriptor descriptor = getContentDescriptor(element);
		if (descriptor == null) return false;
		if (contentProviderExists(descriptor.getId())) {
			INavigatorContentProvider contentProvider = getContentProvider(descriptor.getId());
			return contentProvider.getChildren(element).length > 0;
		} else {
			return true;
		}
	} else {
		return getChildren(element).length > 0;
	}
}
protected boolean isRootElement(Object element) {
	return getRootDescriptor().getElementClass().isInstance(element);
}
}
