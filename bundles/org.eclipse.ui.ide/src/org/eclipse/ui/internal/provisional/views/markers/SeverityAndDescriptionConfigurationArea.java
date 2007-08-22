package org.eclipse.ui.internal.provisional.views.markers;

/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * SeverityAndDescriptionConfigurationArea is the configuration area for the
 * severity and description field.
 * 
 * @since 3.4
 * 
 */
public class SeverityAndDescriptionConfigurationArea extends
		FilterConfigurationArea {

	private Combo descriptionCombo;
	private Text descriptionText;
	private int severities;
	private Button infoButton;
	private Button errorButton;
	private Button warningButton;

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
		SeverityAndDescriptionFieldFilter sevFilter = (SeverityAndDescriptionFieldFilter) filter;
		sevFilter.setContainsModifier(descriptionCombo.getText());
		sevFilter.setContainsText(descriptionText.getText());
		sevFilter.selectedSeverities = severities;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.FilterConfigurationArea#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createContents(Composite parent) {

		createDescriptionGroup(parent);
		createSeverityGroup(parent);

	}

	/**
	 * Create the group for the description filter.
	 * 
	 * @param parent
	 */
	private void createDescriptionGroup(Composite parent) {

		Composite descriptionComposite = new Composite(parent, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout(3, false));
		descriptionComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText(MarkerMessages.filtersDialog_descriptionLabel);

		descriptionCombo = new Combo(descriptionComposite, SWT.READ_ONLY);
		descriptionCombo.add(SeverityAndDescriptionFieldFilter.CONTAINS);
		descriptionCombo
				.add(SeverityAndDescriptionFieldFilter.DOES_NOT_CONTAIN);

		// Prevent Esc and Return from closing the dialog when the combo is
		// active.
		descriptionCombo.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE
						|| e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
				}
			}
		});

		GC gc = new GC(descriptionComposite);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		descriptionText = new Text(descriptionComposite, SWT.SINGLE
				| SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics, 25);
		descriptionText.setLayoutData(data);
	}

	/**
	 * Create a group for the severity selection.
	 * 
	 * @param parent
	 */
	private void createSeverityGroup(Composite parent) {

		Composite severityComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = IDialogConstants.BUTTON_MARGIN;
		severityComposite.setLayout(layout);
		severityComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));

		Label label = new Label(severityComposite, SWT.NONE);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#initialize(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void initialize(MarkerFieldFilter filter) {
		SeverityAndDescriptionFieldFilter sevFilter = (SeverityAndDescriptionFieldFilter) filter;
		descriptionCombo.setText(sevFilter.getContainsModifier());
		descriptionText.setText(sevFilter.getContainsText());
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
	protected void updateSeverities(int constant, boolean enabled) {
		if (enabled)
			severities = constant | severities;
		else
			severities = constant ^ severities;

	}

}
