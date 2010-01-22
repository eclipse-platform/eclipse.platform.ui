/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.FrameworkUtil;

/**
 * An example showing how to control when an element is decorated. This example
 * decorates only elements that are instances of IResource and whose attribute
 * is 'Read-only'.
 * 
 * @see ILightweightLabelDecorator
 */
public class ContributingPluginDecorator implements ILabelDecorator {

	public static String ID = "org.eclipse.ui.decorators.ContributingPluginDecorator"; //$NON-NLS-1$
	
	/**
	 * 
	 */
	public ContributingPluginDecorator() {
	}

	public Image decorateImage(Image image, Object element) {
		return image;
	}

	public String decorateText(String text, Object element) {
		String bundleId;
		if (element instanceof String) {
			bundleId = (String) element;
		} else if (element instanceof IConfigurationElement) {
			bundleId = ((IConfigurationElement) element).getContributor()
					.getName();
		} else {
			bundleId = FrameworkUtil.getBundle(element.getClass())
					.getSymbolicName();
		}
		return element == null ? text : text + " (" + bundleId + ")"; //$NON-NLS-1$//$NON-NLS-2$
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}