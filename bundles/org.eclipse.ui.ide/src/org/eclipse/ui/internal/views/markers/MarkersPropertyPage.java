/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
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
	Button copyButton;

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkersPropertyPage() {
		super();
	}

	@Override
	protected Control createContents(Composite parent) {
		// initialize resources/properties

		IMarker element = Adapters.adapt(getElement(), IMarker.class);
		IResource resource = null;

		if (element != null) {
			marker = element;
			resource = marker.getResource();
		}
		if (resource == null) {
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}

		if (!Util.isEditable(marker)) {
			noDefaultAndApplyButton();
		}

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);

		initializeDialogUnits(composite);
		createAttributesArea(composite);
		createResourceNameArea(composite);
		if (resource != null) {
			createResourceArea(composite);
		}
		if (element != null) {
			createCreationTimeArea(composite);
		}
		createSeparator(composite);
		createDescriptionArea(composite);
		Dialog.applyDialogFont(composite);
		return composite;
	}

	/**
	 * Creates a separator.
	 */
	protected void createSeparator(Composite parent) {
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
		Text creationTime = createReadOnlyText(parent);
		creationTime.setText(Util.getCreationTime(marker));
	}

	private static Text createReadOnlyText(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE | SWT.READ_ONLY);
		text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gridData);
		return text;
	}

	/**
	 * Creates the area for the Description field.
	 */
	private void createDescriptionArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData cGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		cGridData.horizontalSpan = 2;
		composite.setLayoutData(cGridData);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NONE);
		label.setText(MarkerMessages.propertiesDialog_description_text);
		GridData labelGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		label.setLayoutData(labelGridData);

		Composite textContainer = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		textContainer.setLayout(layout);
		textContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		descriptionText = new Text(textContainer, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = gridData.heightHint = 0;
		gridData.grabExcessHorizontalSpace = true;
		descriptionText.setLayoutData(gridData);
		descriptionText.setText(Util.getProperty(IMarker.MESSAGE, marker));
		descriptionText.setEditable(Util.isEditable(marker));

		copyButton = new Button(textContainer, SWT.PUSH);
		copyButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
		copyButton.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		((GridData) copyButton.getLayoutData()).verticalAlignment = SWT.BOTTOM;
		copyButton.addListener(SWT.Selection, event -> {
			Clipboard clipboard = new Clipboard(event.display);
			try {
				clipboard.setContents(new Object[] { descriptionText.getText() },
						new Transfer[] { TextTransfer.getInstance() });
			} finally {
				clipboard.dispose();
			}
		});
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
			if (marker.isSubtypeOf(IMarker.PROBLEM)) {
				createProblemAttributes(parent);
			}

			if (marker.isSubtypeOf(IMarker.TASK)) {
				createTaskAttributes(parent);
			}
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

		priorityCombo.select(marker.getAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL));
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
		new Label(parent, SWT.NONE).setText(MarkerMessages.propertiesDialog_severityLabel);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		Label severityImage = new Label(composite, SWT.NONE);

		severityImage.setImage(Util.getImage(marker.getAttribute(IMarker.SEVERITY, -1)));

		Text severityLabel = createReadOnlyText(composite);
		int severity = marker.getAttribute(IMarker.SEVERITY, -1);
		if (severity == IMarker.SEVERITY_ERROR) {
			severityLabel.setText(MarkerMessages.propertiesDialog_errorLabel);
		} else if (severity == IMarker.SEVERITY_WARNING) {
			severityLabel.setText(MarkerMessages.propertiesDialog_warningLabel);
		} else if (severity == IMarker.SEVERITY_INFO) {
			severityLabel.setText(MarkerMessages.propertiesDialog_infoLabel);
		} else {
			severityLabel.setText(MarkerMessages.propertiesDialog_noseverityLabel);
		}
	}

	/**
	 * Creates the area for the Resource field.
	 */
	private void createResourceArea(Composite parent) {
		createResourcePathArea(parent);

		Label locationLabel = new Label(parent, SWT.NONE);
		locationLabel.setText(MarkerMessages.propertiesDialog_location_text);
		Text locationText = createReadOnlyText(parent);

		String line = Util.getProperty(IMarker.LINE_NUMBER, marker);
		if (line.length() == 0) {
			String location = Util.getProperty(IMarker.LOCATION, marker);
			if (location.length() == 0) {
				locationText.setText(MarkerSupportInternalUtilities.EMPTY_STRING);
			} else {
				locationText.setText(location);
			}
		} else {
			locationText.setText(NLS.bind(MarkerMessages.label_lineNumber, line));
		}
	}

	/**
	 * Creates the area for resource path
	 */
	private void createResourcePathArea(Composite parent) {
		String containerName = Util.getContainerName(marker);
		if (!containerName.isEmpty()) {
			Label folderLabel = new Label(parent, SWT.NONE);
			folderLabel.setText(MarkerMessages.propertiesDialog_folder_text);
			Text folderText = createReadOnlyText(parent);
			folderText.setText(Util.getContainerName(marker));
		}
	}

	/**
	 * Creates the area for resource name
	 */
	private void createResourceNameArea(Composite parent) {
		Label resourceLabel = new Label(parent, SWT.NONE);
		resourceLabel.setText(MarkerMessages.propertiesDialog_resource_text);
		Text resourceText = createReadOnlyText(parent);
		resourceText.setText(Util.getResourceName(marker));
	}

	@Override
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
		Map<String, Object> attrs;
		try {
			attrs = marker.getAttributes();
		} catch (CoreException e) {
			attrs = new HashMap<>();
			Policy.handle(e);
		}

		attrs.put(IMarker.MESSAGE, descriptionText.getText());

		// Are we editing a task?
		if (priorityCombo != null) {
			int priority = IMarker.PRIORITY_NORMAL;

			int index = priorityCombo.getSelectionIndex();
			if (index == priorityCombo.indexOf(MarkerMessages.propertiesDialog_priorityHigh)) {
				priority = IMarker.PRIORITY_HIGH;
			} else if (index == priorityCombo.indexOf(MarkerMessages.propertiesDialog_priorityLow)) {
				priority = IMarker.PRIORITY_LOW;
			}

			attrs.put(IMarker.PRIORITY, priority);
		}

		if (completedCheckbox != null) {
			attrs.put(IMarker.DONE, completedCheckbox.getSelection() ? Boolean.TRUE : Boolean.FALSE);
		}

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
			Throwable cause = e.getCause();
			if (cause instanceof CoreException) {
				StatusManager.getManager().handle(((CoreException) cause).getStatus(), StatusManager.SHOW);
			} else {
				StatusManager.getManager().handle(StatusUtil.newError(e));
			}
		}

	}

}
