package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A strategy to read working set extensions from the registry.
 */
public class WorkingSetRegistryReader extends RegistryReader {
	private static final String TAG="workingSet";	//$NON-NLS-1$
	private WorkingSetRegistry registry;
	
/**
 * Overrides method in RegistryReader.
 * 
 * @see RegistryReader#readElement(IConfigurationElement)
 */
protected boolean readElement(IConfigurationElement element) {
	if (element.getName().equals(TAG)) {
		try {
			WorkingSetDescriptor desc = new WorkingSetDescriptor(element);
			registry.addWorkingSetDescriptor(desc);
		} catch (CoreException e) {
			// log an error since its not safe to open a dialog here
			WorkbenchPlugin.log("Unable to create working set descriptor.",e.getStatus());//$NON-NLS-1$
		}
		return true;
	}
	
	return false;
}
/**
 * Reads the working set extensions within a registry.
 * 
 * @param in the plugin registry to read from
 * @param out the working set registry to store read entries in.
 */
public void readWorkingSets(IPluginRegistry in, WorkingSetRegistry out) {
	registry = out;
	readRegistry(in, IWorkbenchConstants.PLUGIN_ID, IWorkbenchConstants.PL_WORKINGSETS);
}
}
