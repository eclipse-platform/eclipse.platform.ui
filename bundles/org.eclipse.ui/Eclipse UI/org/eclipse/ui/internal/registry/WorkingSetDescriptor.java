package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.dialogs.IWorkingSetDialog;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A working set descriptor stores the plugin registry data for 
 * a working set dialog extension.
 * 
 * @since 2.0
 */
public class WorkingSetDescriptor {
	private String id;
	private String elementClassName;
	private String dialogClassName;
	private IConfigurationElement configElement;

	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_ELEMENT_CLASS = "elementClass"; //$NON-NLS-1$
	private static final String ATT_DIALOG_CLASS = "dialogClass"; //$NON-NLS-1$	

	/**
	 * Create a descriptor from a configuration element.
	 * 
	 * @param configElement configuration element to create a descriptor from
	 */
	public WorkingSetDescriptor(IConfigurationElement configElement) throws CoreException {
		super();
		this.configElement = configElement;
		id = configElement.getAttribute(ATT_ID);
		elementClassName = configElement.getAttribute(ATT_ELEMENT_CLASS);
		dialogClassName = configElement.getAttribute(ATT_DIALOG_CLASS);

		if (elementClassName == null) {
			throw new CoreException(new Status(
				IStatus.ERROR, 
				WorkbenchPlugin.PI_WORKBENCH, 
				0, 
				"Invalid extension (missing class name): " + id, 		//$NON-NLS-1$
				null));
		}
		if (dialogClassName == null) {
			throw new CoreException(new Status(
				IStatus.ERROR,
				WorkbenchPlugin.PI_WORKBENCH,
				0,
				"Invalid extension (missing dialog class name): " + id,	//$NON-NLS-1$
				null));
		}
	}
	/**
	 * Creates a working set dialog from this extension descriptor.
	 * 
	 * @return a working set dialog created from this extension 
	 * 	descriptor.
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
	/**
	 * Returns the working set dialog class name
	 * 
	 * @return the working set dialog class name
	 */
	public String getDialogClassName() {
		return dialogClassName;
	}
	/**
	 * Returns the name of the working set element type the 
	 * dialog works with.
	 * 
	 * @return the working set element type name
	 */
	public String getElementClassName() {
		return elementClassName;
	}
	/**
	 * Returns the working set dialog id.
	 */
	public String getDialogId() {
		return id;
	}
}