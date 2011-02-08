/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.ide.undo.UpdateMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.Util;

/**
 * MarkersPropertyPage is the property page for a marker.
 * 
 * @since 3.4
 * 
 */
public class MarkersPropertyPage extends PropertyPage {

	private Text descriptionText;
	private IMarker marker;
	Combo priorityCombo;
	Button completedCheckbox;

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkersPropertyPage() {
		super();
	}

	protected Control createContents(Composite parent) {
		// initialize resources/properties

		Object element = getElement().getAdapter(IMarker.class);
		IResource resource = null;

		if (element != null) {
			marker = (IMarker) element;
			resource = marker.getResource();
		} else if (resource == null) {
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}

		if (!Util.isEditable(marker))
			noDefaultAndApplyButton();

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);

		initializeDialogUnits(composite);
		createDescriptionArea(composite);
		if (element != null) {
			createSeperator(composite);
			createCreationTimeArea(composite);
		}
		createAttributesArea(composite);
		if (resource != null) {
			createSeperator(composite);
			createResourceArea(composite);
		}

		Dialog.applyDialogFont(composite);

		return composite;
	}

	/**
	 * Creates a separator.
	 */
	protected void createSeperator(Composite parent) {
		Label seperator = new Label(parent, SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		seperator.setLayoutData(gridData);
	}

	/**
	 * Method createCreationTimeArea.
	 * 
	 * @param parent
	 */
	private void createCreationTimeArea(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(MarkerMessages.propertiesDialog_creationTime_text);

		Text creationTime = new Text(parent, SWT.SINGLE | SWT.READ_ONLY);
		creationTime.setText(Util.getCreationTime(marker));
	}

	/**
	 * Creates the area for the Description field.
	 */
	private void createDescriptionArea(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(MarkerMessages.propertiesDialog_description_text);
		GridData labelGridData= new GridData(SWT.LEFT, SWT.TOP, false, false);
		label.setLayoutData(labelGridData);
		descriptionText = new Text(parent, (SWT.MULTI|SWT.WRAP|SWT.V_SCROLL|SWT.BORDER));
		labelGridData.verticalIndent= -descriptionText.computeTrim(0, 0, 0, 0).y;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = convertHorizontalDLUsToPixels(250);
		gridData.heightHint = convertHeightInCharsToPixels(3);
		descriptionText.setLayoutData(gridData);
		descriptionText.setText(Util.getProperty(IMarker.MESSAGE, marker));
		descriptionText.selectAll();
		descriptionText.setEditable(Util.isEditable(marker));

	}

	/**
	 * This method is intended to be overridden by subclasses. The attributes
	 * area is created between the creation time area and the resource area.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createAttributesArea(Composite parent) {
		try {
			if (marker.isSubtypeOf(IMarker.PROBLEM))
				createProblemAttributes(parent);

			if (marker.isSubtypeOf(IMarker.TASK))
				createTaskAttributes(parent);
		} catch (CoreException e) {
			Policy.handle(e);
		}
	}

	/**
	 * Create the attributes area for editing a task
	 * 
	 * @param parent
	 */
	private void createTaskAttributes(Composite parent) {
		createSeperator(parent);

		Label label = new Label(parent, SWT.NONE);
		label.setText(MarkerMessages.propertiesDialog_priority);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		priorityCombo = new Combo(composite, SWT.READ_ONLY);
		priorityCombo.setItems(new String[] {
				MarkerMessages.propertiesDialog_priorityLow,
				MarkerMessages.propertiesDialog_priorityNormal,
				MarkerMessages.propertiesDialog_priorityHigh });

		priorityCombo.select(marker.getAttribute(IMarker.PRIORITY,
				IMarker.PRIORITY_NORMAL));
		priorityCombo.setEnabled(Util.isEditable(marker));

		completedCheckbox = new Button(composite, SWT.CHECK);
		completedCheckbox.setText(MarkerMessages.propertiesDialog_completed);
		GridData gridData = new GridData();
		gridData.horizontalIndent = convertHorizontalDLUsToPixels(20);
		completedCheckbox.setLayoutData(gridData);
		
		completedCheckbox.setEnabled(Util.isEditable(marker));
		
		Object done;
		try {
			done = marker.getAttribute(IMarker.DONE);
			completedCheckbox.setSelection(done != null
					&& done instanceof Boolean
					&& ((Boolean) done).booleanValue());
		} catch (CoreException e) {
			Policy.handle(e);
		}

	}

	/**
	 * Create the attributes area for problems
	 * 
	 * @param parent
	 */
	private void createProblemAttributes(Composite parent) {
		createSeperator(parent);

		new Label(parent, SWT.NONE)
				.setText(MarkerMessages.propertiesDialog_severityLabel);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		Label severityImage = new Label(composite, SWT.NONE);

		severityImage.setImage(Util.getImage(marker.getAttribute(
				IMarker.SEVERITY, -1)));

		Text severityLabel = new Text(composite, SWT.SINGLE | SWT.READ_ONLY);
		int severity = marker.getAttribute(IMarker.SEVERITY, -1);
		if (severity == IMarker.SEVERITY_ERROR) {
			severityLabel.setText(MarkerMessages.propertiesDialog_errorLabel);
		} else if (severity == IMarker.SEVERITY_WARNING) {
			severityLabel.setText(MarkerMessages.propertiesDialog_warningLabel);
		} else if (severity == IMarker.SEVERITY_INFO) {
			severityLabel.setText(MarkerMessages.propertiesDialog_infoLabel);
		} else {
			severityLabel
					.setText(MarkerMessages.propertiesDialog_noseverityLabel);
		}

	}

	/**
	 * Creates the area for the Resource field.
	 */
	private void createResourceArea(Composite parent) {
		Label resourceLabel = new Label(parent, SWT.NONE);
		resourceLabel.setText(MarkerMessages.propertiesDialog_resource_text);
		Text resourceText = new Text(parent, SWT.SINGLE | SWT.WRAP
				| SWT.READ_ONLY | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		resourceText.setLayoutData(gridData);
		resourceText.setText(Util.getResourceName(marker));

		Label folderLabel = new Label(parent, SWT.NONE);
		folderLabel.setText(MarkerMessages.propertiesDialog_folder_text);
		Text folderText = new Text(parent, SWT.SINGLE | SWT.WRAP
				| SWT.READ_ONLY | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		folderText.setLayoutData(gridData);
		folderText.setText(Util.getContainerName(marker));

		Label locationLabel = new Label(parent, SWT.NONE);
		locationLabel.setText(MarkerMessages.propertiesDialog_location_text);
		Text locationText = new Text(parent, SWT.SINGLE | SWT.WRAP
				| SWT.READ_ONLY | SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		locationText.setLayoutData(gridData);

		String line = Util.getProperty(IMarker.LINE_NUMBER, marker);
		if (line.length()==0)
			locationText.setText(MarkerSupportInternalUtilities.EMPTY_STRING);
		else
			locationText.setText(NLS
					.bind(MarkerMessages.label_lineNumber, line));

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		if (marker == null || Util.isEditable(marker)) {
			saveChanges();
		}
		return super.performOk();
	}

	/**
	 * Saves the changes made in the dialog if needed. Creates a new marker if
	 * needed. Updates the existing marker only if there have been changes.
	 */
	private void saveChanges() {
		Map attrs;
		try {
			attrs = marker.getAttributes();
		} catch (CoreException e) {
			attrs = new HashMap();
			Policy.handle(e);
		}

		attrs.put(IMarker.MESSAGE, descriptionText.getText());

		// Are we editing a task?
		if (priorityCombo != null) {
			int priority = IMarker.PRIORITY_NORMAL;

			int index = priorityCombo.getSelectionIndex();
			if (index == priorityCombo
					.indexOf(MarkerMessages.propertiesDialog_priorityHigh))
				priority = IMarker.PRIORITY_HIGH;
			else if (index == priorityCombo
					.indexOf(MarkerMessages.propertiesDialog_priorityLow))
				priority = IMarker.PRIORITY_LOW;

			attrs.put(IMarker.PRIORITY, new Integer(priority));
		}

		if (completedCheckbox != null)
			attrs.put(IMarker.DONE,
					completedCheckbox.getSelection() ? Boolean.TRUE
							: Boolean.FALSE);

		IUndoableOperation op = new UpdateMarkersOperation(marker, attrs, NLS
				.bind(MarkerMessages.qualifiedMarkerCommand_title,
						new Object[] {
								MarkerMessages.DialogMarkerProperties_Modify,
								Util.getResourceName(marker) }), true);

		try {
			PlatformUI.getWorkbench().getOperationSupport()
					.getOperationHistory().execute(op, new NullProgressMonitor(),
							WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
		} catch (ExecutionException e) {
			if (e.getCause() instanceof CoreException) {
				StatusManager.getManager().handle(
						((CoreException) e.getCause()).getStatus(),
						StatusManager.SHOW);
			} else
				StatusManager.getManager().handle(
						StatusUtil.newStatus(IStatus.ERROR, e
								.getLocalizedMessage(), e));
		}

	}

}
