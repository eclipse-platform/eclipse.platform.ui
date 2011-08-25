/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.FrameworkUtil;

/**
 * A decorator for appending the contributing plug-in ID to an existing tooltip.
 * 
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