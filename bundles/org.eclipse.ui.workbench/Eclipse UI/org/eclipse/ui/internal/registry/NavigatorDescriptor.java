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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.WorkbenchException;

/**
 * 
 * @since 3.0
 */
public class NavigatorDescriptor {
	private static final String CHILD_CONTENT = "content"; //$NON-NLS-1$
	private static final String CHILD_ROOT_CONTENT = "rootContent"; //$NON-NLS-1$
	private static final String ATT_TARGET_ID = "targetId"; //$NON-NLS-1$

	private String targetId;
	private IConfigurationElement configElement;
	private NavigatorContentDescriptor contentDescriptor;
	private NavigatorRootContentDescriptor rootContentDescriptor;

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
/**
 */
public NavigatorContentDescriptor getContentDescriptor() {
	return contentDescriptor;
}
public NavigatorRootContentDescriptor getRootContentDescriptor() {
	return rootContentDescriptor;
}
public ITreeContentProvider getContentProvider() {
	return contentDescriptor.createContentProvider();
}
/**
 */
public String getTargetId() {
	return targetId;
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
			
	IConfigurationElement[] children = configElement.getChildren(CHILD_CONTENT);
	if (children.length > 0)
		contentDescriptor = new NavigatorContentDescriptor(children[0]);

	children = configElement.getChildren(CHILD_ROOT_CONTENT);
	if (children.length > 0)
		rootContentDescriptor = new NavigatorRootContentDescriptor(children[0]);
}
}
