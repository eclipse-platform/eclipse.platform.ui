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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 */
public class NavigatorRegistry {
	private Map navigators;
	private Map rootContentDescriptors;

	/**
 * Create a new ViewRegistry.
 */
public NavigatorRegistry() {
	navigators = new HashMap();
	rootContentDescriptors = new HashMap();
}

/**
 */
public void add(NavigatorDescriptor descriptor) {
	String targetId = descriptor.getTargetId();
	Set descriptors = (Set) navigators.get(targetId);
	NavigatorRootContentDescriptor rootContentDescriptor = descriptor.getRootContentDescriptor();
	NavigatorContentDescriptor contentDescriptor = descriptor.getContentDescriptor();
	
	if (contentDescriptor != null) {
		if (descriptors == null) {
			descriptors = new HashSet();
			navigators.put(targetId, descriptors);
		}
		descriptors.add(contentDescriptor);
	}
	if (rootContentDescriptor != null)
		rootContentDescriptors.put(targetId, rootContentDescriptor);	
}
/**
 * Find a descriptor in the registry.
 */
private Collection find(String targetId) {
	return (Collection) navigators.get(targetId);	
}
public ITreeContentProvider getRootContentProvider(String partId) {
	NavigatorRootContentDescriptor rootDescriptor = (NavigatorRootContentDescriptor) rootContentDescriptors.get(partId);
	
	if (rootDescriptor != null)
		return rootDescriptor.createContentProvider();
		
	return null;
}
public NavigatorContentDescriptor[] getDescriptors(String partId) {
	Collection descriptors = find(partId);	//TODO: handle null case
	
	return (NavigatorContentDescriptor[]) descriptors.toArray(new NavigatorContentDescriptor[descriptors.size()]); //TODO: handle null descriptor (no extension for targeted view)
}
}