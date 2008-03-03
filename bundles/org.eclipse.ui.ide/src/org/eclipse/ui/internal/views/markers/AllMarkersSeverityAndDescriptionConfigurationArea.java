/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * {@link ProblemsSeverityAndDescriptionConfigurationArea} is the configuration
 * area for the all markers view.
 * 
 * @since 3.4
 * 
 */
public class AllMarkersSeverityAndDescriptionConfigurationArea extends
		SeverityAndDescriptionConfigurationArea {

	boolean filterOnSeverity;
	private Button enablementButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.DescriptionConfigurationArea#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createContents(Composite parent) {

		super.createContents(parent);

		Composite severityComposite = new Composite(parent, SWT.NONE);
		severityComposite.setLayout(new GridLayout(1, false));
		severityComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));

		enablementButton = new Button(severityComposite, SWT.CHECK);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		enablementButton.setLayoutData(data);
		enablementButton.setSelection(filterOnSeverity);
		enablementButton.setText(MarkerMessages.filtersDialog_filterOnSeverity);

		final Composite buttons = createSeverityGroup(severityComposite);
		GridData buttonData = new GridData();
		buttonData.horizontalIndent = IDialogConstants.INDENT;
		buttons.setLayoutData(buttonData);

		enablementButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				setFilterOnSeverity(enablementButton.getSelection());
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.SeverityAndDescriptionConfigurationArea#apply(org.eclipse.ui.views.markers.MarkerFieldFilter)
	 */
	public void apply(MarkerFieldFilter filter) {
		super.apply(filter);
		((AllMarkersSeverityAndDescriptionFieldFilter) filter)
				.setFilterOnSeverity(filterOnSeverity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.SeverityAndDescriptionConfigurationArea#initialize(org.eclipse.ui.views.markers.MarkerFieldFilter)
	 */
	public void initialize(MarkerFieldFilter filter) {
		super.initialize(filter);
		
		setFilterOnSeverity(((AllMarkersSeverityAndDescriptionFieldFilter) filter)
				.getFilterOnSeverity());
	}

	/**
	 * Set the value of filteringOnSeverity
	 * @param filtering
	 */
	private void setFilterOnSeverity(boolean filtering) {
		filterOnSeverity = filtering;
		enablementButton.setSelection(filtering);
		setSeverityButtonsEnabled(filterOnSeverity);
		
	}

}
