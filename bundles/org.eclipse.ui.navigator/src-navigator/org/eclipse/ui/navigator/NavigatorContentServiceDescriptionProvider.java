/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ui.navigator;

import java.util.List;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.internal.NavigatorMessages;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorRegistry;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;

public class NavigatorContentServiceDescriptionProvider implements
		IDescriptionProvider {
	

	private static final NavigatorContentDescriptorRegistry CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorRegistry.getInstance();
 
	private final NavigatorContentService contentService;

	public NavigatorContentServiceDescriptionProvider(NavigatorContentService aContentService) { 
		Assert.isNotNull(aContentService); 
		contentService = aContentService;		
	}	
	
	public String getDescription(Object anElement) { 
		
		Object target;
		
		if(anElement instanceof IStructuredSelection) {

			IStructuredSelection structuredSelection = (IStructuredSelection) anElement;
			if (structuredSelection.size() > 1) {
				return getDefaultStatusBarMessage(structuredSelection.size()); 
			} else {
				target = structuredSelection.getFirstElement();
			}
		} else {
			target = anElement;
		}
			

		List contentDescriptors = CONTENT_DESCRIPTOR_REGISTRY.getEnabledContentDescriptors(target);
		if (contentDescriptors.size() == 0)
			return getDefaultStatusBarMessage(0);
		else {
			/* Use the first Navigator Content Descriptor for now */
			NavigatorContentDescriptor contentDescriptor = (NavigatorContentDescriptor) contentDescriptors.get(0);
			NavigatorContentExtension contentDescriptorInstance = contentService.getExtension(contentDescriptor);

			ICommonLabelProvider labelProvider = contentDescriptorInstance.getLabelProvider();

			String message = labelProvider.getDescription(target);
			message = (message != null) ? message : getDefaultStatusBarMessage(1);
			return message;
		} 
	}
	
	/**
	 * @param aStructuredSelection The current selection from the {@link CommonViewer}
	 * @return A string of the form "# items selected"
	 */
	protected final String getDefaultStatusBarMessage(int aSize) {
		return NavigatorMessages.format("Navigator.statusLineMultiSelect", //$NON-NLS-1$
					new Object[]{new Integer(aSize)});

	}

}
