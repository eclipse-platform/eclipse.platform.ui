/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.io.File;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.*;

/**
 * Widget group for specifying a linked file or folder target.
 * 
 * @since 2.1
 */
public class CreateLinkedResourceGroup {	
	private Listener listener;
	private String initialLinkTarget;
	private int type;
	private boolean createLink = false;

	// used to compute layout sizes
	private FontMetrics fontMetrics;

	// widgets
	private Composite groupComposite;
	private Button linkButton;
	private Text linkTargetField;
	private Button browseButton;
	private Button variablesButton;
	private Label resolvedPathLabelText;
	private Label resolvedPathLabelData;
 
/**
 * Creates a link target group 
 *
 * @param type specifies the type of resource to link to. 
 * 	<code>IResource.FILE</code> or <code>IResource.FOLDER</code>
 * @param listener listener to notify when one of the widgets'
 * 	value is changed.
 */ 
public CreateLinkedResourceGroup(int type, Listener listener) {
	this.type = type;
	this.listener = listener;
}
/**
 * Creates the widgets 
 * 
 * @param parent parent composite of the widget group
 * @return the widget group
 */
public Composite createContents(Composite parent) {
	Font font = parent.getFont();
	initializeDialogUnits(parent);
	// top level group
	groupComposite = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	groupComposite.setLayout(layout);
	groupComposite.setLayoutData(new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
	groupComposite.setFont(font);

	final Button createLinkButton = new Button(groupComposite, SWT.CHECK);
	if (type == IResource.FILE)
		createLinkButton.setText(WorkbenchMessages.getString("CreateLinkedResourceGroup.linkFileButton")); //$NON-NLS-1$
	else
		createLinkButton.setText(WorkbenchMessages.getString("CreateLinkedResourceGroup.linkFolderButton")); //$NON-NLS-1$
	createLinkButton.setSelection(createLink);
	createLinkButton.setFont(font);
	SelectionListener selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			createLink = createLinkButton.getSelection();
			browseButton.setEnabled(createLink);
			variablesButton.setEnabled(createLink);
			linkTargetField.setEnabled(createLink);
			if (listener != null)
				listener.handleEvent(new Event());
		}
	};
	createLinkButton.addSelectionListener(selectionListener);

	createLinkLocationGroup(groupComposite, createLink);
	return groupComposite;
}
/**
 * Creates the link target location widgets.
 *
 * @param locationGroup the parent composite
 * @param enabled sets the initial enabled state of the widgets
 */
private void createLinkLocationGroup(Composite locationGroup, boolean enabled) {
	Font font = locationGroup.getFont();
	Button button = new Button(locationGroup, SWT.CHECK);
	int indent = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
	
	button.dispose();

	// linkTargetGroup is necessary to decouple layout from 
	// resolvedPathGroup layout
	Composite linkTargetGroup = new Composite(locationGroup, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	linkTargetGroup.setLayout(layout);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.horizontalIndent = indent;
	linkTargetGroup.setLayoutData(data);
	linkTargetGroup.setFont(font);
	
	// link target location entry field
	linkTargetField = new Text(linkTargetGroup, SWT.BORDER);
	data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
	linkTargetField.setLayoutData(data);
	linkTargetField.setFont(font);
	linkTargetField.setEnabled(enabled);
	linkTargetField.addModifyListener(new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			resolveVariable();
			if (listener != null)
				listener.handleEvent(new Event());
		}
	});
	if (initialLinkTarget != null) {
		linkTargetField.setText(initialLinkTarget);
	}

	// browse button
	browseButton = new Button(linkTargetGroup, SWT.PUSH);
	setButtonLayoutData(browseButton);
	browseButton.setFont(font);
	browseButton.setText(WorkbenchMessages.getString("CreateLinkedResourceGroup.browseButton")); //$NON-NLS-1$
	browseButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleLinkTargetBrowseButtonPressed();
		}
	});
	browseButton.setEnabled(enabled);

	// variables button
	variablesButton = new Button(linkTargetGroup, SWT.PUSH);
	setButtonLayoutData(variablesButton);
	variablesButton.setFont(font);
	variablesButton.setText(WorkbenchMessages.getString("CreateLinkedResourceGroup.variablesButton")); //$NON-NLS-1$
	variablesButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleVariablesButtonPressed();
		}
	});
	variablesButton.setEnabled(enabled);

	Composite resolvedPathGroup = new Composite(locationGroup, SWT.NONE);
	layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	resolvedPathGroup.setLayout(layout);
	data = new GridData(GridData.FILL_HORIZONTAL);
	data.horizontalIndent = indent;
	resolvedPathGroup.setLayoutData(data);
	resolvedPathGroup.setFont(font);

	resolvedPathLabelText = new Label(resolvedPathGroup, SWT.SINGLE);
	resolvedPathLabelText.setText(WorkbenchMessages.getString("CreateLinkedResourceGroup.resolvedPathLabel")); //$NON-NLS-1$

	resolvedPathLabelData = new Label(resolvedPathGroup, SWT.SINGLE);
	data = new GridData(GridData.FILL_HORIZONTAL);
	resolvedPathLabelData.setLayoutData(data);
}
/**
 * Returns a new status object with the given severity and message.
 * 
 * @return a new status object with the given severity and message.
 */
private IStatus createStatus(int severity, String message) {
	return new Status(
		severity,
		WorkbenchPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
		severity,
		message,	
		null);
}	
/**
 * Disposes the group's widgets. 
 */
public void dispose() {
	if (groupComposite != null && groupComposite.isDisposed() == false)
	 	groupComposite.dispose();
}
/**
 * Returns the link target location entered by the user. 
 *
 * @return the link target location entered by the user. null if the user
 * 	chose not to create a link.
 */
public String getLinkTarget() {
	if (createLink && linkTargetField != null && linkTargetField.isDisposed() == false) {
		return linkTargetField.getText();
	}
	return null;
}
/**
 * Opens a file or directory browser depending on the link type.
 */
private void handleLinkTargetBrowseButtonPressed() {
	String linkTargetName = linkTargetField.getText();
	File file = null;
	String selection = null;
	
	if ("".equals(linkTargetName) == false) {	//$NON-NLS-1$
		file = new File(linkTargetName);
		if (file.exists() == false) {
			file = null;
		}
	}
	if (type == IResource.FILE) {
		FileDialog dialog = new FileDialog(linkTargetField.getShell());
		if (file != null) {
			if (file.isFile()) {
				dialog.setFileName(linkTargetName);
			}
			else {
				dialog.setFilterPath(linkTargetName);
			}
		}
		selection = dialog.open();		
	}
	else {
		DirectoryDialog dialog = new DirectoryDialog(linkTargetField.getShell());
		if (file != null) {
			if (file.isFile()) {
				linkTargetName = file.getParent();
			}
			if (linkTargetName != null) {
				dialog.setFilterPath(linkTargetName);
			}
		}
		dialog.setMessage(WorkbenchMessages.getString("CreateLinkedResourceGroup.targetSelectionLabel")); //$NON-NLS-1$
		selection = dialog.open();
	}					
	if (selection != null) {
		linkTargetField.setText(selection);
	}
}
/**
 * Opens a path variable selection dialog
 */
private void handleVariablesButtonPressed() {
	PathVariableSelectionDialog dialog = 
		new PathVariableSelectionDialog(linkTargetField.getShell(), IResource.FILE | IResource.FOLDER);
	
	if (dialog.open() == IDialogConstants.OK_ID) {
		String[] variableNames = (String[]) dialog.getResult();
				
		if (variableNames != null) {
			linkTargetField.setText(variableNames[0]);
		}
	}
}
/**
 * Initializes the computation of horizontal and vertical dialog units
 * based on the size of current font.
 * <p>
 * This method must be called before <code>setButtonLayoutData</code> 
 * is called.
 * </p>
 *
 * @param control a control from which to obtain the current font
 */
protected void initializeDialogUnits(Control control) {
	// Compute and store a font metric
	GC gc = new GC(control);
	gc.setFont(control.getFont());
	fontMetrics = gc.getFontMetrics();
	gc.dispose();
}
/**
 * Tries to resolve the value entered in the link target field as 
 * a variable, if the value is a relative path.
 * Displays the resolved value if the entered value is a variable.
 */
private void resolveVariable() {
	IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
	IPath path = new Path(linkTargetField.getText());
	IPath resolvedPath = pathVariableManager.resolvePath(path);
	
	if (path.equals(resolvedPath)) {
		resolvedPathLabelText.setVisible(false);
		resolvedPathLabelData.setVisible(false);
	}
	else {
		resolvedPathLabelText.setVisible(true);
		resolvedPathLabelData.setVisible(true);
		resolvedPathLabelData.setText(resolvedPath.toOSString());
	}
}
/**
 * Sets the <code>GridData</code> on the specified button to
 * be one that is spaced for the current dialog page units. The
 * method <code>initializeDialogUnits</code> must be called once
 * before calling this method for the first time.
 *
 * @param button the button to set the <code>GridData</code>
 * @return the <code>GridData</code> set on the specified button
 */
private GridData setButtonLayoutData(Button button) {
	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	data.heightHint = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_HEIGHT);
	int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
	data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	button.setLayoutData(data);
	return data;
}
/**
 * Sets the value of the link target field
 * 
 * @param target the value of the link target field
 */
public void setLinkTarget(String target) {
	initialLinkTarget = target;
	if (linkTargetField != null && linkTargetField.isDisposed() == false) {
		linkTargetField.setText(target);
	}
}
/**
 * Validates the type of the given file against the link type specified
 * in the constructor.
 * 
 * @param linkTargetFile file to validate
 * @return IStatus indicating the validation result. IStatus.OK if the 
 * 	given file is valid.
 */
private IStatus validateFileType(File linkTargetFile) {
	if (type == IResource.FILE && linkTargetFile.isFile() == false) {
		return createStatus(
			IStatus.ERROR,
			WorkbenchMessages.getString("CreateLinkedResourceGroup.linkTargetNotFile"));	//$NON-NLS-1$
	}
	else
	if (type == IResource.FOLDER && linkTargetFile.isDirectory() == false) {
		return createStatus(
			IStatus.ERROR,
			WorkbenchMessages.getString("CreateLinkedResourceGroup.linkTargetNotFolder"));	//$NON-NLS-1$
	}
	return createStatus(IStatus.OK, "");
}
/**
 * Validates this page's controls.
 *
 * @return IStatus indicating the validation result. IStatus.OK if the 
 * 	specified link target is valid given the linkHandle.
 */
public IStatus validateLinkLocation(IResource linkHandle) {
	if (linkTargetField == null || linkTargetField.isDisposed())
		return createStatus(IStatus.OK, "");	//$NON-NLS-1$
	
	IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();
	String linkTargetName = linkTargetField.getText();
	IPath path = new Path(linkTargetName);
	
	if (createLink == false)
		return createStatus(IStatus.OK, "");

	IStatus locationStatus = workspace.validateLinkLocation(linkHandle,	path);
	if (locationStatus.getSeverity() != IStatus.OK) {
		return locationStatus;
	}
	// use the resolved link target name
	linkTargetName = resolvedPathLabelData.getText();
	path = new Path(linkTargetName);
	File linkTargetFile = new Path(linkTargetName).toFile();
	if (linkTargetFile.exists() == false) {
		return createStatus(
			IStatus.WARNING,
			WorkbenchMessages.getString("CreateLinkedResourceGroup.linkTargetNonExistent"));	//$NON-NLS-1$
	}
	IStatus fileTypeStatus = validateFileType(linkTargetFile);
	if (fileTypeStatus.isOK() == false) {
		return fileTypeStatus;
	}
	return locationStatus;
}
}
