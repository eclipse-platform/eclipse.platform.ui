/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Sebastian Davids <sdavids@gmx.de> - bug 77332 - [Markers] Add task dialog improvements
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * DialogTaskProperties is the properties dialog
 * for tasks.
 */
public class DialogTaskProperties extends DialogMarkerProperties {

	private static final String PRIORITY_HIGH =
		MarkerMessages.propertiesDialog_priorityHigh;

	private static final String PRIORITY_NORMAL =
		MarkerMessages.propertiesDialog_priorityNormal;

	private static final String PRIORITY_LOW =
		MarkerMessages.propertiesDialog_priorityLow;

	protected Combo priorityCombo;

	protected Button completedCheckbox;

	public DialogTaskProperties(Shell parentShell) {
		super(parentShell);
		setType(IMarker.TASK);
	}

	public DialogTaskProperties(Shell parentShell, String title) {
		super(parentShell, title);
		setType(IMarker.TASK);
	}

	@Override
	protected void createAttributesArea(Composite parent) {
		createSeperator(parent);
		super.createAttributesArea(parent);

		Label label = new Label(parent, SWT.NONE);
		label.setText(MarkerMessages.propertiesDialog_priority);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		priorityCombo = new Combo(composite, SWT.READ_ONLY);
		priorityCombo.setItems(PRIORITY_HIGH, PRIORITY_NORMAL, PRIORITY_LOW);
		// Prevent Esc and Return from closing the dialog when the combo is active.
		priorityCombo.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_ESCAPE
					|| e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
			}
		});
		priorityCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getMarker() == null) {
					Map<String, Object> initialAttributes = getInitialAttributes();
					initialAttributes.put(IMarker.PRIORITY, getPriorityFromDialog());
				}
				markDirty();
			}
		});

		completedCheckbox = new Button(composite, SWT.CHECK);
		completedCheckbox.setText(MarkerMessages.propertiesDialog_completed);
		GridData gridData = new GridData();
		gridData.horizontalIndent = convertHorizontalDLUsToPixels(20);
		completedCheckbox.setLayoutData(gridData);
		completedCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getMarker() == null) {
					Map<String, Object> initialAttributes = getInitialAttributes();
					initialAttributes.put(IMarker.DONE, completedCheckbox.getSelection() ? Boolean.TRUE : Boolean.FALSE);
				}
				markDirty();
			}
		});
	}

	protected boolean getCompleted() {
		IMarker marker = getMarker();
		if (marker == null) {
			Map<String, Object> attributes = getInitialAttributes();
			Object done = attributes.get(IMarker.DONE);
			return done != null && done instanceof Boolean
					&& ((Boolean) done).booleanValue();
		}
		return marker.getAttribute(IMarker.DONE, false);
	}

	protected int getPriority() {
		IMarker marker = getMarker();
		int priority = IMarker.PRIORITY_NORMAL;
		if (marker == null) {
			Map<String, Object> attributes = getInitialAttributes();
			Object priorityObj = attributes.get(IMarker.PRIORITY);
			if (priorityObj != null && priorityObj instanceof Integer) {
				priority = ((Integer) priorityObj).intValue();
			}
		} else {
			priority = marker.getAttribute(IMarker.PRIORITY,
					IMarker.PRIORITY_NORMAL);
		}
		return priority;
	}

	@Override
	protected void updateEnablement() {
		super.updateEnablement();
		priorityCombo.setEnabled(isEditable());
		completedCheckbox.setEnabled(isEditable());
	}

	@Override
	protected void updateDialogForNewMarker() {
		Map<String, Object> initialAttributes = getInitialAttributes();
		int priority = getPriority();
		initialAttributes.put(IMarker.PRIORITY, priority);
		if (priority == IMarker.PRIORITY_HIGH) {
			priorityCombo.select(priorityCombo.indexOf(PRIORITY_HIGH));
		} else if (priority == IMarker.PRIORITY_LOW) {
			priorityCombo.select(priorityCombo.indexOf(PRIORITY_LOW));
		} else {
			priorityCombo.select(priorityCombo.indexOf(PRIORITY_NORMAL));
		}
		boolean completed = getCompleted();
		initialAttributes.put(IMarker.DONE, completed ? Boolean.TRUE : Boolean.FALSE);
		completedCheckbox.setSelection(completed);
		super.updateDialogForNewMarker();
	}

	@Override
	protected void updateDialogFromMarker() {
		Map<String, Object> initialAttributes = getInitialAttributes();
		int priority = getPriority();
		initialAttributes.put(IMarker.PRIORITY, priority);
		if (priority == IMarker.PRIORITY_HIGH) {
			priorityCombo.select(priorityCombo.indexOf(PRIORITY_HIGH));
		} else if (priority == IMarker.PRIORITY_LOW) {
			priorityCombo.select(priorityCombo.indexOf(PRIORITY_LOW));
		} else {
			priorityCombo.select(priorityCombo.indexOf(PRIORITY_NORMAL));
		}
		boolean completed = getCompleted();
		initialAttributes.put(IMarker.DONE, completed ? Boolean.TRUE : Boolean.FALSE);
		completedCheckbox.setSelection(completed);
		super.updateDialogFromMarker();
	}

	private int getPriorityFromDialog() {
		int priority = IMarker.PRIORITY_NORMAL;
		if (priorityCombo.getSelectionIndex() == priorityCombo
				.indexOf(PRIORITY_HIGH)) {
			priority = IMarker.PRIORITY_HIGH;
		} else if (priorityCombo.getSelectionIndex() == priorityCombo
				.indexOf(PRIORITY_LOW)) {
			priority = IMarker.PRIORITY_LOW;
		}
		return priority;
	}

	@Override
	protected Map<String, Object> getMarkerAttributes() {
		Map<String, Object> attrs = super.getMarkerAttributes();
		attrs.put(IMarker.PRIORITY, getPriorityFromDialog());
		attrs.put(IMarker.DONE, completedCheckbox.getSelection() ? Boolean.TRUE : Boolean.FALSE);
		Object userEditable = attrs.get(IMarker.USER_EDITABLE);
		if (userEditable == null || !(userEditable instanceof Boolean)) {
			attrs.put(IMarker.USER_EDITABLE, Boolean.TRUE);
		}
		return attrs;
	}

	@Override
	protected String getModifyOperationTitle() {
		return MarkerMessages.modifyTask_title;
	}

	@Override
	protected String getCreateOperationTitle() {
		return MarkerMessages.DialogTaskProperties_CreateTask;

	}

}
