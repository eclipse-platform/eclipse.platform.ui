package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
/**
 * This dialog is an example of a detached window launched
 * from an action in the desktop.
 */
public class SectionsDialog extends Dialog {
	protected IAdaptable input;
/**
 * Creates a new SectionsDialog.
 */
public SectionsDialog(Shell parentShell, IAdaptable input) {
	super(parentShell);
	this.input = input;
}
/* (non-Javadoc)
 * Method declared on Window.
 */
protected void configureShell(Shell newShell) {
	super.configureShell(newShell);
	newShell.setText(MessageUtil.getString("Readme_Sections")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(newShell, IReadmeConstants.SECTIONS_DIALOG_CONTEXT);
}
/* (non-Javadoc)
 * Method declared on Dialog
 */
protected Control createDialogArea(Composite parent) {
	Composite composite = (Composite)super.createDialogArea(parent);
	
	List list = new List(composite, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_BOTH);
	list.setLayoutData(data);
	ListViewer viewer = new ListViewer(list);
	viewer.setContentProvider(new WorkbenchContentProvider());
	viewer.setLabelProvider(new WorkbenchLabelProvider());
	viewer.setInput(input);

	return composite;
}
}
