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
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A strategy to read action set part association extension from the registry.
 */
public class ActionSetPartAssociationsReader extends RegistryReader {
	private ActionSetRegistry registry;
	private static final String TAG_EXTENSION="actionSetPartAssociation";//$NON-NLS-1$
	private static final String TAG_PART="part";//$NON-NLS-1$
	private static final String ATT_ID="id";//$NON-NLS-1$
	private static final String ATT_TARGET_ID="targetID";//$NON-NLS-1$
	
/**
 * Creates a new reader.
 */
public ActionSetPartAssociationsReader() {
	super();
}

/**
 * Process an extension.
 */
private boolean processExtension(IConfigurationElement element) {
	String actionSetId = element.getAttribute(ATT_TARGET_ID);
	IConfigurationElement [] children = element.getChildren();
	for (int i = 0; i < children.length; i++) {
		IConfigurationElement child = children[i];
		String type = child.getName();
		if (type.equals(TAG_PART)) {
			String partId = child.getAttribute(ATT_ID);
			if (partId != null) 
				registry.addAssociation(actionSetId, partId);
		} else {
			WorkbenchPlugin.log("Unable to process element: " +//$NON-NLS-1$
				type +
				" in action set part associations extension: " +//$NON-NLS-1$
				element.getDeclaringExtension().getUniqueIdentifier());
		}
	}
	return true;
}

/**
 * Reads the given element.
 */
protected boolean readElement(IConfigurationElement element) {
	String type = element.getName();
	if (type.equals(TAG_EXTENSION)) {
		return processExtension(element);
	}
	return false;
}

/**
 * Read the association extensions within a registry.
 */
public void readRegistry(IPluginRegistry in, ActionSetRegistry out)
{
	registry = out;
	readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_ACTION_SET_PART_ASSOCIATIONS);
}
}
