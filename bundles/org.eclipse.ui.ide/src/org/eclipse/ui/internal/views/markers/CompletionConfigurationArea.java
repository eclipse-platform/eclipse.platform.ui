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
package org.eclipse.ui.internal.views.markers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.markers.FilterConfigurationArea;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * CompletionConfigurationField is the field for the configuration of filters
 * based on configurations.
 * 
 * @since 3.4
 * 
 */
public class CompletionConfigurationArea extends FilterConfigurationArea {

	private Button completeButton;
	private Button incompleteButton;
	int completionState;

	/**
	 * Create a new instance of the receiver.
	 */
	public CompletionConfigurationArea() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#apply(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void apply(MarkerFieldFilter filter) {
		((CompletionFieldFilter) filter).setCompletion(completionState);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createContents(Composite parent) {

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		completeButton = new Button(parent, SWT.CHECK);
		completeButton.setText(MarkerMessages.filtersDialog_statusComplete);
		completeButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updateCompletion(CompletionFieldFilter.COMPLETED,
						completeButton.getSelection());

			}
		});

		incompleteButton = new Button(parent, SWT.CHECK);
		incompleteButton.setText(MarkerMessages.filtersDialog_statusIncomplete);
		incompleteButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				updateCompletion(CompletionFieldFilter.NOT_COMPLETED,
						incompleteButton.getSelection());

			}
		});
	}

	/**
	 * Update the completion value based on the constant and the selection
	 * value.
	 * 
	 * @param constant
	 * @param enabled
	 */
	void updateCompletion(int constant, boolean enabled) {

		if (enabled)
			completionState = constant | completionState;
		else
			completionState = constant ^ completionState;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#initialize(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void initialize(MarkerFieldFilter filter) {
		completionState = ((CompletionFieldFilter) filter).getCompletion();

		completeButton
				.setSelection((CompletionFieldFilter.COMPLETED & completionState) > 0);
		incompleteButton
				.setSelection((CompletionFieldFilter.NOT_COMPLETED & completionState) > 0);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#getTitle()
	 */
	public String getTitle() {
		return MarkerMessages.filtersDialog_completionTitle;
	}

}
