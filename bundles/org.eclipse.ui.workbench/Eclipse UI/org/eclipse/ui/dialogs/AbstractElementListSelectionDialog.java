/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *     font should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * An abstract class to select elements out of a list of elements.
 *
 * @since 2.0
 */
public abstract class AbstractElementListSelectionDialog extends SelectionStatusDialog {

	private ILabelProvider fRenderer;

	private boolean fIgnoreCase = true;

	private boolean fIsMultipleSelection = false;

	private boolean fMatchEmptyString = true;

	private boolean fAllowDuplicates = true;

	private Label fMessage;

	/**
	 * Allows direct access to the filtered list.
	 */
	protected FilteredList fFilteredList;

	private Text fFilterText;

	private ISelectionStatusValidator fValidator;

	private String fFilter = null;

	private String fEmptyListMessage = ""; //$NON-NLS-1$

	private String fEmptySelectionMessage = ""; //$NON-NLS-1$

	private int fWidth = 60;

	private int fHeight = 18;

	private Object[] fSelection = new Object[0];

	/**
	 * Constructs a list selection dialog.
	 *
	 * @param parent   The parent for the list.
	 * @param renderer ILabelProvider for the list
	 */
	protected AbstractElementListSelectionDialog(Shell parent, ILabelProvider renderer) {
		super(parent);
		fRenderer = renderer;
	}

	/**
	 * Handles default selection (double click). By default, the OK button is
	 * pressed.
	 */
	protected void handleDefaultSelected() {
		if (validateCurrentSelection()) {
			buttonPressed(IDialogConstants.OK_ID);
		}
	}

	/**
	 * Specifies if sorting, filtering and folding is case sensitive.
	 *
	 * @param ignoreCase true if case insensitive, false otherwise.
	 */
	public void setIgnoreCase(boolean ignoreCase) {
		fIgnoreCase = ignoreCase;
	}

	/**
	 * Returns if sorting, filtering and folding is case sensitive.
	 *
	 * @return boolean
	 */
	public boolean isCaseIgnored() {
		return fIgnoreCase;
	}

	/**
	 * Specifies whether everything or nothing should be filtered on empty filter
	 * string.
	 *
	 * @param matchEmptyString boolean
	 */
	public void setMatchEmptyString(boolean matchEmptyString) {
		fMatchEmptyString = matchEmptyString;
	}

	/**
	 * Specifies if multiple selection is allowed.
	 *
	 * @param multipleSelection true if multiple selections are allowed.
	 */
	public void setMultipleSelection(boolean multipleSelection) {
		fIsMultipleSelection = multipleSelection;
	}

	/**
	 * Specifies whether duplicate entries are displayed or not.
	 *
	 * @param allowDuplicates true if duplicate entries must be displayed.
	 */
	public void setAllowDuplicates(boolean allowDuplicates) {
		fAllowDuplicates = allowDuplicates;
	}

	/**
	 * Sets the list size in unit of characters.
	 *
	 * @param width  the width of the list.
	 * @param height the height of the list.
	 */
	public void setSize(int width, int height) {
		fWidth = width;
		fHeight = height;
	}

	/**
	 * Sets the message to be displayed if the list is empty.
	 *
	 * @param message the message to be displayed.
	 */
	public void setEmptyListMessage(String message) {
		fEmptyListMessage = message;
	}

	/**
	 * Sets the message to be displayed if the selection is empty.
	 *
	 * @param message the message to be displayed.
	 */
	public void setEmptySelectionMessage(String message) {
		fEmptySelectionMessage = message;
	}

	/**
	 * Sets an optional validator to check if the selection is valid. The validator
	 * is invoked whenever the selection changes.
	 *
	 * @param validator the validator to validate the selection.
	 */
	public void setValidator(ISelectionStatusValidator validator) {
		fValidator = validator;
	}

	/**
	 * Sets the elements of the list (widget).
	 *
	 * @param elements the elements of the list.
	 */
	protected void setListElements(Object[] elements) {
		Assert.isNotNull(fFilteredList);
		fFilteredList.setElements(elements);
		handleElementsChanged();
	}

	/**
	 * This method is called when the elements of the backing list are changed to
	 * refresh the standard dialog widgets.
	 *
	 * @since 3.8
	 */
	protected void handleElementsChanged() {
		boolean enabled = !fFilteredList.isEmpty();

		if (fMessage != null && !fMessage.isDisposed())
			fMessage.setEnabled(enabled);

		fFilteredList.setEnabled(enabled);
		updateOkState();
	}

	/**
	 * Sets the filter pattern.
	 *
	 * @param filter the filter pattern.
	 */
	public void setFilter(String filter) {
		if (fFilterText == null) {
			fFilter = filter;
		} else {
			fFilterText.setText(filter);
		}
	}

	/**
	 * Returns the current filter pattern.
	 *
	 * @return returns the current filter pattern or <code>null</code> if filter was
	 *         not set.
	 */
	public String getFilter() {
		if (fFilteredList == null) {
			return fFilter;
		}
		return fFilteredList.getFilter();
	}

	/**
	 * Returns the indices referring the current selection. To be called within
	 * open().
	 *
	 * @return returns the indices of the current selection.
	 */
	protected int[] getSelectionIndices() {
		Assert.isNotNull(fFilteredList);
		return fFilteredList.getSelectionIndices();
	}

	/**
	 * Returns an index referring the first current selection. To be called within
	 * open().
	 *
	 * @return returns the indices of the current selection.
	 */
	protected int getSelectionIndex() {
		Assert.isNotNull(fFilteredList);
		return fFilteredList.getSelectionIndex();
	}

	/**
	 * Sets the selection referenced by an array of elements. Empty or null array
	 * removes selection. To be called within open().
	 *
	 * @param selection the indices of the selection.
	 */
	protected void setSelection(Object[] selection) {
		Assert.isNotNull(fFilteredList);
		fFilteredList.setSelection(selection);
	}

	/**
	 * Returns an array of the currently selected elements. To be called within or
	 * after open().
	 *
	 * @return returns an array of the currently selected elements.
	 */
	protected Object[] getSelectedElements() {
		Assert.isNotNull(fFilteredList);
		return fFilteredList.getSelection();
	}

	/**
	 * Returns all elements which are folded together to one entry in the list.
	 *
	 * @param index the index selecting the entry in the list.
	 * @return returns an array of elements folded together.
	 */
	public Object[] getFoldedElements(int index) {
		Assert.isNotNull(fFilteredList);
		return fFilteredList.getFoldedElements(index);
	}

	/**
	 * Creates the message text widget and sets layout data.
	 *
	 * @param composite the parent composite of the message area.
	 */
	@Override
	protected Label createMessageArea(Composite composite) {
		Label label = super.createMessageArea(composite);

		GridData data = new GridData();
		data.grabExcessVerticalSpace = false;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		label.setLayoutData(data);

		fMessage = label;

		return label;
	}

	/**
	 * Handles a selection changed event. By default, the current selection is
	 * validated.
	 */
	protected void handleSelectionChanged() {
		validateCurrentSelection();
	}

	/**
	 * Validates the current selection and updates the status line accordingly.
	 *
	 * @return boolean <code>true</code> if the current selection is valid.
	 */
	protected boolean validateCurrentSelection() {
		Assert.isNotNull(fFilteredList);

		IStatus status;
		Object[] elements = getSelectedElements();

		if (elements.length > 0) {
			if (fValidator != null) {
				status = fValidator.validate(elements);
			} else {
				status = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, "", //$NON-NLS-1$
						null);
			}
		} else if (fFilteredList.isEmpty()) {
			status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, fEmptyListMessage, null);
		} else {
			status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, fEmptySelectionMessage, null);
		}

		updateStatus(status);

		return status.isOK();
	}

	@Override
	protected void cancelPressed() {
		setResult(null);
		super.cancelPressed();
	}

	/**
	 * Creates a filtered list.
	 *
	 * @param parent the parent composite.
	 * @return returns the filtered list widget.
	 */
	protected FilteredList createFilteredList(Composite parent) {
		int flags = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | (fIsMultipleSelection ? SWT.MULTI : SWT.SINGLE);

		FilteredList list = new FilteredList(parent, flags, fRenderer, fIgnoreCase, fAllowDuplicates,
				fMatchEmptyString);

		GridData data = new GridData();
		data.widthHint = convertWidthInCharsToPixels(fWidth);
		data.heightHint = convertHeightInCharsToPixels(fHeight);
		data.grabExcessVerticalSpace = true;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		list.setLayoutData(data);
		list.setFont(parent.getFont());
		list.setFilter((fFilter == null ? "" : fFilter)); //$NON-NLS-1$

		list.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handleDefaultSelected();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleWidgetSelected();
			}
		});

		fFilteredList = list;

		return list;
	}

	// 3515
	private void handleWidgetSelected() {
		Object[] newSelection = fFilteredList.getSelection();

		if (newSelection.length != fSelection.length) {
			fSelection = newSelection;
			handleSelectionChanged();
		} else {
			for (int i = 0; i != newSelection.length; i++) {
				if (!newSelection[i].equals(fSelection[i])) {
					fSelection = newSelection;
					handleSelectionChanged();
					break;
				}
			}
		}
	}

	/**
	 * Creates the filter text. Subclasses are allowed to override if they want the
	 * text to be created differently and must not call super in this case.
	 *
	 * @param parent the parent to create the widget on
	 * @return the created filter text widget.
	 */
	protected Text createFilterText(Composite parent) {
		Text text = new Text(parent, SWT.BORDER);

		GridData data = new GridData();
		data.grabExcessVerticalSpace = false;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		text.setLayoutData(data);
		text.setFont(parent.getFont());

		text.setText((fFilter == null ? "" : fFilter)); //$NON-NLS-1$

		Listener listener = e -> fFilteredList.setFilter(fFilterText.getText());
		text.addListener(SWT.Modify, listener);

		text.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					fFilteredList.setFocus();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});

		fFilterText = text;

		return text;
	}

	@Override
	public int open() {
		super.open();
		return getReturnCode();
	}

	private void access$superCreate() {
		super.create();
	}

	@Override
	public void create() {

		BusyIndicator.showWhile(null, () -> {
			access$superCreate();

			Assert.isNotNull(fFilteredList);

			if (fFilteredList.isEmpty()) {
				handleEmptyList();
			} else {
				validateCurrentSelection();
				fFilterText.selectAll();
				fFilterText.setFocus();
			}
		});

	}

	/**
	 * Handles empty list by disabling widgets.
	 */
	protected void handleEmptyList() {
		fMessage.setEnabled(false);
		fFilterText.setEnabled(false);
		fFilteredList.setEnabled(false);
		updateOkState();
	}

	/**
	 * Update the enablement of the OK button based on whether or not there is a
	 * selection.
	 */
	protected void updateOkState() {
		Button okButton = getOkButton();
		if (okButton != null) {
			okButton.setEnabled(getSelectedElements().length != 0);
		}
	}

	/**
	 * Gets the optional validator used to check if the selection is valid. The
	 * validator is invoked whenever the selection changes.
	 *
	 * @return the validator to validate the selection, or <code>null</code> if no
	 *         validator has been set.
	 *
	 * @since 3.5
	 */
	protected ISelectionStatusValidator getValidator() {
		return fValidator;
	}
}
