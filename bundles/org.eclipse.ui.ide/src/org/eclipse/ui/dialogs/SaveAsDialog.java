package org.eclipse.ui.dialogs;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *    IBM Corporation - initial API and implementation 
 *    Bob Foster <bob@objfac.com>
 *     - Fix for bug 23025 - SaveAsDialog should not assume what is being saved is an IFile
**********************************************************************/
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.ResourceAndContainerGroup;

/**
 * A standard "Save As" dialog which solicits a path from the user. The
 * <code>getResult</code> method returns the path. Note that the folder
 * at the specified path might not exist and might need to be created.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see org.eclipse.ui.dialogs.ContainerGenerator
 */
public class SaveAsDialog extends TitleAreaDialog {
	private IFile originalFile = null;
	private String originalName = null;
	private IPath result;

	// widgets
	private ResourceAndContainerGroup resourceGroup;
	private Button okButton;
	private Button cancelButton;

	/**
	 * Image for title area
	 */
	private Image dlgTitleImage = null;

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
	shell.setText(WorkbenchMessages.getString("SaveAsDialog.text")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(shell, IHelpContextIds.SAVE_AS_DIALOG);
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected Control createContents(Composite parent) {
	
	Control contents = super.createContents(parent);
	
	initializeControls();
	validatePage();
	resourceGroup.setFocus();
	setTitle(WorkbenchMessages.getString("SaveAsDialog.title")); //$NON-NLS-1$
	dlgTitleImage = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_DLGBAN_SAVEAS_DLG).createImage();
	setTitleImage(dlgTitleImage);
	setMessage(WorkbenchMessages.getString("SaveAsDialog.message")); //$NON-NLS-1$
	
	return contents;
}
/** 
 * The <code>SaveAsDialog</code> implementation of this <code>Window</code>
 * method disposes of the banner image when the dialog is closed.
 */
public boolean close() {
	if (dlgTitleImage != null) 
		dlgTitleImage.dispose();
	return super.close();
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

	resourceGroup = new ResourceAndContainerGroup(composite, listener, WorkbenchMessages.getString("SaveAsDialog.fileLabel"), WorkbenchMessages.getString("SaveAsDialog.file")); //$NON-NLS-2$ //$NON-NLS-1$
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
	else if (originalName != null)
		resourceGroup.setResource(originalName);
	setDialogComplete(validatePage());
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void okPressed() {
	// Get new path.
	IPath path = resourceGroup.getContainerFullPath().append(resourceGroup.getResource());

	
	//If the user does not supply a file extension and if the save 
	//as dialog was provided a default file name append the extension 
	//of the default filename to the new name
	if(path.getFileExtension() == null) {
		if (originalFile != null && originalFile.getFileExtension() != null)
			path = path.addFileExtension(originalFile.getFileExtension());
		else if (originalName != null) {
			int pos = originalName.lastIndexOf('.');
			if (++pos > 0 && pos < originalName.length())
				path = path.addFileExtension(originalName.substring(pos));
		}
	}

	// If the path already exists then confirm overwrite.
	IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	if (file.exists()) {
		String [] buttons= new String[] { 
			IDialogConstants.YES_LABEL,
			IDialogConstants.NO_LABEL,
			IDialogConstants.CANCEL_LABEL
		};
		String question = WorkbenchMessages.format("SaveAsDialog.overwriteQuestion", //$NON-NLS-1$
			new Object[] { path.toOSString() } );
		MessageDialog d= new MessageDialog(
			getShell(),
			WorkbenchMessages.getString("Question"), //$NON-NLS-1$
			null,
			question,
			MessageDialog.QUESTION,
			buttons,
			0
		);
		int overwrite = d.open();
		switch (overwrite) {
			case 0: // Yes
				break;
			case 1: // No
				return;
			case 2: // Cancel
			default:
				cancelPressed();
				return;
		}
	}

	// Store path and close.
	result = path;
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
 * Set the original file name to use.
 * Used instead of <code>setOriginalFile</code>
 * when the original resource is not an IFile.
 * Must be called before <code>create</code>.
 * @param originalName default file name
 */
public void setOriginalName(String originalName) {
	this.originalName = originalName;
}
/**
 * Returns whether this page's visual components all contain valid values.
 *
 * @return <code>true</code> if valid, and <code>false</code> otherwise
 */
private boolean validatePage() {
	setErrorMessage(null);

	if (!resourceGroup.areAllValuesValid()) {
		if (!resourceGroup.getResource().equals(""))	// if blank name then fail silently//$NON-NLS-1$
			setErrorMessage(resourceGroup.getProblemMessage());
		return false;
	}

	return true;
}
}
