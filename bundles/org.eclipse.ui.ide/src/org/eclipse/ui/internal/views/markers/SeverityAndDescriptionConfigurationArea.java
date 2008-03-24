package org.eclipse.ui.internal.views.markers;

/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * SeverityAndDescriptionConfigurationArea is the configuration area for the
 * severity and description field.
 * 
 * @since 3.4
 * 
 */
public class SeverityAndDescriptionConfigurationArea extends
		DescriptionConfigurationArea {

	private int severities;
	private Button infoButton;
	private Button errorButton;
	private Button warningButton;
	private Label label;

	/**
	 * Create a new instance of the receiver.
	 */
	public SeverityAndDescriptionConfigurationArea() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#apply(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void apply(MarkerFieldFilter filter) {
		super.apply(filter);
		((SeverityAndDescriptionFieldFilter) filter).selectedSeverities = severities;

	}

	/**
	 * Create a group for the severity selection.
	 * 
	 * @param parent
	 * @return {@link Composite}
	 */
	Composite createSeverityGroup(Composite parent) {

		Composite severityComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = IDialogConstants.BUTTON_MARGIN;
		severityComposite.setLayout(layout);
		severityComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));

		label = new Label(severityComposite, SWT.NONE);
		label.setText(MarkerMessages.filtersDialog_severityLabel);

		errorButton = new Button(severityComposite, SWT.CHECK);
		errorButton.setText(MarkerMessages.filtersDialog_severityError);
		errorButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updateSeverities(
						SeverityAndDescriptionFieldFilter.SEVERITY_ERROR,
						errorButton.getSelection());
			}
		});
		GridData data = new GridData();
		data.horizontalIndent = IDialogConstants.BUTTON_MARGIN;
		errorButton.setLayoutData(data);

		warningButton = new Button(severityComposite, SWT.CHECK);
		warningButton.setText(MarkerMessages.filtersDialog_severityWarning);
		warningButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updateSeverities(
						SeverityAndDescriptionFieldFilter.SEVERITY_WARNING,
						warningButton.getSelection());
			}
		});

		infoButton = new Button(severityComposite, SWT.CHECK);
		infoButton.setText(MarkerMessages.filtersDialog_severityInfo);
		infoButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updateSeverities(
						SeverityAndDescriptionFieldFilter.SEVERITY_INFO,
						infoButton.getSelection());
			}
		});
		return severityComposite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#initialize(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void initialize(MarkerFieldFilter filter) {
		super.initialize(filter);
		SeverityAndDescriptionFieldFilter sevFilter = (SeverityAndDescriptionFieldFilter) filter;

		severities = sevFilter.selectedSeverities;
		infoButton
				.setSelection((SeverityAndDescriptionFieldFilter.SEVERITY_INFO & severities) > 0);
		warningButton
				.setSelection((SeverityAndDescriptionFieldFilter.SEVERITY_WARNING & severities) > 0);
		errorButton
				.setSelection((SeverityAndDescriptionFieldFilter.SEVERITY_ERROR & severities) > 0);
	}

	/**
	 * Set or clear the flag for the constant based on the enablement.
	 * 
	 * @param constant
	 *            one of {@link IMarker#SEVERITY_ERROR},{@link IMarker#SEVERITY_WARNING},{@link IMarker#SEVERITY_INFO}
	 * @param enabled
	 */
	private void updateSeverities(int constant, boolean enabled) {
		if (enabled)
			severities = constant | severities;
		else
			severities = constant ^ severities;

	}

	/**
	 * Set the enabled state of the severity buttons.
	 * 
	 * @param enabled
	 */
	void setSeverityButtonsEnabled(boolean enabled) {
		label.setEnabled(enabled);
		errorButton.setEnabled(enabled);
		infoButton.setEnabled(enabled);
		warningButton.setEnabled(enabled);
	}

}
