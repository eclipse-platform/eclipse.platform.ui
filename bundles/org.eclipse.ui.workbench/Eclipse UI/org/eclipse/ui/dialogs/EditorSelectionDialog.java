/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
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
 */

public final class EditorSelectionDialog extends Dialog {
	private EditorDescriptor selectedEditor;

	/**
	 * The tab folder that holds the tabs for the listing of internal and
	 * external editors.
	 */
	private TabFolder editorsFolder;

	/**
	 * The table that renders the list of internal editors to the user.
	 */
	private Table internalEditorsTable;

	/**
	 * The table that renders the list of external editors to the user.
	 */
	private Table externalEditorsTable;

	/**
	 * The button for browsing to an external application for opening a file.
	 */
	private Button browseExternalEditorsButton;

	private Button okButton;

	private static final String STORE_ID_INTERNAL_EXTERNAL = "EditorSelectionDialog.STORE_ID_INTERNAL_EXTERNAL";//$NON-NLS-1$

	private String message = WorkbenchMessages.EditorSelection_chooseAnEditor;

	// collection of IEditorDescriptor
	private IEditorDescriptor[] externalEditors;

	private IEditorDescriptor[] internalEditors;

	private IEditorDescriptor[] editorsToFilter;

	private DialogListener listener = new DialogListener();

	private ResourceManager resourceManager;

	private TableViewer internalEditorsTreeViewer;

	private TableViewer externalEditorsTableViewer;

	private LabelProvider labelProvider;

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
	 * This method is called if the 'OK' button has been pressed.
	 */
	protected void okPressed() {
		saveWidgetValues();
		if (editorsFolder.getSelectionIndex() == 0) {
			selectedEditor = (EditorDescriptor) internalEditorsTable.getSelection()[0].getData();
		} else {
			selectedEditor = (EditorDescriptor) externalEditorsTable.getSelection()[0].getData();
		}
		super.okPressed();
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
		// create main group
		Composite contents = (Composite) super.createDialogArea(parent);

		// begin the layout
		Label textLabel = new Label(contents, SWT.NONE);
		textLabel.setText(message);

		editorsFolder = new TabFolder(contents, SWT.TOP);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = convertHorizontalDLUsToPixels(TABLE_WIDTH);
		editorsFolder.setLayoutData(data);

		labelProvider = new LabelProvider() {
			public String getText(Object element) {
				IEditorDescriptor d = (IEditorDescriptor) element;
				return TextProcessor.process(d.getLabel(), "."); //$NON-NLS-1$
			}

			public Image getImage(Object element) {
				IEditorDescriptor d = (IEditorDescriptor) element;
				return (Image) resourceManager.get(d.getImageDescriptor());
			}
		};

		createInternalEditorsTable();
		createExternalEditorsTable();

		restoreWidgetValues(); // Place buttons to the appropriate state

		internalEditorsTreeViewer.setInput(getInternalEditors());
		handleTabSelection();

		editorsFolder.addListener(SWT.Selection, listener);
		editorsFolder.addListener(SWT.DefaultSelection, listener);

		Dialog.applyDialogFont(contents);

		int tableHeight = externalEditorsTable.getItemHeight() * 12;
		((GridData) internalEditorsTreeViewer.getControl().getLayoutData()).heightHint = tableHeight;
		((GridData) externalEditorsTable.getLayoutData()).heightHint = tableHeight;

		return contents;
	}

	private void createInternalEditorsTable() {
		TabItem internalEditorsItem = new TabItem(editorsFolder, SWT.LEAD);
		internalEditorsItem.setText(WorkbenchMessages.EditorSelection_internal);

		Composite internalComposite = new Composite(editorsFolder, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		internalComposite.setLayout(layout);

		internalEditorsTreeViewer = new TableViewer(internalComposite, SWT.SINGLE | SWT.BORDER);
		internalEditorsTreeViewer.setContentProvider(ArrayContentProvider.getInstance());
		internalEditorsTreeViewer.setLabelProvider(labelProvider);
		internalEditorsTreeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				okPressed();
			}
		});

		internalEditorsTable = internalEditorsTreeViewer.getTable();
		internalEditorsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		internalEditorsTable.addListener(SWT.Selection, listener);
		internalEditorsTable.addListener(SWT.DefaultSelection, listener);

		internalEditorsItem.setControl(internalComposite);
	}

	private void createExternalEditorsTable() {
		Composite externalComposite = new Composite(editorsFolder, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		externalComposite.setLayout(layout);

		TabItem externalEditorsItem = new TabItem(editorsFolder, SWT.LEAD);
		externalEditorsItem.setText(WorkbenchMessages.EditorSelection_external);

		externalEditorsTableViewer = new TableViewer(externalComposite, SWT.SINGLE | SWT.BORDER);
		externalEditorsTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		externalEditorsTableViewer.setLabelProvider(labelProvider);
		externalEditorsTableViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				okPressed();
			}
		});

		externalEditorsTable = externalEditorsTableViewer.getTable();
		externalEditorsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		externalEditorsTable.addListener(SWT.Selection, listener);
		externalEditorsTable.addListener(SWT.DefaultSelection, listener);

		browseExternalEditorsButton = new Button(externalComposite, SWT.PUSH);
		GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, browseExternalEditorsButton
				.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		browseExternalEditorsButton.setLayoutData(data);
		browseExternalEditorsButton.setText(WorkbenchMessages.EditorSelection_browse);
		browseExternalEditorsButton.addListener(SWT.Selection, listener);

		externalEditorsItem.setControl(externalComposite);
	}

	private void handleTabSelection() {
		if (editorsFolder.getSelectionIndex() == 0) {
			internalEditorsTable.setFocus();
		} else {
			if (externalEditors == null) {
				externalEditorsTableViewer.setInput(getExternalEditors());
			}
			externalEditorsTable.setFocus();
		}
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
			TableItem ti = new TableItem(externalEditorsTable, SWT.NULL);
			ti.setData(editor);
			ti.setText(editor.getLabel());
			Image image = editor.getImageDescriptor().createImage();
			ti.setImage(image);

			externalEditorsTable.setSelection(ti);
			externalEditorsTable.showSelection();
			externalEditorsTable.setFocus();

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
	 * Use the dialog store to restore widget values to the values that they
	 * held last time this wizard was used to completion
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		boolean wasExternal = settings.getBoolean(STORE_ID_INTERNAL_EXTERNAL);
		editorsFolder.setSelection(wasExternal ? 1 : 0);
	}

	/**
	 * Since Finish was pressed, write widget values to the dialog store so that
	 * they will persist into the next invocation of this wizard page
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		// record whether use was viewing internal or external editors
		settings.put(STORE_ID_INTERNAL_EXTERNAL, editorsFolder.getSelectionIndex() == 1);
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

	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		// initially there is no selection so OK button should not be enabled
		okButton.setEnabled(false);

	}

	/**
	 * Update the button enablement state.
	 */
	protected void updateOkButton() {
		// Buttons are null during dialog creation
		if (okButton != null && !okButton.isDisposed()) {
			// only enable the 'OK' button if something was selected
			if (editorsFolder.getSelectionIndex() == 0) {
				okButton.setEnabled(internalEditorsTable.getSelectionCount() == 1);
			} else {
				okButton.setEnabled(externalEditorsTable.getSelectionCount() == 1);
			}
		}
	}

	private class DialogListener implements Listener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			if (event.widget == editorsFolder) {
				handleTabSelection();
			} else if (event.widget == browseExternalEditorsButton) {
				promptForExternalEditor();
			}
			updateOkButton();
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
