package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A strategy to read view extensions from the registry.
 */
public class PerspectiveRegistryReader extends RegistryReader {
	private static final String TAG_LAYOUT="perspective";//$NON-NLS-1$
	private PerspectiveRegistry registry;
	
/**
 * RegistryViewReader constructor comment.
 */
public PerspectiveRegistryReader() {
	super();
}
/**
 * readElement method comment.
 */
protected boolean readElement(IConfigurationElement element) {
	if (element.getName().equals(TAG_LAYOUT)) {
		try {
			PerspectiveDescriptor desc = new PerspectiveDescriptor(element);
			registry.addPerspective(desc);
		} catch (CoreException e) {
			// log an error since its not safe to open a dialog here
			WorkbenchPlugin.log("Unable to create layout descriptor.",e.getStatus());//$NON-NLS-1$
		}
		return true;
	}
	
	return false;
}
/**
 * Read the view extensions within a registry.
 */
public void readPerspectives(IPluginRegistry in, PerspectiveRegistry out)
{
	registry = out;
	readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_PERSPECTIVES);
}
}
