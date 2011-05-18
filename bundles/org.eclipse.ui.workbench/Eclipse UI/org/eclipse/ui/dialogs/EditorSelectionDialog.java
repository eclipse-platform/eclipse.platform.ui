/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla -	Bug 29633 [EditorMgmt] "Open" menu should
 *     						have Open With-->Other
 *     Helena Halperin - Bug 298747 [EditorMgmt] Bidi Incorrect file type direction in mirrored "Editor Selection" dialog
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;


/**
 * This class is used to allow the user to select a dialog from the set of
 * internal and external editors.
 * 
 * @since 3.3
 * @noextend This class is not intended to be subclassed by clients.
 */

public class EditorSelectionDialog extends Dialog {
	private EditorDescriptor selectedEditor;

	private EditorDescriptor hiddenSelectedEditor;

	private int hiddenTableTopIndex;

	private Button externalButton;

	private Table editorTable;

	private Button browseExternalEditorsButton;

	private Button internalButton;

	private Button okButton;

	/**
	 * For internal use only.
	 * 
	 * @noreference This field is not intended to be referenced by clients.
	 * @since 3.7
	 */
	protected static final String STORE_ID_INTERNAL_EXTERNAL = "EditorSelectionDialog.STORE_ID_INTERNAL_EXTERNAL";//$NON-NLS-1$

	private String message = WorkbenchMessages.EditorSelection_chooseAnEditor;

	// collection of IEditorDescriptor
	private IEditorDescriptor[] externalEditors;

	private IEditorDescriptor[] internalEditors;

	private IEditorDescriptor[] editorsToFilter;

	private DialogListener listener = new DialogListener();

	private ResourceManager resourceManager;

	private TableViewer editorTableViewer;

	private static final String[] Executable_Filters;

	private static final int TABLE_WIDTH = 200;
	static {
		if (Util.isWindows()) {
			Executable_Filters = new String[] { "*.exe", "*.bat", "*.*" };//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			Executable_Filters = new String[] { "*" }; //$NON-NLS-1$
		}
	}

	/**
	 * Create an instance of this class.
	 * 
	 * @param parentShell
	 *            the parent shell
	 */
	public EditorSelectionDialog(Shell parentShell) {
		super(parentShell);
		resourceManager = new LocalResourceManager(JFaceResources.getResources(parentShell
				.getDisplay()));
	}

	/**
	 * This method is called if a button has been pressed.
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			saveWidgetValues();
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Close the window.
	 */
	public boolean close() {
		boolean result = super.close();
		resourceManager.dispose();
		resourceManager = null;
		return result;
	}

	/*
	 * (non-Javadoc) Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(WorkbenchMessages.EditorSelection_title);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.EDITOR_SELECTION_DIALOG);
	}

	/**
	 * Creates and returns the contents of the upper part of the dialog (above
	 * the button bar).
	 * 
	 * Subclasses should overide.
	 * 
	 * @param parent
	 *            the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		Font font = parent.getFont();
		// create main group
		Composite contents = (Composite) super.createDialogArea(parent);
		((GridLayout) contents.getLayout()).numColumns = 2;

		// begin the layout
		Label textLabel = new Label(contents, SWT.WRAP);

		textLabel.setText(message);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.FILL;
		data.widthHint = TABLE_WIDTH;
		textLabel.setLayoutData(data);
		textLabel.setFont(font);

		internalButton = new Button(contents, SWT.RADIO | SWT.LEFT);
		internalButton.setText(WorkbenchMessages.EditorSelection_internal);
		internalButton.addListener(SWT.Selection, listener);
		data = new GridData();
		data.horizontalSpan = 1;
		internalButton.setLayoutData(data);
		internalButton.setFont(font);

		externalButton = new Button(contents, SWT.RADIO | SWT.LEFT);
		externalButton.setText(WorkbenchMessages.EditorSelection_external);
		externalButton.addListener(SWT.Selection, listener);
		data = new GridData();
		data.horizontalSpan = 1;
		externalButton.setLayoutData(data);
		externalButton.setFont(font);

		editorTable = new Table(contents, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(TABLE_WIDTH);
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		data.horizontalSpan = 2;
		editorTable.setLayoutData(data);
		editorTable.setFont(font);
		data.heightHint = editorTable.getItemHeight() * 12;
		editorTableViewer = new TableViewer(editorTable);
		editorTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		editorTableViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				IEditorDescriptor d = (IEditorDescriptor) element;
				return TextProcessor.process(d.getLabel(), "."); //$NON-NLS-1$
			}

			public Image getImage(Object element) {
				IEditorDescriptor d = (IEditorDescriptor) element;
				return (Image) resourceManager.get(d.getImageDescriptor());
			}
		});
		editorTable.addListener(SWT.Selection, listener);
		editorTable.addListener(SWT.DefaultSelection, listener);
		editorTable.addListener(SWT.MouseDoubleClick, listener);

		browseExternalEditorsButton = new Button(contents, SWT.PUSH);
		browseExternalEditorsButton
				.setText(WorkbenchMessages.EditorSelection_browse);
		browseExternalEditorsButton.addListener(SWT.Selection, listener);
		data = new GridData();
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, browseExternalEditorsButton
				.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		browseExternalEditorsButton.setLayoutData(data);
		browseExternalEditorsButton.setFont(font);

		restoreWidgetValues(); // Place buttons to the appropriate state

		fillEditorTable();

		updateEnableState();

		return contents;
	}

	protected void fillEditorTable() {
		EditorDescriptor newSelection = null;
		int newTopIndex = 0;

		boolean showInternal = internalButton.getSelection();
		boolean isShowingInternal = editorTableViewer.getInput() == getInternalEditors();
		if (showInternal != isShowingInternal) {
			newSelection = hiddenSelectedEditor;
			newTopIndex = hiddenTableTopIndex;
			if (editorTable.getSelectionIndex() != -1) {
				hiddenSelectedEditor = (EditorDescriptor) editorTable.getSelection()[0].getData();
				hiddenTableTopIndex = editorTable.getTopIndex();
			}
		}

		editorTableViewer.setInput(showInternal ? getInternalEditors() : getExternalEditors());

		if (newSelection != null) {
			editorTable.setTopIndex(newTopIndex);
			editorTableViewer.setSelection(new StructuredSelection(newSelection), true);
		} else {
			// set focus to first element, but don't select it:
			editorTable.setTopIndex(0);
			editorTable.setSelection(0);
			editorTable.deselectAll();
		}
		editorTable.setFocus();
	}

	/**
	 * Return the dialog store to cache values into
	 */

	protected IDialogSettings getDialogSettings() {
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings section = workbenchSettings
				.getSection("EditorSelectionDialog");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("EditorSelectionDialog");//$NON-NLS-1$
		}
		return section;
	}

	/**
	 * Get a list of registered programs from the OS
	 */
	protected IEditorDescriptor[] getExternalEditors() {
		if (externalEditors == null) {
			// Since this can take a while, show the busy
			// cursor. If the dialog is not yet visible,
			// then use the parent shell.
			Control shell = getShell();
			if (!shell.isVisible()) {
				Control topShell = shell.getParent();
				if (topShell != null) {
					shell = topShell;
				}
			}
			Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
			shell.setCursor(busy);
			// Get the external editors available
			EditorRegistry reg = (EditorRegistry) WorkbenchPlugin.getDefault()
					.getEditorRegistry();
			externalEditors = reg.getSortedEditorsFromOS();
			externalEditors = filterEditors(externalEditors);
			// Clean up
			shell.setCursor(null);
			busy.dispose();
		}
		return externalEditors;
	}

	/**
	 * Returns an array of editors which have been filtered according to the
	 * array of editors in the editorsToFilter instance variable.
	 * 
	 * @param editors
	 *            an array of editors to filter
	 * @return a filtered array of editors
	 */
	protected IEditorDescriptor[] filterEditors(IEditorDescriptor[] editors) {
		if ((editors == null) || (editors.length < 1)) {
			return editors;
		}

		if ((editorsToFilter == null) || (editorsToFilter.length < 1)) {
			return editors;
		}

		ArrayList filteredList = new ArrayList();
		for (int i = 0; i < editors.length; i++) {
			boolean add = true;
			for (int j = 0; j < editorsToFilter.length; j++) {
				if (editors[i].getId().equals(editorsToFilter[j].getId())) {
					add = false;
				}
			}
			if (add) {
				filteredList.add(editors[i]);
			}
		}

		return (IEditorDescriptor[]) filteredList
				.toArray(new IEditorDescriptor[filteredList.size()]);
	}

	/**
	 * Returns the internal editors
	 */
	protected IEditorDescriptor[] getInternalEditors() {
		if (internalEditors == null) {
			EditorRegistry reg = (EditorRegistry) WorkbenchPlugin.getDefault()
					.getEditorRegistry();
			internalEditors = reg.getSortedEditorsFromPlugins();
			internalEditors = filterEditors(internalEditors);
		}
		return internalEditors;
	}

	/**
	 * Return the editor the user selected
	 * 
	 * @return the selected editor
	 */
	public IEditorDescriptor getSelectedEditor() {
		return selectedEditor;
	}

	protected void promptForExternalEditor() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN
				| SWT.PRIMARY_MODAL | SWT.SHEET);
		dialog.setFilterExtensions(Executable_Filters);
		String result = dialog.open();
		if (result != null) {
			EditorDescriptor editor = EditorDescriptor.createForProgram(result);
			// pretend we had obtained it from the list of os registered editors
			TableItem ti = new TableItem(editorTable, SWT.NULL);
			ti.setData(editor);
			ti.setText(editor.getLabel());
			Image image = (Image) resourceManager.get(editor.getImageDescriptor());
			ti.setImage(image);

			// need to pass an array to setSelection -- 1FSKYVO: SWT:ALL -
			// inconsistent setSelection api on Table
			editorTable.setSelection(new TableItem[] { ti });
			editorTable.showSelection();
			editorTable.setFocus();
			selectedEditor = editor;

			/*
			 * add to our collection of cached external editors in case the user
			 * flips back and forth between internal/external
			 */
			IEditorDescriptor[] newEditors = new IEditorDescriptor[externalEditors.length + 1];
			System.arraycopy(externalEditors, 0, newEditors, 0,
					externalEditors.length);
			newEditors[newEditors.length - 1] = editor;
			externalEditors = newEditors;
		}
	}

	/**
	 * Handle a double click event on the list
	 */
	protected void handleDoubleClickEvent() {
		buttonPressed(IDialogConstants.OK_ID);
	}

	/**
	 * Use the dialog store to restore widget values to the values that they
	 * held last time this wizard was used to completion
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		boolean wasExternal = settings.getBoolean(STORE_ID_INTERNAL_EXTERNAL);
		internalButton.setSelection(!wasExternal);
		externalButton.setSelection(wasExternal);
	}

	/**
	 * Since Finish was pressed, write widget values to the dialog store so that
	 * they will persist into the next invocation of this wizard page
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		// record whether use was viewing internal or external editors
		settings
				.put(STORE_ID_INTERNAL_EXTERNAL, !internalButton.getSelection());
	}

	/**
	 * Set the message displayed by this message dialog
	 * 
	 * @param aMessage
	 *            the message
	 */
	public void setMessage(String aMessage) {
		message = aMessage;
	}

	/**
	 * Set the editors which will not appear in the dialog.
	 * 
	 * @param editors
	 *            an array of editors
	 */
	public void setEditorsToFilter(IEditorDescriptor[] editors) {
		editorsToFilter = editors;
	}

	/**
	 * Update enabled state.
	 */
	protected void updateEnableState() {
		boolean enableExternal = externalButton.getSelection();
		browseExternalEditorsButton.setEnabled(enableExternal);
		updateOkButton();
	}

	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// initially there is no selection so OK button should not be enabled
		okButton.setEnabled(false);

	}

	/**
	 * Update the button enablement state.
	 */
	protected void updateOkButton() {
		// Buttons are null during dialog creation
		if (okButton == null) {
			return;
		}
		// If there is no selection, do not enable OK button
		if (editorTable.getSelectionCount() == 0) {
			okButton.setEnabled(false);
			return;
		}
		// At this point, there is a selection
		okButton.setEnabled(selectedEditor != null);
	}

	private class DialogListener implements Listener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			if (event.type == SWT.MouseDoubleClick) {
				handleDoubleClickEvent();
				return;
			}
			if (event.widget == externalButton) {
				fillEditorTable();
			} else if (event.widget == browseExternalEditorsButton) {
				promptForExternalEditor();
			} else if (event.widget == editorTable) {
				if (editorTable.getSelectionIndex() != -1) {
					selectedEditor = (EditorDescriptor) editorTable
							.getSelection()[0].getData();
				} else {
					selectedEditor = null;
					okButton.setEnabled(false);
				}
			}
			updateEnableState();
		}

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 * @since 3.4
	 */
	protected boolean isResizable() {
		return true;
	}
}
