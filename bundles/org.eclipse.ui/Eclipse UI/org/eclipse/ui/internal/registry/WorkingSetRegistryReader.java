package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.*;

/**
 * A strategy to read working set extensions from the registry.
 */
public class WorkingSetRegistryReader extends RegistryReader {
	private static final String TAG="workingSet";//$NON-NLS-1$
	private WorkingSetRegistry registry;
	
/**
 * RegistryViewReader constructor comment.
 */
public WorkingSetRegistryReader() {
	super();
}
/**
 * readElement method comment.
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
 * Read the view extensions within a registry.
 */
public void readWorkingSets(IPluginRegistry in, WorkingSetRegistry out) {
	registry = out;
	readRegistry(in, IWorkbenchConstants.PLUGIN_ID, IWorkbenchConstants.PL_WORKINGSETS);
}
}
