/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.views.bookmarkexplorer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Shows the properties of a new or existing task, or a problem.
 */
public class BookmarkPropertiesDialog extends Dialog {

	/**
	 * The task or problem being shown, or <code>null</code> for a new task.
	 */
	private IMarker marker = null;

	/**
	 * The resource on which to create a new task.
	 */
	private IResource resource = null;

	/**
	 * The initial attributes to use when creating a new task.
	 */
	private Map initialAttributes = null;

	/**
	 * The text control for the Description field.
	 */
	private Text descriptionText;

	/**
	 * The control for the Creation Time field.
	 */
	private Label creationTime;

	/**
	 * The control for the Severity field.
	 */
	private Label severityLabel;

	/**
	 * The text control for the Resource field.
	 */
	private Text resourceText;

	/**
	 * The text control for the Folder field.
	 */
	private Text folderText;

	/**
	 * The text control for the Location field.
	 */
	private Text locationText;

	/**
	 * Dirty flag.  True if any changes have been made.
	 */
	private boolean dirty;

	private String title;

	/**
	 * Creates the dialog.  By default this dialog creates a new task.
	 * To set the resource and initial attributes for the new task, 
	 * use <code>setResource</code> and <code>setInitialAttributes</code>.
	 * To show or modify an existing task, use <code>setMarker</code>.
	 * 
	 * @param shell the parent shell
	 */
	public BookmarkPropertiesDialog(Shell parentShell) {
		super(parentShell);
	}

	public BookmarkPropertiesDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title = title;
	}

	public void create() {
		super.create();
	}

	/**
	 * Sets the marker to show or modify.
	 * 
	 * @param marker the marker, or <code>null</code> to create a new marker
	 */
	public void setMarker(IMarker marker) {
		this.marker = marker;
		resource = marker.getResource();
	}

	/**
	 * Returns the marker being created or modified.
	 * For a new marker, this returns <code>null</code> until
	 * the dialog returns, but is non-null after.
	 */
	public IMarker getMarker() {
		return marker;
	}

	/**
	 * Sets the resource to use when creating a new task.
	 * If not set, the new task is created on the workspace root.
	 */
	public void setResource(IResource resource) {
		this.resource = resource;
	}

	/**
	 * Returns the resource to use when creating a new task,
	 * or <code>null</code> if none has been set.
	 * If not set, the new task is created on the workspace root.
	 */
	public IResource getResource() {
		return resource;
	}

	/**
	 * Sets initial attributes to use when creating a new task.
	 * If not set, the new task is created with default attributes.
	 */
	public void setInitialAttributes(Map initialAttributes) {
		this.initialAttributes = initialAttributes;
	}

	/**
	 * Returns the initial attributes to use when creating a new task,
	 * or <code>null</code> if not set.
	 * If not set, the new task is created with default attributes.
	 */
	public Map getInitialAttributes() {
		return initialAttributes;
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (title == null)
			newShell.setText(
				BookmarkMessages.getString("PropertiesDialogTitle.text"));
		//$NON-NLS-1$
		else
			newShell.setText(title);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		initializeDialogUnits(composite);
		createDescriptionArea(composite);
		createCreationTimeArea(composite);
		//	if (isTask()) {
		//		createPriorityAndStatusArea(composite);
		//	}
		//	else {
		//		createSeverityArea(composite);
		//	}
		createResourceArea(composite);
		updateDialogFromMarker();
		return composite;
	}
	/**
	 * Method createCreationTimeArea.
	 * @param composite
	 */
	private void createCreationTimeArea(Composite parent) {
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NONE);
		label.setText(BookmarkMessages.getString("MarkerCreationTime.text")); //$NON-NLS-1$
		label.setFont(font);

		creationTime = new Label(composite, SWT.NONE);
		creationTime.setFont(font);
	}

	/**
	 * Creates only the OK button if showing problem properties, otherwise creates
	 * both OK and Cancel buttons.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(
			parent,
			IDialogConstants.OK_ID,
			IDialogConstants.OK_LABEL,
			true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			IDialogConstants.CANCEL_LABEL,
			false);
	}

	/**
	 * Creates the area for the Description field.
	 */
	private void createDescriptionArea(Composite parent) {
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		Label label = new Label(composite, SWT.NONE);
		label.setText(BookmarkMessages.getString("ColumnDescription.text")); //$NON-NLS-1$
		label.setFont(font);
		int style = SWT.SINGLE | SWT.BORDER;
		descriptionText = new Text(composite, style);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = convertHorizontalDLUsToPixels(400);
		descriptionText.setLayoutData(gridData);
		descriptionText.setFont(font);
	}

	/**
	 * Creates the area for the Resource field.
	 */
	private void createResourceArea(Composite parent) {
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label resourceLabel = new Label(composite, SWT.NONE);
		resourceLabel.setText(BookmarkMessages.getString("ColumnResource.text")); //$NON-NLS-1$
		resourceLabel.setFont(font);
		resourceText =
			new Text(
				composite,
				SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		resourceText.setLayoutData(gridData);
		resourceText.setFont(font);

		Label folderLabel = new Label(composite, SWT.NONE);
		folderLabel.setText(BookmarkMessages.getString("ColumnFolder.text")); //$NON-NLS-1$
		folderLabel.setFont(font);
		folderText =
			new Text(
				composite,
				SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		folderText.setLayoutData(gridData);
		folderText.setFont(font);

		Label locationLabel = new Label(composite, SWT.NONE);
		locationLabel.setText(BookmarkMessages.getString("ColumnLocation.text")); //$NON-NLS-1$
		locationLabel.setFont(font);
		locationText =
			new Text(
				composite,
				SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		locationText.setLayoutData(gridData);
		locationText.setFont(font);
	}

	/**
	 * Updates the dialog from the marker state.
	 */
	private void updateDialogFromMarker() {
		descriptionText.setText(MarkerUtil.getMessage(marker));
		creationTime.setText(MarkerUtil.getCreationTime(marker));
		resourceText.setText(MarkerUtil.getResourceName(marker));
		folderText.setText(MarkerUtil.getContainerName(marker));
		int line = MarkerUtil.getLineNumber(marker);
		if (line < 0)
			locationText.setText("");
		//$NON-NLS-1$
		else
			locationText.setText(BookmarkMessages.format("LineIndicator.text", new String[] {String.valueOf(line)})); //$NON-NLS-1$

		descriptionText.selectAll();
	}

	/* (non-Javadoc)
	 * Method declared on Dialog
	 */
	protected void okPressed() {
		saveChanges();
		super.okPressed();
	}

	private void markDirty() {
		dirty = true;
	}

	private boolean isDirty() {
		return dirty;
	}

	/**
	 * Saves the changes made in the dialog if needed.
	 * Creates a new task if needed.
	 * Updates the existing task only if there have been changes.
	 * Does nothing for problems, since they cannot be modified.
	 */
	private void saveChanges() {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor)
					throws CoreException {
					createOrUpdateMarker();
				}
			}, null);
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), BookmarkMessages.getString("Error"), null, e.getStatus()); //$NON-NLS-1$
			return;
		}
	}

	/**
	 * Creates or updates the marker.  Must be called within a workspace runnable.
	 */
	private void createOrUpdateMarker() throws CoreException {
		if (marker == null) {
			IResource resource = getResource();
			if (resource == null) {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
			marker = resource.createMarker(IMarker.TASK);
			Map initialAttrs = getInitialAttributes();
			if (initialAttrs != null) {
				marker.setAttributes(initialAttrs);
			}
		}

		// Set the marker attributes from the current dialog field values.
		// Do not use setAttributes(Map) as that overwrites any attributes
		// not covered by the dialog.
		Map attrs = getMarkerAttributesFromDialog();
		for (Iterator i = attrs.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			Object val = attrs.get(key);
			marker.setAttribute(key, val);
		}
	}

	/**
	 * Returns the marker attributes to save back to the marker, 
	 * based on the current dialog fields.
	 */
	private Map getMarkerAttributesFromDialog() {
		Map attribs = new HashMap(11);
		attribs.put(IMarker.MESSAGE, descriptionText.getText());
		return attribs;
	}

}