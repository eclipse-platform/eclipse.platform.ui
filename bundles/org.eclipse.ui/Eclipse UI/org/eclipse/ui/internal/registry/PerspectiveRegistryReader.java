package org.eclipse.ui.internal.registry;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.UIHackFinder;

/**
 * A strategy to read view extensions from the registry.
 */
public class PerspectiveRegistryReader extends RegistryReader {
	private static final String TAG_LAYOUT="perspective";
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
			WorkbenchPlugin.log("Unable to create layout descriptor.",e.getStatus());
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
	readRegistry(in, IWorkbenchConstants.PLUGIN_ID, IWorkbenchConstants.PL_PERSPECTIVES);
}
}
