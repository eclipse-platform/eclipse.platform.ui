package org.eclipse.ui.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.misc.ResourceAndContainerGroup;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog; // disambiguate from SWT
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 * A standard "Save As" dialog which solicits a path from the user. The
 * <code>getResult</code> method returns the path. Note that the folder
 * at the specified path might not exist and might need to be created.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see IFile#create
 * @see ContainerGenerator
 */
public class SaveAsDialog extends TitleAreaDialog {
	private IStructuredSelection currentSelection;
	private IContainer currentParent;
	private int lastContainerSegmentCount = 0;
	private IFile originalFile = null;
	private IPath result;

	// widgets
	private ResourceAndContainerGroup resourceGroup;
	private Button okButton;
	private Button cancelButton;
/**
 * Creates a new Save As dialog for no specific file.
 *
 * @param parentShell the parent shell
 */
public SaveAsDialog(Shell parentShell) {
	super(parentShell);
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText("Save As...");
}
public void create() {
	super.create();
	
	initializeControls();
	validatePage();
	resourceGroup.setFocus();
	setTitle("Save File As...");
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void createButtonsForButtonBar(Composite parent) {
	okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	// top level composite
	Composite parentComposite = (Composite)super.createDialogArea(parent);

	// create a composite with standard margins and spacing
	Composite composite = new Composite(parentComposite, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
	layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
	layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
	composite.setLayout(layout);
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	composite.setFont(parentComposite.getFont());

	Listener listener = new Listener() {
		public void handleEvent(Event event) {
			setDialogComplete(validatePage());
		}
	};

	resourceGroup = new ResourceAndContainerGroup(composite, listener, "File name:", "file");
	resourceGroup.setAllowExistingResources(true);

	return parentComposite;
}
/**
 * Returns the full path entered by the user.
 * <p>
 * Note that the file and container might not exist and would need to be created.
 * See the <code>IFile.create</code> method and the 
 * <code>ContainerGenerator</code> class.
 * </p>
 *
 * @return the path, or <code>null</code> if Cancel was pressed
 */
public IPath getResult() {
	return result;
}
/**
 * Initializes the controls of this dialog.
 */
private void initializeControls() {
	if (originalFile != null) {
		resourceGroup.setContainerFullPath(originalFile.getParent().getFullPath());
		resourceGroup.setResource(originalFile.getName());
	}
	setDialogComplete(validatePage());
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void okPressed() {
	result = resourceGroup.getContainerFullPath().append(resourceGroup.getResource());
	close();
}
/**
 * Sets the completion state of this dialog and adjusts the enable state of
 * the Ok button accordingly.
 *
 * @param value <code>true</code> if this dialog is compelete, and
 *  <code>false</code> otherwise
 */
protected void setDialogComplete(boolean value) {
	okButton.setEnabled(value);
}
/**
 * Sets the original file to use.
 *
 * @param originalFile the original file
 */
public void setOriginalFile(IFile originalFile) {
	this.originalFile = originalFile;
}
/**
 * Returns whether this page's visual components all contain valid values.
 *
 * @return <code>true</code> if valid, and <code>false</code> otherwise
 */
private boolean validatePage() {
	setErrorMessage(null);

	if (!resourceGroup.areAllValuesValid()) {
		if (!resourceGroup.getResource().equals(""))	// if blank name then fail silently
			setErrorMessage(resourceGroup.getProblemMessage());
		return false;
	}

	return true;
}
}
