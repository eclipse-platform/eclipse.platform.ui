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

import java.util.StringTokenizer;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

/**
 * Capture the attributes of a view extension.
 */
public class ViewDescriptor implements IViewDescriptor {
	private String id;
	private ImageDescriptor imageDescriptor;
	private static final String ATT_ID="id";//$NON-NLS-1$
	private static final String ATT_NAME="name";//$NON-NLS-1$
	private static final String ATT_ACCELERATOR="accelerator";//$NON-NLS-1$
	private static final String ATT_ICON="icon";//$NON-NLS-1$
	private static final String ATT_CATEGORY="category";//$NON-NLS-1$
	private static final String ATT_CLASS="class";//$NON-NLS-1$
	private static final String ATT_RATIO="fastViewWidthRatio";//$NON-NLS-1$
	private String label;
	private String accelerator;
	private String className;
	private IConfigurationElement configElement;
	private String [] categoryPath;
	private float fastViewWidthRatio;
/**
 * Create a new ViewDescriptor for an extension.
 */
public ViewDescriptor(IConfigurationElement e) throws CoreException {
	configElement = e;
	loadFromExtension();
}
/**
 * Return an instance of the declared view.
 */
public IViewPart createView() throws CoreException
{
	Object obj = WorkbenchPlugin.createExtension(configElement, ATT_CLASS);
	return (IViewPart) obj;
}
/**
 * Returns tokens for the category path or null if not defined.
 */
public String[] getCategoryPath() {
	return categoryPath;
}
public IConfigurationElement getConfigurationElement() {
	return configElement;
}
public String getID() {
	return id;
}
public String getId() {
	return id;
}
public ImageDescriptor getImageDescriptor() {
	if (imageDescriptor != null)
		return imageDescriptor;
	String iconName = configElement.getAttribute(ATT_ICON);
	if (iconName == null)
		return null;
	imageDescriptor = 
		WorkbenchImages.getImageDescriptorFromExtension(
			configElement.getDeclaringExtension(), 
			iconName); 
	return imageDescriptor;
}

public String getLabel() {
	return label;
}

public String getAccelerator() {
	return accelerator;
}

public float getFastViewWidthRatio() {
	return fastViewWidthRatio;	
}

/**
 * load a view descriptor from the registry.
 */
private void loadFromExtension() throws CoreException {
	id = configElement.getAttribute(ATT_ID);
	label = configElement.getAttribute(ATT_NAME);
	accelerator = configElement.getAttribute(ATT_ACCELERATOR);
	className = configElement.getAttribute(ATT_CLASS);
	String category = configElement.getAttribute(ATT_CATEGORY);
	String ratio = configElement.getAttribute(ATT_RATIO);

	// Sanity check.
	if ((label == null) || (className == null)) {
		throw new CoreException(
			new Status(
				IStatus.ERROR, 
				configElement.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), 
				0, 
				"Invalid extension (missing label or class name): " + id, //$NON-NLS-1$
				null)); 
	}
	if (category != null) {
		StringTokenizer stok = new StringTokenizer(category, "/");//$NON-NLS-1$
		categoryPath = new String[stok.countTokens()];
		// Parse the path tokens and store them
		for (int i = 0; stok.hasMoreTokens(); i++) {
			categoryPath[i] = stok.nextToken();
		}
	}
	
	if(ratio != null) {
		try {
			fastViewWidthRatio = new Float(ratio).floatValue();
			if(fastViewWidthRatio > IPageLayout.RATIO_MAX)
				fastViewWidthRatio = IPageLayout.RATIO_MAX;
			if(fastViewWidthRatio < IPageLayout.RATIO_MIN)
				fastViewWidthRatio = IPageLayout.RATIO_MIN;
		} catch(NumberFormatException e) {
			fastViewWidthRatio = IPageLayout.DEFAULT_FASTVIEW_RATIO;
		}
	} else {
		fastViewWidthRatio = IPageLayout.DEFAULT_FASTVIEW_RATIO;
	}
}
/**
 * Returns a string representation of this descriptor.  For
 * debugging purposes only.
 */
public String toString() {
	return "View(" + getID() + ")";//$NON-NLS-2$//$NON-NLS-1$
}
}
