/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * A LinkedResourceDecorator decorates an element's image with a linked 
 * resource overlay. 
 * 
 * @since 2.1
 */
public class LinkedResourceDecorator implements ILightweightLabelDecorator {
	private static final String ICON_NAME = "link_ovr.gif";
	private static final ImageDescriptor LINKED;

	static {
		String fileName = WorkbenchImages.ICONS_PATH + "ovr16/" + ICON_NAME;
		
		LINKED = WorkbenchImages.getImageDescriptorFromPlugin(WorkbenchPlugin.getDefault().getDescriptor(), fileName); 
	}
		
	/**
	 * Creates a new <code>LinkedResourceDecorator</code>.
	 */
	public LinkedResourceDecorator() {
	}
	/**
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}
	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		// no resources to dispose
	}
	/**
	 * Returns the linked resource overlay if the given element is a 
	 * linked resource.
	 * 
	 * @param element element to decorate
	 * @return the linked resource overlay or null if element is not a 
	 * 	linked resource.
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#getOverlay(java.lang.Object)
	 */
	public ImageDescriptor getOverlay(Object element) {
		if (element instanceof IResource == false)
			return null;
			
		IResource resource = (IResource) element;
		if (resource.isLinked()) {
			return LINKED;
		}		
		return null;
	}
	/**
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#getPrefix(java.lang.Object)
	 */
	public String getPrefix(Object element) {
		return "";
	}
	/**
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#getSuffix(java.lang.Object)
	 */
	public String getSuffix(Object element) {
		return "";
	}
	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}
	/**
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}
