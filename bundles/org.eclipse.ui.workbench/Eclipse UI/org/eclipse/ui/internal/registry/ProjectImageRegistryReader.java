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

import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.*;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A strategy to project nature image extensions from the registry.
 * 
 * @deprecated Extension point no longer applicable with new project capability
 */
public class ProjectImageRegistryReader extends RegistryReader {
	private static final String TAG_IMAGE="image";//$NON-NLS-1$
	private static final String ATT_ID="id";//$NON-NLS-1$
	private static final String ATT_NATURE_ID="natureId";//$NON-NLS-1$
	private static final String ATT_ICON="icon";//$NON-NLS-1$
	private ProjectImageRegistry registry;
	
/**
 * Reads the contents of the given element
 */
protected boolean readElement(IConfigurationElement element) {
	if (!element.getName().equals(TAG_IMAGE)) 
		return false;

	String id = element.getAttribute(ATT_ID);
	if (id == null) {
		logMissingAttribute(element, ATT_ID);
		return true;
	}

	String natureId = element.getAttribute(ATT_NATURE_ID);
	if (natureId == null) {
		logMissingAttribute(element, ATT_NATURE_ID);
		return true;
	}

	String icon = element.getAttribute(ATT_ICON);
	if (icon == null) {
		logMissingAttribute(element, ATT_ICON);
		return true;
	}
	ImageDescriptor image = WorkbenchImages.getImageDescriptorFromExtension(
			element.getDeclaringExtension(), icon);

	if (image != null)	
		registry.setNatureImage(natureId, image);
		
	return true;
}
/**
 * Read the project nature images within a registry.
 */
public void readProjectNatureImages(IPluginRegistry in, ProjectImageRegistry out)
{
	registry = out;
	readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_PROJECT_NATURE_IMAGES);
}
}
