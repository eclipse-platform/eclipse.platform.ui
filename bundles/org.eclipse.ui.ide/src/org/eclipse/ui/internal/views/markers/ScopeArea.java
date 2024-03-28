/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import static org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup.ON_ANY;
import static org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER;
import static org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN;
import static org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup.ON_SELECTED_ONLY;
import static org.eclipse.ui.internal.views.markers.MarkerFieldFilterGroup.ON_WORKING_SET;

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
 */
class ScopeArea extends GroupFilterConfigurationArea {

	/**
	 * Constant for any element button availability.
	 */
	static final int BUTTON_ON_ANY = 1;

	/**
	 * Constant for any element in same container button availability.
	 */
	static final int BUTTON_ON_ANY_IN_SAME_CONTAINER = 1 << 1;

	/**
	 * Constant for selected element and children button availability.
	 */
	static final int BUTTON_ON_SELECTED_AND_CHILDREN = 1 << 2;
	/**
	 * Constant for any selected element only button availability.
	 */
	static final int BUTTON_ON_SELECTED_ONLY = 1 << 3;
	/**
	 * Constant for on working set button availability.
	 */
	static final int BUTTON_ON_WORKING_SET = 1 << 4;

	/**
	 * Constant for all buttons availability.
	 */
	static final int ALL_BUTTONS_ENABLED = BUTTON_ON_ANY | BUTTON_ON_ANY_IN_SAME_CONTAINER
			| BUTTON_ON_SELECTED_AND_CHILDREN | BUTTON_ON_SELECTED_ONLY | BUTTON_ON_WORKING_SET;

	private Button[] buttons;
	private int buttonVisibleOptions = ALL_BUTTONS_ENABLED;
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

			selectButton = new Button(parent, SWT.PUSH);
			selectButton.setText(MarkerMessages.filtersDialog_workingSetSelect);

			initializeFontMetrics(parent);
			GridData layoutData = new GridData();
			layoutData.horizontalIndent = 20;
			int widthHint = Dialog.convertHorizontalDLUsToPixels(getFontMetrics(),IDialogConstants.BUTTON_WIDTH);
			Point minSize = selectButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			layoutData.widthHint = Math.max(widthHint, minSize.x);

			selectButton.setLayoutData(layoutData);
			selectButton.addSelectionListener(new SelectionAdapter() {
				@Override
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
				for (Button currentButton : buttons) {
					currentButton.setSelection(false);
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
	 *
	 * @param buttonVisibleOptions
	 */
	public ScopeArea(int buttonVisibleOptions) {
		super();
		this.buttonVisibleOptions = buttonVisibleOptions;
	}

	@Override
	public void applyToGroup(MarkerFieldFilterGroup group) {
		group.setScope(scope);
		if (workingSetArea != null) {
			group.setWorkingSet(workingSetArea.getWorkingSet());
		}

	}

	@Override
	public void createContents(Composite parent) {

		buttons = new Button[5];

		if ((buttonVisibleOptions & BUTTON_ON_ANY) != 0) {
			buttons[ON_ANY] = createRadioButton(parent, MarkerMessages.filtersDialog_anyResource, ON_ANY);
		}

		if ((buttonVisibleOptions & BUTTON_ON_ANY_IN_SAME_CONTAINER) != 0) {
			buttons[ON_ANY_IN_SAME_CONTAINER] = createRadioButton(parent,
					MarkerMessages.filtersDialog_anyResourceInSameProject, ON_ANY_IN_SAME_CONTAINER);
		}

		if ((buttonVisibleOptions & BUTTON_ON_SELECTED_ONLY) != 0) {

			buttons[ON_SELECTED_ONLY] = createRadioButton(parent, MarkerMessages.filtersDialog_selectedResource,
					ON_SELECTED_ONLY);
		}

		if ((buttonVisibleOptions & BUTTON_ON_SELECTED_AND_CHILDREN) != 0) {
			buttons[ON_SELECTED_AND_CHILDREN] = createRadioButton(parent,
					MarkerMessages.filtersDialog_selectedAndChildren, ON_SELECTED_AND_CHILDREN);
		}

		if ((buttonVisibleOptions & BUTTON_ON_WORKING_SET) != 0) {
			workingSetArea = new WorkingSetArea(parent);
			buttons[ON_WORKING_SET] = workingSetArea.getRadioButton();
		}
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

			@Override
			public void widgetSelected(SelectionEvent e) {
				scope = value;
			}
		});
		return button;
	}

	/**
	 * Set the scope
	 */
	private void setScope(int value){
		scope = value;
	}
	@Override
	public String getTitle() {
		return MarkerMessages.filtersDialog_scopeTitle;
	}

	@Override
	public void initializeFromGroup(MarkerFieldFilterGroup group) {
		if (buttons[scope] != null)
			buttons[scope].setSelection(false);

		scope = group.getScope();

		if (buttons[scope] == null)
			throw new IllegalArgumentException("Requested scope button not available for configuration."); //$NON-NLS-1$

		buttons[scope].setSelection(true);

		if (workingSetArea != null)
			workingSetArea.setWorkingSet(group.getWorkingSet());
	}

	@Override
	public void apply(MarkerFieldFilter filter) {
		// Do nothing as this is a group level setting

	}

	@Override
	public void initialize(MarkerFieldFilter filter) {
		// Do nothing as this is a group level setting

	}

}
