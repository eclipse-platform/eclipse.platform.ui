package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;

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
	readRegistry(in, IWorkbenchConstants.PLUGIN_ID, IWorkbenchConstants.PL_ACTION_SET_PART_ASSOCIATIONS);
}
}
