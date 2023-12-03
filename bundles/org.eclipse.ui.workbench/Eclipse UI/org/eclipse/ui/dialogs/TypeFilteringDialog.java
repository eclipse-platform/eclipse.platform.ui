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
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *       activated and used by other components.
 *     Markus Schorn <markus.schorn@windriver.com> - Fix for bug 136591 -
 *       [Dialogs] TypeFilteringDialog appends unnecessary comma
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.registry.EditorRegistry;

/**
 * The TypeFilteringDialog is a SelectionDialog that allows the user to select a
 * file editor.
 */
public class TypeFilteringDialog extends SelectionDialog {
	Button addTypesButton;

	Collection initialSelections;

	// the visual selection widget group
	CheckboxTableViewer listViewer;

	// sizing constants
	private static final int SIZING_SELECTION_WIDGET_HEIGHT = 250;

	private static final int SIZING_SELECTION_WIDGET_WIDTH = 300;

	private static final String TYPE_DELIMITER = WorkbenchMessages.TypesFiltering_typeDelimiter;

	// Define a title for the filter entry field.
	private String filterTitle = WorkbenchMessages.TypesFiltering_otherExtensions;

	Text userDefinedText;

	IFileEditorMapping[] currentInput;

	/**
	 * Creates a type filtering dialog using the supplied entries. Set the initial
	 * selections to those whose extensions match the preselections.
	 *
	 * @param parentShell   The shell to parent the dialog from.
	 * @param preselections of String - a Collection of String to define the
	 *                      preselected types
	 */
	public TypeFilteringDialog(Shell parentShell, Collection preselections) {
		super(parentShell);
		setTitle(WorkbenchMessages.TypesFiltering_title);
		this.initialSelections = preselections;
		setMessage(WorkbenchMessages.TypesFiltering_message);
		setShellStyle(getShellStyle() | SWT.SHEET);
	}

	/**
	 * Creates a type filtering dialog using the supplied entries. Set the initial
	 * selections to those whose extensions match the preselections.
	 *
	 * @param parentShell   The shell to parent the dialog from.
	 * @param preselections of String - a Collection of String to define the
	 *                      preselected types
	 * @param filterText    - the title of the text entry field for other
	 *                      extensions.
	 */
	public TypeFilteringDialog(Shell parentShell, Collection preselections, String filterText) {
		this(parentShell, preselections);
		this.filterTitle = filterText;
	}

	/**
	 * Add the selection and deselection buttons to the dialog.
	 *
	 * @param composite org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);
		Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID,
				WorkbenchMessages.WizardTransferPage_selectAll, false);
		SelectionListener listener = widgetSelectedAdapter(e -> listViewer.setAllChecked(true));
		selectButton.addSelectionListener(listener);
		Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID,
				WorkbenchMessages.WizardTransferPage_deselectAll, false);
		listener = widgetSelectedAdapter(e -> listViewer.setAllChecked(false));
		deselectButton.addSelectionListener(listener);
	}

	/**
	 * Add the currently-specified extensions to result.
	 */
	private void addUserDefinedEntries(List<String> result) {
		StringTokenizer tokenizer = new StringTokenizer(userDefinedText.getText(), TYPE_DELIMITER);
		// Allow the *. and . prefix and strip out the extension
		while (tokenizer.hasMoreTokens()) {
			String currentExtension = tokenizer.nextToken().trim();
			if (!currentExtension.isEmpty()) {
				if (currentExtension.startsWith("*.")) { //$NON-NLS-1$
					result.add(currentExtension.substring(2));
				} else if (currentExtension.startsWith(".")) { //$NON-NLS-1$
					result.add(currentExtension.substring(1));
				} else {
					result.add(currentExtension);
				}
			}
		}
	}

	/**
	 * Visually checks the previously-specified elements in this dialog's list
	 * viewer.
	 */
	private void checkInitialSelections() {
		IFileEditorMapping editorMappings[] = ((EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry())
				.getUnifiedMappings();
		ArrayList<String> selectedMappings = new ArrayList<>();
		for (IFileEditorMapping mapping : editorMappings) {
			// Check for both extension and label matches
			if (this.initialSelections.contains(mapping.getExtension())) {
				listViewer.setChecked(mapping, true);
				selectedMappings.add(mapping.getExtension());
			} else if (this.initialSelections.contains(mapping.getLabel())) {
				listViewer.setChecked(mapping, true);
				selectedMappings.add(mapping.getLabel());
			}
		}
		// Now add in the ones not selected to the user defined list
		Iterator<String> initialIterator = this.initialSelections.iterator();
		StringBuilder entries = new StringBuilder();
		while (initialIterator.hasNext()) {
			String nextExtension = initialIterator.next();
			if (!selectedMappings.contains(nextExtension)) {
				if (entries.length() != 0) {
					entries.append(',');
				}
				entries.append(nextExtension);
			}
		}
		this.userDefinedText.setText(entries.toString());
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IWorkbenchHelpContextIds.TYPE_FILTERING_DIALOG);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
		createMessageArea(composite);
		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);
		listViewer.getTable().setFont(parent.getFont());
		listViewer.setLabelProvider(FileEditorMappingLabelProvider.INSTANCE);
		listViewer.setContentProvider(ArrayContentProvider.getInstance());
		listViewer.setComparator(new ViewerComparator());
		addSelectionButtons(composite);
		createUserEntryGroup(composite);
		initializeViewer();
		// initialize page
		if (this.initialSelections != null && !this.initialSelections.isEmpty()) {
			checkInitialSelections();
		}
		return composite;
	}

	/**
	 * Create the group that shows the user defined entries for the dialog.
	 *
	 * @param parent the parent this is being created in.
	 */
	private void createUserEntryGroup(Composite parent) {
		Font font = parent.getFont();
		// destination specification group
		Composite userDefinedGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		userDefinedGroup.setLayout(layout);
		userDefinedGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		Label fTitle = new Label(userDefinedGroup, SWT.NONE);
		fTitle.setFont(font);
		fTitle.setText(filterTitle);
		// user defined entry field
		userDefinedText = new Text(userDefinedGroup, SWT.SINGLE | SWT.BORDER);
		userDefinedText.setFont(font);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		userDefinedText.setLayoutData(data);
	}

	/**
	 * Return the input to the dialog.
	 *
	 * @return IFileEditorMapping[]
	 */
	private IFileEditorMapping[] getInput() {
		// Filter the mappings to be just those with a wildcard extension
		if (currentInput == null) {
			List<IFileEditorMapping> wildcardEditors = new ArrayList<>();
			IFileEditorMapping[] allMappings = ((EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry())
					.getUnifiedMappings();
			for (IFileEditorMapping allMapping : allMappings) {
				if (allMapping.getName().equals("*")) { //$NON-NLS-1$
					wildcardEditors.add(allMapping);
				}
			}
			currentInput = new IFileEditorMapping[wildcardEditors.size()];
			wildcardEditors.toArray(currentInput);
		}
		return currentInput;
	}

	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer() {
		listViewer.setInput(getInput());
	}

	/**
	 * The <code>TypeFilteringDialog</code> implementation of this
	 * <code>Dialog</code> method builds a list of the selected elements for later
	 * retrieval by the client and closes this dialog.
	 */
	@Override
	protected void okPressed() {
		// Get the input children.
		IFileEditorMapping[] children = getInput();
		List<String> list = new ArrayList<>();
		// Build a list of selected children.
		for (IFileEditorMapping element : children) {
			if (listViewer.getChecked(element)) {
				list.add(element.getExtension());
			}
		}
		addUserDefinedEntries(list);
		setResult(list);
		super.okPressed();
	}
}
