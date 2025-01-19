/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 * 	   Sebastian Davids <sdavids@gmx.de> - Fix for bug 90273 - [Dialogs]
 * 			ListSelectionDialog dialog alignment
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A standard dialog which solicits a list of selections from the user. This
 * class is configured with an arbitrary data model represented by content and
 * label provider objects. The <code>getResult</code> method returns the
 * selected elements.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * ListSelectionDialog dlg = new ListSelectionDialog(getShell(), input, new BaseWorkbenchContentProvider(),
 * 		new WorkbenchLabelProvider(), "Select the resources to save:");
 * dlg.setInitialSelections(dirtyEditors);
 * dlg.setTitle("Save Resources");
 * dlg.open();
 * </pre>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ListSelectionDialog extends SelectionDialog {
	// the root element to populate the viewer with
	private Object inputElement;

	// providers for populating this dialog
	private ILabelProvider labelProvider;

	private IStructuredContentProvider contentProvider;

	// the visual selection widget group
	CheckboxTableViewer listViewer;

	// optional checkbox
	private final String optionalCheckboxText;
	private boolean optionalCheckboxValue;
	private Button optionalCheckbox;

	// OK and Cancel buttons
	private final String okButtonText;
	private final String okButtonTextWhenNoSelection;
	private final boolean canCancel;

	// sizing constants
	private static final int SIZING_SELECTION_WIDGET_HEIGHT = 250;

	private static final int SIZING_SELECTION_WIDGET_WIDTH = 300;

	/**
	 * Creates a list selection dialog.
	 *
	 * @param parentShell     the parent shell
	 * @param input           the root element to populate this dialog with
	 * @param contentProvider the content provider for navigating the model
	 * @param labelProvider   the label provider for displaying model elements
	 * @param message         the message to be displayed at the top of this dialog,
	 *                        or <code>null</code> to display a default message
	 */
	public ListSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider, String message) {
		this(parentShell, input, contentProvider, labelProvider, message, null, null, true, null);
	}

	private ListSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider, String message, String okButtonText, String okButtonTextWhenNoSelection,
			boolean canCancel, String optionalCheckboxText) {
		super(parentShell);
		this.inputElement = input;
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;
		setMessage(message == null ? WorkbenchMessages.ListSelection_message : message);
		this.okButtonText = okButtonText;
		this.okButtonTextWhenNoSelection = okButtonTextWhenNoSelection;
		this.canCancel = canCancel;
		this.optionalCheckboxText = optionalCheckboxText;
	}

	/**
	 * Creates a new {@link ListSelectionDialog} {@link Builder} for the given
	 * input.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * ListSelectionDialog dialog = ListSelectionDialog.of(input).title(title).message(message).create(shell);
	 * </pre>
	 *
	 * @param input the root element to populate this dialog with
	 * @return a new {@link Builder} instance
	 * @since 3.123
	 */
	public static Builder of(Object input) {
		return new Builder(input);
	}

	/**
	 * The Builder to create {@link ListSelectionDialog} instances. It has a fluent
	 * API (every method returns the same builder instance).
	 *
	 * @see ListSelectionDialog#of(Object)
	 * @since 3.123
	 */
	public static final class Builder {

		private final Object input;
		private IStructuredContentProvider contentProvider;
		private ILabelProvider labelProvider;
		private Object[] initialSelections;
		private String title;
		private String message;
		private String okButtonText;
		private String okButtonTextWhenNoSelection;
		private boolean canCancel = true;
		private boolean asSheet = false;
		private String checkboxText;
		boolean checkboxValue = false;

		private Builder(Object input) {
			this.input = input;
		}

		/**
		 * Sets the content provider.
		 * <p>
		 * When this method is not called or when set to {@code null},
		 * {@link ArrayContentProvider} will be used.
		 *
		 * @param contentProvider the content provider for navigating the model
		 * @return this
		 */
		public Builder contentProvider(IStructuredContentProvider contentProvider) {
			this.contentProvider = contentProvider;
			return this;
		}

		/**
		 * Sets the label provider.
		 * <p>
		 * When this method is not called or when set to {@code null},
		 * {@link LabelProvider} will be used.
		 *
		 * @param labelProvider the label provider for displaying model elements
		 * @return this
		 */
		public Builder labelProvider(ILabelProvider labelProvider) {
			this.labelProvider = labelProvider;
			return this;
		}

		/**
		 * Sets the initial selection to the given elements.
		 * <p>
		 * When this method is not called, no element will be preselected.
		 *
		 * @param initialSelections the array of elements to preselect
		 * @return this
		 * @see SelectionDialog#setInitialSelections(Object...)
		 */
		public Builder preselect(Object... initialSelections) {
			this.initialSelections = initialSelections;
			return this;
		}

		/**
		 * Sets the title for this dialog.
		 * <p>
		 * When this method is not called or when set to {@code null},
		 * {@link WorkbenchMessages#ListSelection_title} will be used as dialog title.
		 *
		 * @param title the title
		 * @return this
		 * @see SelectionDialog#setTitle(String)
		 */
		public Builder title(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Sets the message.
		 * <p>
		 * When this method is not called or when set to {@code null}, a default message
		 * will shown.
		 *
		 * @param message the message to be displayed at the top of this dialog, or
		 *                {@code null} to display a default message
		 * @return this
		 */
		public Builder message(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Sets the OK button label.
		 *
		 * @param text the label of the OK button; can contain the placeholder
		 *             <code>{0}</code> for the number of currently selected elements
		 *             and the placeholder <code>{1}</code> for the total number of
		 *             elements
		 * @return this
		 * @see #okButtonTextWhenNoSelection(String)
		 */
		public Builder okButtonText(String text) {
			this.okButtonText = text;
			return this;
		}

		/**
		 * Sets the OK button label that will be displayed when no item is selected;
		 * otherwise the label of {@link #okButtonText(String)} will be displayed.
		 *
		 * @param text the label of the OK button when no item is selected which can
		 *             contain the placeholder <code>{0}</code> for the number of
		 *             currently selected elements and the placeholder <code>{1}</code>
		 *             for the total number of elements
		 * @return this
		 * @see #okButtonText(String)
		 */
		public Builder okButtonTextWhenNoSelection(String text) {
			this.okButtonTextWhenNoSelection = text;
			return this;
		}

		/**
		 * Sets whether the dialog can be canceled.
		 *
		 * @param canCancel whether selecting can be canceled or not (via the
		 *                  <i>Cancel</i> button or by closing the dialog)
		 * @return this
		 */
		public Builder canCancel(boolean canCancel) {
			this.canCancel = canCancel;
			return this;
		}

		/**
		 * Sets whether to show the dialog as sheet.
		 *
		 * @param asSheet whether to use {@link SWT#SHEET} (modal dialog that is
		 *                attached to a parent window)
		 * @return this
		 * @see SWT#SHEET
		 */
		public Builder asSheet(boolean asSheet) {
			this.asSheet = asSheet;
			return this;
		}

		/**
		 * Sets the label of the optional check box.
		 * <p>
		 * When this method is not called or when set to {@code null}, the optional
		 * check box will be hidden.
		 *
		 * @param checkboxText the check box label
		 * @return this
		 * @see Button#setText(String)
		 * @see #checkboxValue(boolean)
		 */
		public Builder checkboxText(String checkboxText) {
			this.checkboxText = checkboxText;
			return this;
		}

		/**
		 * Sets the selection state of the optional check box.
		 *
		 * @param checkboxValue the initial selection state
		 * @return this
		 * @see Button#setSelection(boolean)
		 * @see #checkboxText(String)
		 */
		public Builder checkboxValue(boolean checkboxValue) {
			this.checkboxValue = checkboxValue;
			return this;
		}

		/**
		 * Creates and assembles the dialog.
		 *
		 * @param parentShell the parent shell
		 * @return the new assembled {@link ListSelectionDialog}
		 */
		public ListSelectionDialog create(Shell parentShell) {
			ListSelectionDialog dialog = new ListSelectionDialog(parentShell, input,
					contentProvider == null ? ArrayContentProvider.getInstance() : contentProvider,
					labelProvider == null ? new LabelProvider() : labelProvider, message, okButtonText,
					okButtonTextWhenNoSelection, canCancel, checkboxText);
			int shellStyle = dialog.getShellStyle();
			if (!canCancel) {
				shellStyle &= ~SWT.CLOSE;
			}
			if (asSheet) {
				shellStyle |= SWT.SHEET;
			}
			dialog.setShellStyle(shellStyle);
			dialog.setTitle(title == null ? WorkbenchMessages.ListSelection_title : title);
			if (initialSelections != null) {
				dialog.setInitialSelections(initialSelections);
			}
			dialog.optionalCheckboxValue = checkboxValue;
			return dialog;
		}

	}

	/**
	 * Add the selection and deselection buttons to the dialog.
	 *
	 * @param composite org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));
		Button selectAllButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID,
				WorkbenchMessages.SelectionDialog_selectLabel, false);
		selectAllButton.setToolTipText(WorkbenchMessages.SelectionDialog_selectLabel);
		selectAllButton.addSelectionListener(widgetSelectedAdapter(e -> setAllChecked(true)));
		Button deselectAllButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID,
				WorkbenchMessages.SelectionDialog_deselectLabel,
				false);
		deselectAllButton.setToolTipText(WorkbenchMessages.SelectionDialog_deselectLabel);
		deselectAllButton.addSelectionListener(widgetSelectedAdapter(e -> setAllChecked(false)));
	}

	private void setAllChecked(boolean state) {
		listViewer.setAllChecked(state);
		updateButtonsOnSelection();
	}

	private void updateButtonsOnSelection() {
		long selectedCount = Arrays.stream(getViewer().getTable().getItems()).filter(TableItem::getChecked).count();
		int totalCount = getViewer().getTable().getItemCount();
		getButton(IDialogConstants.SELECT_ALL_ID).setEnabled(selectedCount < totalCount);
		getButton(IDialogConstants.DESELECT_ALL_ID).setEnabled(selectedCount > 0);
		if (okButtonText != null) {
			getButton(IDialogConstants.OK_ID).setText(
					NLS.bind(selectedCount == 0 && okButtonTextWhenNoSelection != null ? okButtonTextWhenNoSelection
							: okButtonText,
							selectedCount, totalCount));
		}
	}

	/**
	 * Visually checks the previously-specified elements in this dialog's list
	 * viewer.
	 */
	private void checkInitialSelections() {
		Iterator<?> itemsToCheck = getInitialElementSelections().iterator();

		while (itemsToCheck.hasNext()) {
			listViewer.setChecked(itemsToCheck.next(), true);
		}
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IWorkbenchHelpContextIds.LIST_SELECTION_DIALOG);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		updateButtonsOnSelection();
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);

		initializeDialogUnits(composite);

		createMessageArea(composite);

		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);

		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(contentProvider);
		listViewer.addCheckStateListener(e -> updateButtonsOnSelection());

		addSelectionButtons(composite);

		initializeViewer();

		// initialize page
		if (!getInitialElementSelections().isEmpty()) {
			checkInitialSelections();
		}

		// optional check box
		if (optionalCheckboxText != null) {
			optionalCheckbox = WidgetFactory.button(SWT.CHECK).text(optionalCheckboxText).layoutData(new GridData())
					.onSelect(e -> optionalCheckboxValue = optionalCheckbox.getSelection()).create(composite);
			optionalCheckbox.setSelection(optionalCheckboxValue);
		}

		Dialog.applyDialogFont(composite);
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		if (canCancel) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}
	}

	/**
	 * Returns the viewer used to show the list.
	 *
	 * @return the viewer, or <code>null</code> if not yet created
	 */
	protected CheckboxTableViewer getViewer() {
		return listViewer;
	}

	/**
	 * @return the current or, if the dialog has been closed, the last value of the
	 *         optional check box; {@code false} when there is no optional check box
	 *         and no default value has been set
	 * @since 3.123
	 */
	public boolean getCheckboxValue() {
		return optionalCheckboxValue;
	}

	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer() {
		listViewer.setInput(inputElement);
	}

	/**
	 * The <code>ListSelectionDialog</code> implementation of this
	 * <code>Dialog</code> method builds a list of the selected elements for later
	 * retrieval by the client and closes this dialog.
	 */
	@Override
	protected void okPressed() {

		// Get the input children.
		Object[] children = contentProvider.getElements(inputElement);

		// Build a list of selected children.
		if (children != null) {
			ArrayList<Object> list = new ArrayList<>();
			for (Object element : children) {
				if (listViewer.getChecked(element)) {
					list.add(element);
				}
			}
			setResult(list);
		}

		super.okPressed();
	}
}
