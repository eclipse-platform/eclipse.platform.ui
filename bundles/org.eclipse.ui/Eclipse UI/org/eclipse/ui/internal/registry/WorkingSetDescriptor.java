package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetDialog;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * PerspectiveDescriptor.
 * <p>
 * A PerspectiveDesciptor has 3 states:
 * </p>
 * <ol>
 * <li>It <code>isPredefined()</code>, in which case it was defined from an
 * extension point.</li>
 * <li>It <code>isPredefined()</code> and <code>hasCustomFile</code>, in which 
 * case the user has customized a predefined perspective.</li>
 * <li>It <code>hasCustomFile</code>, in which case the user created a
 * new perspective.</li>
 * </ol>
 */
public class WorkingSetDescriptor {
	private String id;
	private String className;
	private String dialogId;		
	private String dialogClassName;	
//	private ImageDescriptor image;
	private IConfigurationElement configElement;
	
	private static final String ATT_ID="id";//$NON-NLS-1$
//	private static final String ATT_ICON="icon";//$NON-NLS-1$
	private static final String ATT_CLASS="class";//$NON-NLS-1$
	private static final String ATT_DIALOG_ID="dialogId";//$NON-NLS-1$
	private static final String ATT_DIALOG_CLASS="dialogClass";//$NON-NLS-1$	
		
/**
 * Create a descriptor from a config element.
 */
public WorkingSetDescriptor(IConfigurationElement configElement)
	throws CoreException
{
	super();
	this.configElement = configElement;
	id = configElement.getAttribute(ATT_ID);
	className = configElement.getAttribute(ATT_CLASS);
	dialogId = configElement.getAttribute(ATT_DIALOG_ID);	
	dialogClassName = configElement.getAttribute(ATT_DIALOG_CLASS);

	// Sanity check.
	if (className == null) {
		throw new CoreException(new Status(IStatus.ERROR,
			WorkbenchPlugin.PI_WORKBENCH, 0,
			"Invalid extension (missing class name): " + id,//$NON-NLS-1$
			null));
	}
	if (dialogClassName == null) {
		throw new CoreException(new Status(IStatus.ERROR,
			WorkbenchPlugin.PI_WORKBENCH, 0,
			"Invalid extension (missing class name): " + dialogId,//$NON-NLS-1$
			null));
	}

	// Load icon.
/*	String icon = configElement.getAttribute(ATT_ICON);
	if (icon != null) {
		image = WorkbenchImages.getImageDescriptorFromExtension(
			configElement.getDeclaringExtension(), icon);
	}*/
}
/**
 *
 * @throws a CoreException if the object could not be instantiated.
 */
public IWorkingSet createWorkingSet() throws CoreException {
	if (className == null)
		return null;
	Object obj = WorkbenchPlugin.createExtension(configElement, ATT_CLASS);
	return (IWorkingSet) obj;
}
/**
 *
 * @throws a CoreException if the object could not be instantiated.
 */
public IWorkingSetDialog createWorkingSetDialog() {
	Object dialog = null;
	
	if (dialogClassName != null) {
		try {
			dialog = WorkbenchPlugin.createExtension(configElement, ATT_DIALOG_CLASS);
		} catch (CoreException e) {
			WorkbenchPlugin.log("Unable to create working set dialog: " + //$NON-NLS-1$
				dialogClassName, e.getStatus());
		}
	}
	return (IWorkingSetDialog) dialog;
}
public String getDialogClassName() {
	return dialogClassName;
}
/**
 * Returns the ID.
 */
public String getWorkingSetId() {
	return id;
}
public String getWorkingSetClassName() {
	return className;
}
/**
 * Returns the descriptor of the image for this perspective.
 *
 * @return the descriptor of the image to display next to this perspective
 */
/*public ImageDescriptor getImageDescriptor() {
	return image;
}*/
}
