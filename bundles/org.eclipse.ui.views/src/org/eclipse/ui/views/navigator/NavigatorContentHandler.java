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
import org.eclipse.ui.internal.registry.NavigatorAbstractContentDescriptor;
import org.eclipse.ui.internal.registry.NavigatorContentDescriptor;
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
	this.rootDescriptor = registry.getRootDescriptor(partId);
	this.rootProvider = createContentProvider(rootDescriptor);
}
public boolean contentProviderDefined(Object element) {
	return registry.getContentDescriptor(rootDescriptor, element) != null;
}
public INavigatorContentProvider createContentProvider(NavigatorAbstractContentDescriptor descriptor) {
	INavigatorContentProvider contentProvider = (INavigatorContentProvider)contentProviders.get(descriptor.getId());
	if  (contentProvider != null) return contentProvider;
	try {
		System.out.println("creating content provider " + descriptor.getClassName()); //$NON-NLS-1$
		contentProvider = (INavigatorContentProvider)WorkbenchPlugin.createExtension(descriptor.getConfigurationElement(), NavigatorAbstractContentDescriptor.ATT_CLASS);
		contentProvider.init(this,descriptor.getId());
		contentProviders.put(descriptor.getId(),contentProvider);
	} catch (CoreException exception) {
		WorkbenchPlugin.log("Unable to create content provider: " + //$NON-NLS-1$
			descriptor.getClassName(), exception.getStatus());
	}
	return contentProvider;		
}
public INavigatorContentProvider getContentProvider(String id) {
	return (INavigatorContentProvider)contentProviders.get(id);
}
protected NavigatorContentDescriptor getContentDescriptor(Object element) {
	return registry.getContentDescriptor(rootDescriptor, element);
}
protected NavigatorContentDescriptor getParentContentDescriptor(Object element) {
	ArrayList descriptors = registry.getDelegateDescriptors(rootDescriptor);
	for (int i = 0; i < descriptors.size(); i++) {
		NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor)descriptors.get(i);
		INavigatorContentProvider contentProvider = createContentProvider(descriptor);
		Object contentProviderElement = getContentProviderElement(contentProvider, element);
		if (contentProviderElement != null) {
			return registry.getContentDescriptor(rootDescriptor, contentProviderElement);
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
	return rootProvider.getChildren(element);
}
public Object[] getElements(Object element) {
	return rootProvider.getElements(element);	
}
public Object getParent(Object element) {
	return rootProvider.getParent(element);
}
protected NavigatorRootDescriptor getRootDescriptor() {
	return rootDescriptor;
}
protected INavigatorContentProvider getRootProvider() {
	return rootProvider;
}
public boolean hasChildren(Object element) {
	return rootProvider.hasChildren(element);
}
}
