/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla -Bug 29633
 *     Helena Halperin - Bug 298747
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 378485, 460555, 463262
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;


/**
 * This class is used to allow the user to select a dialog from the set of
 * internal and external editors.
 *
 * @since 3.3
 * @noextend This class is not intended to be subclassed by clients.
 */

public class EditorSelectionDialog extends Dialog {

	private static class TreeArrayContentProvider implements ITreeContentProvider {

		private static final Object[] EMPTY = new Object[0];

		@Override
		public void dispose() {
			//
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			//
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement == null || !inputElement.getClass().isArray()) {
				return EMPTY;
			}
			// see bug 9262 why we can't return the same array
			Object[] orig = (Object[]) inputElement;
			Object[] arr = new Object[orig.length];
			System.arraycopy(orig, 0, arr, 0, arr.length);
			return arr;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return EMPTY;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

	}

	private IEditorDescriptor selectedEditor;

	private IEditorDescriptor hiddenSelectedEditor;

	private Button externalButton;

	private FilteredTree editorTable;

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

	private static final String STORE_ID_DESCR = "EditorSelectionDialog.STORE_ID_DESCR";//$NON-NLS-1$

	private static final String STORE_ID_FILE_EXTENSION = "EditorSelectionDialog.STORE_ID_FILE_EXTENSION";//$NON-NLS-1$

	private String message = WorkbenchMessages.EditorSelection_chooseAnEditor;

	// collection of IEditorDescriptor
	private IEditorDescriptor[] externalEditors;

	private IEditorDescriptor[] internalEditors;

	private IEditorDescriptor[] editorsToFilter;

	private DialogListener listener = new DialogListener();

	private ResourceManager resourceManager;

	private TreeViewer editorTableViewer;

	private String fileName;

	private Button rememberTypeButton;

	private Button rememberEditorButton;

	private static final String[] Executable_Filters;

	private static final int TABLE_WIDTH = 200;
	static {
		if (Util.isWindows()) {
			Executable_Filters = new String[] { "*.exe", "*.bat", "*.*" };//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (Util.isMac()) {
			Executable_Filters = new String[] { "*.app", "*" }; //$NON-NLS-1$ //$NON-NLS-2$
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
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			saveWidgetValues();
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Close the window.
	 */
	@Override
	public boolean close() {
		boolean result = super.close();
		resourceManager.dispose();
		resourceManager = null;
		return result;
	}

	@Override
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
	@Override
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

		Composite group = new Composite(contents, SWT.SHADOW_NONE);
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		data.horizontalSpan = 2;
		group.setLayout(new RowLayout(SWT.HORIZONTAL));
		group.setLayoutData(data);

		internalButton = new Button(group, SWT.RADIO | SWT.LEFT);
		internalButton.setText(WorkbenchMessages.EditorSelection_internal);
		internalButton.addListener(SWT.Selection, listener);
		internalButton.setFont(font);

		externalButton = new Button(group, SWT.RADIO | SWT.LEFT);
		externalButton.setText(WorkbenchMessages.EditorSelection_external);
		externalButton.addListener(SWT.Selection, listener);
		externalButton.setFont(font);

		editorTable = new FilteredTree(contents, SWT.SINGLE | SWT.BORDER, new PatternFilter(), true);
		editorTableViewer = editorTable.getViewer();
		Tree tree = editorTableViewer.getTree();
		tree.addListener(SWT.Selection, listener);
		tree.addListener(SWT.DefaultSelection, listener);
		tree.addListener(SWT.MouseDoubleClick, listener);
		data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(TABLE_WIDTH);
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		data.horizontalSpan = 2;
		editorTable.setLayoutData(data);
		editorTable.setFont(font);
		data.heightHint = tree.getItemHeight() * 12;
		editorTableViewer.setContentProvider(new TreeArrayContentProvider());
		editorTableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				IEditorDescriptor d = (IEditorDescriptor) element;
				return TextProcessor.process(d.getLabel(), "."); //$NON-NLS-1$
			}

			@Override
			public Image getImage(Object element) {
				IEditorDescriptor d = (IEditorDescriptor) element;
				return (Image) resourceManager.get(d.getImageDescriptor());
			}
		});

		browseExternalEditorsButton = new Button(contents, SWT.PUSH);
		browseExternalEditorsButton
				.setText(WorkbenchMessages.EditorSelection_browse);
		browseExternalEditorsButton.addListener(SWT.Selection, listener);
		data = new GridData();
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, browseExternalEditorsButton
				.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		data.horizontalSpan = 2;
		browseExternalEditorsButton.setLayoutData(data);
		browseExternalEditorsButton.setFont(font);

		if (fileName != null) {

			rememberEditorButton = new Button(contents, SWT.CHECK | SWT.LEFT);
			rememberEditorButton.setText(NLS.bind(WorkbenchMessages.EditorSelection_rememberEditor, fileName));
			rememberEditorButton.addListener(SWT.Selection, listener);
			data = new GridData();
			data.horizontalSpan = 2;
			rememberEditorButton.setLayoutData(data);
			rememberEditorButton.setFont(font);

			String fileType = getFileType();
			if (!fileType.isEmpty()) {
				rememberTypeButton = new Button(contents, SWT.CHECK | SWT.LEFT);
				rememberTypeButton.setText(NLS.bind(WorkbenchMessages.EditorSelection_rememberType, fileType));
				rememberTypeButton.addListener(SWT.Selection, listener);
				data = new GridData();
				data.horizontalSpan = 2;
				data.horizontalIndent = 15;
				rememberTypeButton.setLayoutData(data);
				rememberTypeButton.setFont(font);
				rememberTypeButton.setEnabled(false);
			}
		}

		initializeSuggestion();
		restoreWidgetValues(); // Place buttons to the appropriate state

		// Run async to restore selection on *visible* dialog - otherwise three won't scroll
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (editorTable.isDisposed()) {
					return;
				}
				fillEditorTable();
				updateEnableState();
			}
		});
	    return contents;
	}

	private String getFileType() {
		if (fileName == null) {
			return ""; //$NON-NLS-1$
		}
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot == -1 || lastDot >= fileName.length() - 1) {
			return ""; //$NON-NLS-1$
		}
		return fileName.substring(lastDot + 1, fileName.length());
	}

	protected void fillEditorTable() {
		IEditorDescriptor newSelection = selectedEditor;

		boolean showInternal = internalButton.getSelection();
		Object[] input = (Object[]) editorTableViewer.getInput();
		if (input != null) {
			// we are switching between external/internal editors
			boolean isShowingInternal = Arrays.equals(input, getInternalEditors());
			if (showInternal != isShowingInternal) {
				newSelection = hiddenSelectedEditor;
				if (!editorTableViewer.getSelection().isEmpty()) {
					hiddenSelectedEditor = (EditorDescriptor) editorTableViewer.getStructuredSelection()
							.getFirstElement();
				}
			}
		}

		editorTableViewer.setInput(showInternal ? getInternalEditors() : getExternalEditors());

		if (fileName != null && newSelection == null) {
			if (!showInternal) {
				newSelection = findBestExternalEditor();
			} else {
				newSelection = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(fileName);
			}
		}
		if (newSelection != null) {
			editorTableViewer.setSelection(new StructuredSelection(newSelection), true);
		}

		if (editorTableViewer.getSelection().isEmpty()) {
			// set focus to first element, but don't select it:
			Tree tree = editorTableViewer.getTree();
			if (tree.getItemCount() > 0) {
				tree.showItem(tree.getItem(0));
			}
		}
		editorTable.setFocus();
	}

	private static String getFileExtension(String fileName) {
		if (fileName == null) {
			return null;
		}
		int index = fileName.lastIndexOf('.');
		if (index != -1) {
			return fileName.substring(index);
		}
		return fileName;
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
			IProgressService ps = PlatformUI.getWorkbench().getService(IProgressService.class);
			// Since this can take a while, show the busy cursor.
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) {
					// Get the external editors available
					EditorRegistry reg = (EditorRegistry) WorkbenchPlugin.getDefault().getEditorRegistry();
					externalEditors = reg.getSortedEditorsFromOS();
					externalEditors = filterEditors(externalEditors);
				}
			};
			try {
				// See bug 47556 - Program.getPrograms() requires a Display.getCurrent() != null
				ps.runInUI(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), runnable, null);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				IStatus status;
				if (cause instanceof CoreException) {
					status = ((CoreException) cause).getStatus();
				} else {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
							"Error while retrieving native editors", cause); //$NON-NLS-1$
				}
				StatusManager.getManager().handle(status);
			} catch (InterruptedException e) {
				// Canceled by the user
			}

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

		List<IEditorDescriptor> filteredList = new ArrayList<>();
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

		return filteredList.toArray(new IEditorDescriptor[filteredList.size()]);
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

			/*
			 * add to our collection of cached external editors in case the user
			 * flips back and forth between internal/external
			 */
			IEditorDescriptor[] newEditors = new IEditorDescriptor[externalEditors.length + 1];
			System.arraycopy(externalEditors, 0, newEditors, 0,
					externalEditors.length);
			newEditors[newEditors.length - 1] = editor;
			externalEditors = newEditors;
			editorTableViewer.setInput(externalEditors);
			editorTableViewer.setSelection(new StructuredSelection(editor), true);
			editorTable.setFocus();
			selectedEditor = editor;
		}
	}

	/**
	 * Handle a double click event on the list
	 */
	protected void handleDoubleClickEvent() {
		buttonPressed(IDialogConstants.OK_ID);
	}

	private void initializeSuggestion() {
		if (fileName == null) {
			return;
		}
		IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor suggestion = editorRegistry.getDefaultEditor(fileName);
		if (suggestion != null && suggestion.isInternal()) {
			selectedEditor = suggestion;
		} else {
			selectedEditor = findBestExternalEditor();
		}
		boolean enableInternalList = selectedEditor == null || selectedEditor.isInternal();
		internalButton.setSelection(enableInternalList);
		externalButton.setSelection(!enableInternalList);
	}

	private IEditorDescriptor findBestExternalEditor() {
		if (fileName == null) {
			return null;
		}
		String extension = getFileExtension(fileName);
		Program program = Program.findProgram(extension);
		if (program != null) {
			for (IEditorDescriptor descriptor : getExternalEditors()) {
				if (descriptor instanceof EditorDescriptor
						&& program.equals(((EditorDescriptor) descriptor).getProgram())) {
					return descriptor;
				}
			}
		}
		return null;
	}

	/**
	 * Use the dialog store to restore widget values to the values that they
	 * held last time this wizard was used to completion, if the previous file
	 * has same extension.
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (fileName == null || selectedEditor == null
				|| getFileExtension(fileName).equals(settings.get(STORE_ID_FILE_EXTENSION))) {
			boolean wasExternal = settings.getBoolean(STORE_ID_INTERNAL_EXTERNAL);
			internalButton.setSelection(!wasExternal);
			externalButton.setSelection(wasExternal);
			String id = settings.get(STORE_ID_DESCR);
			if (id != null) {
				IEditorDescriptor[] editors;
				if (wasExternal) {
					editors = getExternalEditors();
				} else {
					editors = getInternalEditors();
				}
				for (IEditorDescriptor desc : editors) {
					if (id.equals(desc.getId())) {
						selectedEditor = desc;
					}
				}
			}
		}
	}

	/**
	 * Since Finish was pressed, write widget values to the dialog store so that
	 * they will persist into the next invocation of this wizard page
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		// record whether use was viewing internal or external editors
		settings.put(STORE_ID_FILE_EXTENSION, getFileExtension(fileName));
		settings.put(STORE_ID_INTERNAL_EXTERNAL, !internalButton.getSelection());
		settings.put(STORE_ID_DESCR, selectedEditor.getId());
		String editorId = selectedEditor.getId();
		settings.put(STORE_ID_DESCR, editorId);
		if (rememberEditorButton == null || !rememberEditorButton.getSelection()) {
			return;
		}
		EditorRegistry reg = (EditorRegistry) WorkbenchPlugin.getDefault().getEditorRegistry();
		if (rememberTypeButton == null || !rememberTypeButton.getSelection()) {
			updateFileMappings(reg, true);
			reg.setDefaultEditor(fileName, selectedEditor);
		} else {
			updateFileMappings(reg, false);
			reg.setDefaultEditor("*." + getFileType(), selectedEditor); //$NON-NLS-1$
		}
		// bug 468906: always re-set editor mappings: this is needed to rebuild
		// internal editors map after setting the default editor
		List<IFileEditorMapping> newMappings = new ArrayList<>();
		newMappings.addAll(Arrays.asList(reg.getFileEditorMappings()));
		reg.setFileEditorMappings(newMappings.toArray(new FileEditorMapping[newMappings.size()]));
		reg.saveAssociations();
	}

	/**
	 * Make sure EditorRegistry has editor mapping for the file name/type
	 */
	private void updateFileMappings(EditorRegistry reg, boolean useFileName) {
		IFileEditorMapping[] mappings = reg.getFileEditorMappings();
		boolean hasMapping = false;
		String fileType = getFileType();
		for (IFileEditorMapping mapping : mappings) {
			if (useFileName) {
				if (fileName.equals(mapping.getLabel())) {
					hasMapping = true;
					break;
				}
			} else {
				if (fileType.equals(mapping.getExtension())) {
					hasMapping = true;
					break;
				}

			}
		}
		if (hasMapping) {
			return;
		}
		FileEditorMapping mapping;
		if (useFileName) {
			mapping = new FileEditorMapping(fileName, null);
		} else {
			mapping = new FileEditorMapping(null, fileType);
		}
		List<IFileEditorMapping> newMappings = new ArrayList<>();
		newMappings.addAll(Arrays.asList(mappings));
		newMappings.add(mapping);
		FileEditorMapping[] array = newMappings.toArray(new FileEditorMapping[newMappings.size()]);
		reg.setFileEditorMappings(array);
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
	 * Set the file name which can be used to store the selected editor
	 * preference
	 *
	 * @param fileName
	 *            the file name
	 * @since 3.107
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
		if (rememberEditorButton != null && rememberTypeButton != null) {
			rememberTypeButton.setEnabled(rememberEditorButton.getSelection());
		}
		updateOkButton();
	}

	@Override
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
		if (editorTableViewer.getSelection().isEmpty()) {
			okButton.setEnabled(false);
			return;
		}
		// At this point, there is a selection
		okButton.setEnabled(selectedEditor != null);
	}

	private class DialogListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			if (event.type == SWT.MouseDoubleClick) {
				handleDoubleClickEvent();
				return;
			}
			if (event.widget == externalButton) {
				fillEditorTable();
			} else if (event.widget == browseExternalEditorsButton) {
				promptForExternalEditor();
			} else if (event.widget == editorTableViewer.getTree()) {
				if (!editorTableViewer.getSelection().isEmpty()) {
					selectedEditor = (EditorDescriptor) editorTableViewer.getStructuredSelection().getFirstElement();
				} else {
					selectedEditor = null;
					okButton.setEnabled(false);
				}
			}
			updateEnableState();
		}

	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
