/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * ScopeArea is the filter configuration area that handles the scope of the
 * filter.
 * 
 * @since 3.4
 * 
 */
class ScopeArea extends GroupFilterConfigurationArea {

	private Button[] buttons;
	int scope;
	private WorkingSetArea workingSetArea;

	private class WorkingSetArea {

		Button button;
		Button selectButton;

		/**
		 * Creates the working set filter selection widgets.
		 * 
		 * @param parent
		 *            the parent composite of the working set widgets
		 */
		WorkingSetArea(Composite parent) {
			
			
			// radio button has to be part of main radio button group
			button = createRadioButton(parent,
					MarkerMessages.filtersDialog_noWorkingSet,
					MarkerFieldFilterGroup.ON_WORKING_SET);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			button.setLayoutData(data);

			// Composite composite = new Composite(parent, SWT.NONE);
			// composite.setFont(parent.getFont());
			// GridLayout layout = new GridLayout();
			// Button radio = new Button(parent, SWT.RADIO);
			// layout.marginWidth = radio.computeSize(SWT.DEFAULT,
			// SWT.DEFAULT).x;
			// layout.marginHeight = 0;
			// radio.dispose();
			// composite.setLayout(layout);
			selectButton = new Button(parent, SWT.PUSH);
			selectButton.setText(MarkerMessages.filtersDialog_workingSetSelect);
			
			initializeFontMetrics(parent);
			GridData layoutData = new GridData();
			layoutData.horizontalIndent = IDialogConstants.INDENT;
			int widthHint = Dialog.convertHorizontalDLUsToPixels(getFontMetrics(),IDialogConstants.BUTTON_WIDTH);
			Point minSize = selectButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			layoutData.widthHint = Math.max(widthHint, minSize.x);
			
			selectButton.setLayoutData(layoutData);
			selectButton.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {

					IWorkingSetSelectionDialog dialog = PlatformUI
							.getWorkbench().getWorkingSetManager()
							.createWorkingSetSelectionDialog(button.getShell(),
									false);
					IWorkingSet workingSet = getWorkingSet();

					if (workingSet != null) {
						dialog.setSelection(new IWorkingSet[] { workingSet });
					}
					if (dialog.open() == Window.OK) {
						IWorkingSet[] result = dialog.getSelection();
						if (result != null && result.length > 0) {
							setWorkingSet(result[0]);
						} else {
							setWorkingSet(null);
						}
						if (getSelection() == false) {
							setSelection(true);
						}
					}

				}
			});
			
			

		}

		/**
		 * Returns wether or not a working set filter should be used
		 * 
		 * @return true=a working set filter should be used false=a working set
		 *         filter should not be used
		 */
		boolean getSelection() {
			return button.getSelection();
		}

		/**
		 * Returns the selected working set filter or null if none is selected.
		 * 
		 * @return the selected working set filter or null if none is selected.
		 */
		IWorkingSet getWorkingSet() {
			return (IWorkingSet) button.getData();
		}

		/**
		 * Sets the working set filter selection.
		 * 
		 * @param selected
		 *            true=a working set filter should be used false=no working
		 *            set filter should be used
		 */
		void setSelection(boolean selected) {
			if (selected || (button.getSelection() && !selected)) {
				for (int i = 0; i < buttons.length; i++) {
					buttons[i].setSelection(false);
				}
				if (selected) {
					setScope(MarkerFieldFilterGroup.ON_WORKING_SET);
				}
			}
			if ((button.getSelection() && !selected)) {
				buttons[MarkerFieldFilterGroup.ON_ANY].setSelection(true);
				setScope(MarkerFieldFilterGroup.ON_ANY);
			}
			button.setSelection(selected);
		}

		/**
		 * Sets the specified working set.
		 * 
		 * @param workingSet
		 *            the working set
		 */
		void setWorkingSet(IWorkingSet workingSet) {
			button.setData(workingSet);
			if (workingSet != null) {
				button.setText(NLS.bind(
						MarkerMessages.filtersDialog_workingSet, workingSet
								.getLabel()));
			} else {
				button.setText(MarkerMessages.filtersDialog_noWorkingSet);
			}
		}

		/**
		 * Return the radio button for the receiver.
		 * @return Button
		 */
		Button getRadioButton() {
			return button;
		}

	}

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
		group.setWorkingSet(workingSetArea.getWorkingSet());
		
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
				parent, MarkerMessages.filtersDialog_anyResourceInSameProject,
				MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER);
		buttons[MarkerFieldFilterGroup.ON_SELECTED_ONLY] = createRadioButton(
				parent, MarkerMessages.filtersDialog_selectedResource,
				MarkerFieldFilterGroup.ON_SELECTED_ONLY);
		buttons[MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN] = createRadioButton(
				parent, MarkerMessages.filtersDialog_selectedAndChildren,
				MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN);
		workingSetArea = new WorkingSetArea(parent);
		buttons[MarkerFieldFilterGroup.ON_WORKING_SET] = workingSetArea.getRadioButton();
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

	/**
	 * Set the scope
	 * @param value
	 */
	private void setScope(int value){
		scope = value;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.FilterConfigurationArea#getTitle()
	 */
	public String getTitle() {
		return MarkerMessages.filtersDialog_scopeTitle;
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
		workingSetArea.setWorkingSet(group.getWorkingSet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#apply(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void apply(MarkerFieldFilter filter) {
		// Do nothing as this is a group level setting

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea#initialize(org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter)
	 */
	public void initialize(MarkerFieldFilter filter) {
		// Do nothing as this is a group level setting

	}

}
