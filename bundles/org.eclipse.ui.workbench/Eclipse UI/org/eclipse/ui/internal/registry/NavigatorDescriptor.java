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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IInputProvider;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * 
 * @since 3.0
 */
public class NavigatorDescriptor {
	private static final String TAG_CONTENT = "content"; //$NON-NLS-1$
	private static final String ATT_TARGET_ID = "targetId"; //$NON-NLS-1$
	private static final String ATT_INPUT = "input"; //$NON-NLS-1$	

	private String targetId;
	private String input;
	private IConfigurationElement configElement;
	private List contentDescriptors = new ArrayList();

/**
 * Creates a descriptor from a configuration element.
 * 
 * @param configElement configuration element to create a descriptor from
 */
public NavigatorDescriptor(IConfigurationElement configElement) throws WorkbenchException {
	super();
	this.configElement = configElement;
	readConfigElement();
}
public ITreeContentProvider getContentProvider() {
	Iterator contentIterator = contentDescriptors.iterator();
				
	while (contentIterator.hasNext()) {
		ContentDescriptor contentDescriptor = (ContentDescriptor) contentIterator.next();
		List targetNatures = contentDescriptor.getNatures();
		if (targetNatures.isEmpty())
			return contentDescriptor.createContentProvider();
	}		
	return null;
}
public ITreeContentProvider[] getContentProviders() {
	List contentProviders = new ArrayList();

	Iterator contentIterator = contentDescriptors.iterator();
	
	while (contentIterator.hasNext()) {
		ContentDescriptor contentDescriptor = (ContentDescriptor) contentIterator.next();
		contentProviders.add(contentDescriptor.createContentProvider());
	}		
	return (ITreeContentProvider[]) contentProviders.toArray(new ITreeContentProvider[contentProviders.size()]);
}
/**
 */
public IInputProvider getInputProvider() {
	Object inputProvider = null;

	if (input == null)
		return null;
	try {
		inputProvider = WorkbenchPlugin.createExtension(configElement, ATT_INPUT);
	} catch (CoreException exception) {
		WorkbenchPlugin.log("Unable to create input provider: " + //$NON-NLS-1$
			input, exception.getStatus());
	}
	return (IInputProvider) inputProvider;		
}
/**
 */
public String getTargetId() {
	return targetId;
}
/**
 */
public List getContentDescriptors() {
	return contentDescriptors;
}
	/*
	 * Performance: Should create a lookup table indexed by nature.
	 */
	/*public IStructuredContentProvider getContentProvider(IProjectNature[] natures) {
		Iterator iterator = contentDescriptors.iterator();
		ContentDescriptor bestDescriptor = null;
		int bestAvailableNatureCount = 0;
		int bestExtraNatureCount = Integer.MAX_VALUE;
		
		while (iterator.hasNext()) {
			ContentDescriptor descriptor = (ContentDescriptor) iterator.next();
			List targetNatures = descriptor.getNatures();
			if (natures == null) {
				if (targetNatures.isEmpty()) {
					bestDescriptor = descriptor;
					break;
				}
			} 
			else {
				int availableNatureCount = 0;
				for (int i = 0; i < natures.length; i++) {
					if (targetNatures.contains(natures[i]))
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
		}
		if (bestDescriptor != null)
			return bestDescriptor.createContentProvider();
		return null;
	}
	*/
private void readConfigElement() throws WorkbenchException {
	targetId = configElement.getAttribute(ATT_TARGET_ID);
	input = configElement.getAttribute(ATT_INPUT);
			
	IConfigurationElement [] children = configElement.getChildren();
	for (int i = 0; i < children.length; i++) {
		IConfigurationElement child = children[i];
		String type = child.getName();
		if (type.equals(TAG_CONTENT)) {
			ContentDescriptor content = new ContentDescriptor(child);
			contentDescriptors.add(content);
		}
		else  {
			throw new WorkbenchException(
				"Unable to process element: " +//$NON-NLS-1$
				type +
				" in navigator extension: " +//$NON-NLS-1$
				configElement.getDeclaringExtension().getUniqueIdentifier());				
		}
	}
}
}
