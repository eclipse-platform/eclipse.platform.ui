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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 */
public class NavigatorRegistry {
	private Map navigators;
		
/**
 * Create a new ViewRegistry.
 */
public NavigatorRegistry() {
	navigators = new HashMap();
}

/**
 */
public void add(NavigatorDescriptor descriptor) {
	String targetId = descriptor.getTargetId();
	List descriptors = (List) navigators.get(targetId);
	
	if (descriptors == null) {
		descriptors = new ArrayList();
		navigators.put(targetId, descriptors);
	}
	descriptors.add(descriptor);	
}
/**
 * Find a descriptor in the registry.
 */
private List find(String targetId) {
	return (List) navigators.get(targetId);	
}
private ContentDescriptor findBestContent(List contentDescriptors, String[] natureIds)  {
	Iterator iterator = contentDescriptors.iterator();
	ContentDescriptor bestDescriptor = null;
	int bestAvailableNatureCount = 0;
	int bestExtraNatureCount = Integer.MAX_VALUE;
		
	while (iterator.hasNext()) {
		ContentDescriptor descriptor = (ContentDescriptor) iterator.next();
		List targetNatures = descriptor.getNatures();
		int availableNatureCount = 0;

		for (int i = 0; i < natureIds.length; i++) {
			if (targetNatures.contains(natureIds[i]))
				availableNatureCount++;
		}
		if (availableNatureCount > bestAvailableNatureCount) {
			bestDescriptor = descriptor;
			bestAvailableNatureCount = availableNatureCount;
			bestExtraNatureCount = targetNatures.size() - availableNatureCount;
		}
		else 
		if (availableNatureCount > 0 && 
				availableNatureCount == bestAvailableNatureCount &&
				targetNatures.size() - availableNatureCount < bestExtraNatureCount) {
			//content descriptor supports same number of natures as current best descriptor
			//but has fewer extra natures that are not requested. Prefer this one.
			bestDescriptor = descriptor;
			bestAvailableNatureCount = availableNatureCount;
			bestExtraNatureCount = targetNatures.size() - availableNatureCount;
		}
	}
	return bestDescriptor;
}
public ITreeContentProvider[] getContentProviders(String targetId) {
	List descriptors = find(targetId);	//TODO: handle null descriptor (no extension for targeted view)
	Iterator iterator = descriptors.iterator();
	List contentProviders = new ArrayList();
	
	while (iterator.hasNext())  {
		NavigatorDescriptor descriptor = (NavigatorDescriptor) iterator.next();
		Iterator contentIterator = descriptor.getContentDescriptors().iterator();
			
		while (contentIterator.hasNext()) {
			ContentDescriptor contentDescriptor = (ContentDescriptor) contentIterator.next();
			contentProviders.add(contentDescriptor.createContentProvider());
		}		
	}
	return (ITreeContentProvider[]) contentProviders.toArray(new ITreeContentProvider[contentProviders.size()]);
}
public ITreeContentProvider getContentProvider(String targetId, String[] natureIds) {
	List descriptors = find(targetId);	//TODO: handle null descriptor
	Iterator iterator = descriptors.iterator();
	ContentDescriptor bestContent = null;
	List naturesList = new ArrayList();
	List contentDescriptors = new ArrayList();
	
	for (int i = 0; i < natureIds.length; i++)
		naturesList.add(natureIds[i]);
	
	while (iterator.hasNext() && bestContent == null)  {
		NavigatorDescriptor descriptor = (NavigatorDescriptor) iterator.next();	
		Iterator contentIterator = descriptor.getContentDescriptors().iterator();
		while (contentIterator.hasNext() && bestContent == null) {
			ContentDescriptor contentDescriptor = (ContentDescriptor) contentIterator.next();
			
			if (naturesList.isEmpty()) {
				if (contentDescriptor.getNatures().isEmpty())
					bestContent = contentDescriptor;
			}
			else
			if (contentDescriptor.getNatures().containsAll(naturesList))
				bestContent = contentDescriptor;
			else 
				contentDescriptors.add(contentDescriptor);
		}
	}
	if (bestContent == null && naturesList.isEmpty() == false) {	
		bestContent = findBestContent(contentDescriptors, natureIds);
	}
	if (bestContent != null)
		return bestContent.createContentProvider();
	return new WorkbenchContentProvider();
}
public NavigatorDescriptor[] getDescriptors(String partId) {
	List descriptors = find(partId);
	
	return (NavigatorDescriptor[]) descriptors.toArray(new NavigatorDescriptor[descriptors.size()]); //TODO: handle null descriptor (no extension for targeted view)
} 
public String[] getNatures(String targetId) {
	List descriptors = find(targetId);//TODO: handle null descriptor (no extension for targeted view)
	Iterator iterator = descriptors.iterator();
	List natures = new ArrayList();
	
	while (iterator.hasNext())  {
		NavigatorDescriptor descriptor = (NavigatorDescriptor) iterator.next();
		Iterator contentIterator = descriptor.getContentDescriptors().iterator();
			
		while (contentIterator.hasNext()) {
			ContentDescriptor contentDescriptor = (ContentDescriptor) contentIterator.next();
			natures.add(contentDescriptor.getNatures());
		}		
	}
	return (String[]) natures.toArray(new String[natures.size()]);
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