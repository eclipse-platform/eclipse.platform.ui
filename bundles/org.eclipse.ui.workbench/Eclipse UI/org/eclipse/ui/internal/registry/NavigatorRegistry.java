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
package org.eclipse.ui.internal.registry;

import java.util.*;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 */
public class NavigatorRegistry {
	// keyed by target id, value = rootContentDescriptor
	private Map rootContentDescriptors;
/**
 * Create a new ViewRegistry.
 */
public NavigatorRegistry() {
	rootContentDescriptors = new HashMap();
}

/**
 */
public void add(NavigatorDescriptor descriptor) {
	NavigatorRootContentDescriptor rootContentDescriptor = descriptor.getRootContentDescriptor();
	NavigatorContentDescriptor contentDescriptor = descriptor.getContentDescriptor();
	String viewTargetId = descriptor.getTargetId();
	
	if (rootContentDescriptor != null)
		rootContentDescriptors.put(viewTargetId, rootContentDescriptor);	

	if (contentDescriptor != null) {
		String contentTargetId = contentDescriptor.getContentTargetId();
		NavigatorAbstractContentDescriptor parentDescriptor = findContentDescriptor(viewTargetId, contentTargetId);
		if (parentDescriptor != null) parentDescriptor.addSubContentDescriptor(contentDescriptor);
	}
}
private NavigatorAbstractContentDescriptor findContentDescriptor(String viewTargetId, String contentProviderId) {
	NavigatorRootContentDescriptor rootDescriptor = (NavigatorRootContentDescriptor) rootContentDescriptors.get(viewTargetId);
	if (rootDescriptor != null) return rootDescriptor.findContentDescriptor(contentProviderId);
	return null;
}
public ITreeContentProvider getRootContentProvider(String partId) {
	NavigatorRootContentDescriptor rootDescriptor = (NavigatorRootContentDescriptor) rootContentDescriptors.get(partId);
	if (rootDescriptor != null)
		return rootDescriptor.createContentProvider();
		
	return null;
}
public ITreeContentProvider[] getSubContentProviders(String viewTargetId, String contentProviderId) {
	NavigatorAbstractContentDescriptor contentDescriptor = findContentDescriptor(viewTargetId, contentProviderId);
	return getSubContentProviders(contentDescriptor);
}
public ITreeContentProvider[] getSubContentProviders(String viewTargetId) {
	NavigatorAbstractContentDescriptor rootContentDescriptor = (NavigatorAbstractContentDescriptor)rootContentDescriptors.get(viewTargetId);
	return getSubContentProviders(rootContentDescriptor);
}
protected ITreeContentProvider[] getSubContentProviders(NavigatorAbstractContentDescriptor contentDescriptor) {
	ArrayList list = contentDescriptor.getSubContentDescriptors();
	ITreeContentProvider[] providers = new ITreeContentProvider[list.size()];
	for (int i=0; i<list.size(); i++) {
		NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor)list.get(i);
		ITreeContentProvider provider = descriptor.createContentProvider();
		providers[i] = provider;
	}
	return providers;
}
public NavigatorContentDescriptor[] getDescriptors(String partId) {
	NavigatorRootContentDescriptor rootDescriptor = (NavigatorRootContentDescriptor) rootContentDescriptors.get(partId);
	ArrayList descriptors = rootDescriptor.getSubContentDescriptors();
	NavigatorContentDescriptor[] descriptorsArray = new NavigatorContentDescriptor[descriptors.size()];
	for (int i=0; i<descriptors.size(); i++) {
		descriptorsArray[i] = (NavigatorContentDescriptor)descriptors.get(i);
	}
	return descriptorsArray;
}

/*
public ITreeContentProvider getContentProvider(String targetId, Object element) {
	List descriptors = find(targetId);
	Iterator iterator = descriptors.iterator();
	IStructuredContentProvider contentProvider = null;
	
	
	if (project != null)  {
		String[] natures = project.getDescription().getNatureIds();
		while (iterator.hasNext() && contentProvider == null)  {
			NavigatorDescriptor descriptor = (NavigatorDescriptor) iterator.next();
			contentProvider = descriptor.getContentProvider(natures);
		}
		
	}
	while (iterator.hasNext() && contentProvider == null)  {
		NavigatorDescriptor descriptor = (NavigatorDescriptor) iterator.next();
		contentProvider = descriptor.getContentProvider(null);
	}
	if (contentProvider != null)
		return contentProvider;
	return new WorkbenchContentProvider();
}*/
}