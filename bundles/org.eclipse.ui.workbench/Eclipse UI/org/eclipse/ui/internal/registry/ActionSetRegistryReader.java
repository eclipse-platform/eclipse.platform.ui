package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.*;

/**
 * A strategy to read view extensions from the registry.
 */
public class ActionSetRegistryReader extends RegistryReader {
	private static final String TAG_SET="actionSet";//$NON-NLS-1$
	private ActionSetRegistry registry;
	
/**
 * RegistryViewReader constructor comment.
 */
public ActionSetRegistryReader() {
	super();
}
/**
 * readElement method comment.
 */
protected boolean readElement(IConfigurationElement element) {
	if (element.getName().equals(TAG_SET)) {
		try {
			ActionSetDescriptor desc = new ActionSetDescriptor(element);
			registry.addActionSet(desc);
		} catch (CoreException e) {
			// log an error since its not safe to open a dialog here
			WorkbenchPlugin.log("Unable to create action set descriptor.",e.getStatus());//$NON-NLS-1$
		}
		return true;
	} else {
		return false;
	}
}
/**
 * Read the view extensions within a registry.
 */
public void readRegistry(IPluginRegistry in, ActionSetRegistry out)
{
	registry = out;
	readRegistry(in, IWorkbenchConstants.PLUGIN_ID, IWorkbenchConstants.PL_ACTION_SETS);
	out.mapActionSetsToCategories();
}
}
