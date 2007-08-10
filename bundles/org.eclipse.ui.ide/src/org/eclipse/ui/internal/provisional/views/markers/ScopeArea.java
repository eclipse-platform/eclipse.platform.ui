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

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * ScopeArea is the filter configuration area that handles the scope of the
 * filter.
 * @since 3.4
 *
 */
class ScopeArea extends FilterConfigurationArea {

	private Button[] buttons;
	int scope;

	/**
	 * Create a new instance of the receiver.
	 */
	public ScopeArea() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.FilterConfigurationArea#applyToGroup(org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilterGroup)
	 */
	public void applyToGroup(MarkerFieldFilterGroup group) {
		group.setScope(scope);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.FilterConfigurationArea#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createContents(Composite parent) {

		buttons = new Button[5];

		buttons[MarkerFieldFilterGroup.ON_ANY] = createRadioButton(parent,
				MarkerMessages.filtersDialog_anyResource,
				MarkerFieldFilterGroup.ON_ANY);
		buttons[MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER] = createRadioButton(
				parent,
				MarkerMessages.filtersDialog_anyResourceInSameProject,
				MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER);
		buttons[MarkerFieldFilterGroup.ON_SELECTED_ONLY] = createRadioButton(
				parent, MarkerMessages.filtersDialog_selectedResource,
				MarkerFieldFilterGroup.ON_SELECTED_ONLY);
		buttons[MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN] = createRadioButton(
				parent, MarkerMessages.filtersDialog_selectedAndChildren,
				MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN);
		buttons[MarkerFieldFilterGroup.ON_WORKING_SET] = createRadioButton(
				parent, MarkerMessages.filtersDialog_currentWorkingSet,
				MarkerFieldFilterGroup.ON_WORKING_SET);
	}

	/**
	 * Creates a radio button with the given parent and text.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param text
	 *            the text for the check box
	 * @return the radio box button
	 */
	protected Button createRadioButton(Composite parent, String text,
			final int value) {
		Button button = new Button(parent, SWT.RADIO);
		button.setText(text);
		button.setSelection(value == scope);
		button.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				scope = value;
			}
		});
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.FilterConfigurationArea#getTitle()
	 */
	public String getTitle() {
		return MarkerMessages.filtersDialog_scopeLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.FilterConfigurationArea#initializeFromGroup(org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilterGroup)
	 */
	public void initializeFromGroup(MarkerFieldFilterGroup group) {
		buttons[scope].setSelection(false);
		scope = group.getScope();
		buttons[scope].setSelection(true);
	}

}