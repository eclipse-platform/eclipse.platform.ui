/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide.dialogs;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.LinkedResourcesPreferencePage;
import org.eclipse.ui.internal.ide.dialogs.RelativePathVariableGroup;


/**
 * Dialog to let the user customise how files and resources are created in a project
 * hierarchy after the user drag and drop items on a workspace container.
 * 
 * Files and folders can be created either by copying the source objects, creating
 * linked resources, and/or creating virtual folders.
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.6
 *
 */
public class ImportTypeDialog extends TrayDialog {

	/**
	 * Copy the files and folders to the destination
	 */
	public final static int IMPORT_COPY 			= 1;
	/**
	 * Import only files
	 */
	public final static int IMPORT_FILES_ONLY		= 16;
	/**
	 * Create linked resources for each file and folder
	 */
	public final static int IMPORT_LINK 			= 4;
	/**
	 * Move the files and folders to the destination
	 */
	public final static int IMPORT_MOVE 			= 8;
	/**
	 * Do not perform an import operation
	 */
	public final static int IMPORT_NONE 			= 0;
	/**
	 * Recreate the file and folder hierarchy using groups and links
	 */
	public final static int IMPORT_VIRTUAL_FOLDERS_AND_LINKS = 2;
	
	private Button copyButton = null;

	private int currentSelection;

	private Button linkButton = null;

	private Button moveButton = null;

	private int operationMask;
	private String preferredVariable;
	private IResource receivingResource = null;
	private Button shadowCopyButton = null;
	private String variable = null;
	
	private RelativePathVariableGroup relativePathVariableGroup;
	
	/**
	 * Creates the Import Type Dialog when resources are dragged and dropped from an Eclipse
	 * view.
	 * 
	 * @param shell
	 * 			the parent Shell
	 * @param dropOperation
	 * 		The dropOperation that was used by the user
	 * @param sources
	 * 		The list of resources that were dragged
	 * @param target
	 * 		The target container onto which the resources were dropped
	 */
	public ImportTypeDialog(Shell shell, int dropOperation,
			IResource[] sources, IContainer target) {
		this(shell, selectAppropriateMask(dropOperation, sources, target), RelativePathVariableGroup.getPreferredVariable(sources, target));
	}
	
	/**
	 * Creates the Import Type Dialog when files are dragged and dropped from the
	 * operating system's shell (Windows Explorer on Windows Platform, for example).
	 * 
	 * @param shell
	 * 			the parent Shell
	 * @param dropOperation
	 * 		The dropOperation that was used by the user
	 * @param names
	 * 		The list of files that were dragged
	 * @param target
	 * 		The target container onto which the files were dropped
	 */
	public ImportTypeDialog(Shell shell, int dropOperation, String[] names, IContainer target) {
		this(shell, selectAppropriateMask(dropOperation, names, target), RelativePathVariableGroup.getPreferredVariable(names, target));
	}
	
	/**
	 * @param parentShell
	 * @param operationMask
	 */
	private ImportTypeDialog(Shell parentShell, int operationMask, String preferredVariable) {
		super(parentShell);
		
		this.preferredVariable = preferredVariable;
		this.operationMask = operationMask;
		currentSelection = 0;
		String tmp = readContextPreference(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_TYPE);
		if (tmp.length() > 0)
			currentSelection = Integer.parseInt(tmp);
		currentSelection = currentSelection & operationMask;
		if (currentSelection == 0) {
			if (hasFlag(IMPORT_COPY))
				currentSelection = IMPORT_COPY;
			else
				currentSelection = IMPORT_MOVE;
		}

		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		if (store.getBoolean(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_RELATIVE))
			variable = preferredVariable;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#close()
	 */
	public boolean close() {
		return super.close();
	}
	
	/**
	 * Get the user selection from the dialog.
	 * @return The current selection (one of IMPORT_COPY, IMPORT_VIRTUAL_FOLDERS_AND_LINKS, IMPORT_LINK and IMPORT_MOVE)
	 */
	public int getSelection() {
		return currentSelection;
	}
	
	/**
	 * Get the selected variable if the selection is either IMPORT_VIRTUAL_FOLDERS_AND_LINKS or IMPORT_LINK
	 * @return The currently selected variable, or AUTOMATIC or ABSOLUTE_PATH
	 */
	public String getVariable() {
		return variable;
	}
	
	/** Set the project that is the destination of the import operation
	 * @param resource
	 */
	public void setResource(IResource resource) {
		receivingResource = resource;
	}
	
	private boolean hasFlag(int flag) {
		return (operationMask & flag) != 0;
	}

	// the format of the context is operationMask,value:operationMask,value:operationMask,value
	private String readContextPreference(String key) {
		String value = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getString(key);
		String [] keyPairs = value.split(":"); //$NON-NLS-1$
		for (int i = 0; i < keyPairs.length; i++) {
			String [] element = keyPairs[i].split(","); //$NON-NLS-1$
			if (element.length == 2) {
				if (element[0].equals(Integer.toString(operationMask)))
					return element[1];
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	private void refreshSelection() {
		if (copyButton != null)
			copyButton.setSelection(currentSelection == IMPORT_COPY);
		if (shadowCopyButton != null)
			shadowCopyButton.setSelection(currentSelection == IMPORT_VIRTUAL_FOLDERS_AND_LINKS);
		if (linkButton != null)
			linkButton.setSelection(currentSelection == IMPORT_LINK);
		if (moveButton != null)
			moveButton.setSelection(currentSelection == IMPORT_MOVE);
		if (relativePathVariableGroup != null) {
			relativePathVariableGroup.setEnabled((currentSelection & (IMPORT_VIRTUAL_FOLDERS_AND_LINKS | IMPORT_LINK)) != 0);
		}
	}

	private void writeContextPreference(String key, String value) {
		String oldValue = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getString(key);
		StringBuffer buffer = new StringBuffer();
		String [] keyPairs = oldValue.split(":"); //$NON-NLS-1$
		boolean found = false;
		for (int i = 0; i < keyPairs.length; i++) {
			if (i > 0)
				buffer.append(":"); //$NON-NLS-1$
			String [] element = keyPairs[i].split(","); //$NON-NLS-1$
			if (element.length == 2) {
				if (element[0].equals(Integer.toString(operationMask))) {
					buffer.append(element[0] + "," + value); //$NON-NLS-1$
					found = true;
				}
				else
					buffer.append(keyPairs[i]);
			}
		}
		if (!found) {
			if (buffer.length() > 0)
				buffer.append(":"); //$NON-NLS-1$
			buffer.append(Integer.toString(operationMask) + "," + value); //$NON-NLS-1$
		}
		String newValue = buffer.toString();
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(key, newValue);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			writeContextPreference(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_TYPE, Integer.toString(currentSelection));

			IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
			store.putValue(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_RELATIVE, Boolean.toString(variable != null));
		}
		super.buttonPressed(buttonId);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		String title = (operationMask & IMPORT_FILES_ONLY) != 0 ? IDEWorkbenchMessages.ImportTypeDialog_titleFilesOnly:
			IDEWorkbenchMessages.ImportTypeDialog_title;
		shell.setText(title);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.IMPORT_TYPE_DIALOG);
	}

	protected Control createDialogArea(Composite parent) {
		boolean linkIsOnlyChoice = hasFlag(IMPORT_LINK) && !(hasFlag(IMPORT_COPY | IMPORT_MOVE) || (hasFlag(IMPORT_VIRTUAL_FOLDERS_AND_LINKS) && !hasFlag(IMPORT_FILES_ONLY)));

		if (!linkIsOnlyChoice)
			createMessageArea(parent);
		Composite composite = new Composite(parent, 0);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		composite.setLayoutData(gridData);
 		composite.setFont(parent.getFont());

		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);

		int indent= linkIsOnlyChoice ? 0: convertWidthInCharsToPixels(3);

		layout.marginWidth += indent;
		composite.setLayout(layout);
		SelectionListener listener = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				currentSelection = ((Integer) e.widget.getData()).intValue();
				refreshSelection();
			}

			public void widgetSelected(SelectionEvent e) {
				currentSelection = ((Integer) e.widget.getData()).intValue();
				refreshSelection();
			}
		};
		
		if (hasFlag(IMPORT_COPY)) {
			copyButton = new Button(composite, SWT.RADIO);
			copyButton.setText(hasFlag(IMPORT_FILES_ONLY) ? IDEWorkbenchMessages.ImportTypeDialog_copyFiles: IDEWorkbenchMessages.ImportTypeDialog_copyFilesAndDirectories);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			copyButton.setLayoutData(gridData);
			copyButton.setData(new Integer(IMPORT_COPY));
			copyButton.addSelectionListener(listener);
			copyButton.setFont(parent.getFont());
		}
		
		if (hasFlag(IMPORT_MOVE)) {
			moveButton = new Button(composite, SWT.RADIO);
			moveButton.setText(hasFlag(IMPORT_FILES_ONLY) ? IDEWorkbenchMessages.ImportTypeDialog_moveFiles:IDEWorkbenchMessages.ImportTypeDialog_moveFilesAndDirectories);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			moveButton.setLayoutData(gridData);
			moveButton.setData(new Integer(IMPORT_MOVE));
			moveButton.addSelectionListener(listener);
			moveButton.setFont(parent.getFont());
		}

		if (hasFlag(IMPORT_LINK) && !linkIsOnlyChoice) {
			linkButton = new Button(composite, SWT.RADIO);
			linkButton.setText(hasFlag(IMPORT_FILES_ONLY) ? IDEWorkbenchMessages.ImportTypeDialog_linkFiles:IDEWorkbenchMessages.ImportTypeDialog_createLinks);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			linkButton.setLayoutData(gridData);
			linkButton.setData(new Integer(IMPORT_LINK));
			linkButton.addSelectionListener(listener);
			linkButton.setFont(parent.getFont());
		}

		if (hasFlag(IMPORT_VIRTUAL_FOLDERS_AND_LINKS) && !hasFlag(IMPORT_FILES_ONLY)) {
			shadowCopyButton = new Button(composite, SWT.RADIO);
			shadowCopyButton.setText(IDEWorkbenchMessages.ImportTypeDialog_recreateFilesAndDirectories);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			shadowCopyButton.setLayoutData(gridData);
			shadowCopyButton.setData(new Integer(IMPORT_VIRTUAL_FOLDERS_AND_LINKS));
			shadowCopyButton.addSelectionListener(listener);
			shadowCopyButton.setFont(parent.getFont());
		}

		if (hasFlag(IMPORT_VIRTUAL_FOLDERS_AND_LINKS | IMPORT_LINK)) {
			relativePathVariableGroup = new RelativePathVariableGroup(new RelativePathVariableGroup.IModel() {
				public IResource getResource() {
					return receivingResource;
				}
				public void setVariable(String string) {
					variable = string;
				}
				public String getVariable() {
					return variable;
				}
			});
			
			int groupIndent = 0;
			
			if (!linkIsOnlyChoice) {
				Button tmp = new Button(composite, SWT.CHECK);
				tmp.setText("."); //$NON-NLS-1$
				groupIndent = tmp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				tmp.dispose();
				
				Label tmpLabel = new Label(composite, SWT.NONE);
				tmpLabel.setText("."); //$NON-NLS-1$
				groupIndent -= tmpLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				tmpLabel.dispose();
			}
			
			Composite variableGroup = new Composite(composite, 0);
			gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.horizontalIndent = groupIndent;
			variableGroup.setLayoutData(gridData);
			variableGroup.setFont(parent.getFont());

			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth= 0;
			variableGroup.setLayout(layout);

			relativePathVariableGroup.createContents(variableGroup);
			relativePathVariableGroup.setSelection(variable != null);
			if (variable != null)
				relativePathVariableGroup.selectVariable(variable);
			else
				relativePathVariableGroup.selectVariable(preferredVariable);
		}
		
		if (linkIsOnlyChoice) {
			currentSelection = IMPORT_LINK;
			parent.getShell().setText(IDEWorkbenchMessages.ImportTypeDialog_titleFilesLinking);
		}
		createLinkControl(parent);
		refreshSelection();
		return composite;
	}

	private Control createLinkControl(Composite composite) {
		Link link= new Link(composite, SWT.WRAP | SWT.RIGHT);
		link.setText(IDEWorkbenchMessages.ImportTypeDialog_configureSettings);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openSettingsPage();
			}
		});
		GridData gridData= new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		link.setLayoutData(gridData);
		link.setFont(composite.getFont());

		return link;
	}

	protected void openSettingsPage() {
		String prefID = LinkedResourcesPreferencePage.PREF_ID;
		PreferencesUtil.createPreferenceDialogOn(getShell(), prefID, new String[] {prefID}, null).open();
	}

	protected Control createMessageArea(Composite parent) {
		Composite composite = new Composite(parent, 0);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		composite.setLayoutData(gridData);
		composite.setFont(parent.getFont());

		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginTop= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		composite.setLayout(layout);

		String message = (operationMask & IMPORT_FILES_ONLY) != 0 ? IDEWorkbenchMessages.ImportTypeDialog_questionFilesOnly:
			IDEWorkbenchMessages.ImportTypeDialog_question;

		// create message
		if (message != null) {
			Label messageLabel = new Label(composite, SWT.WRAP);
			messageLabel.setFont(parent.getFont());
			messageLabel.setText(message);
			gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			messageLabel.setLayoutData(gridData);
		}
		return composite;
	}
	
	/**
	 * @param resources
	 * 		The list of items that were dragged
	 * @return true if a set of paths are files only or a mix of files and folders, false otherwise
	 */
	private static boolean areOnlyFiles(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getType() != IResource.FILE)
				return false;
		}
		return true;
	}

	/**
	 * @param names
	 * 		The list of items that were dragged
	 * @return true if a set of paths are files only or a mix of files and folders, false otherwise
	 */
	private static boolean areOnlyFiles(String[] names) {
		for (int i = 0; i < names.length; i++) {
			File file = new File(names[i]);
			if (file.exists() && !file.isFile())
				return false;
		}
		return true;
	}

	/**
	 * Select the most appropriate mode that should be used for the dialog given
	 * the items dropped on the container, the container type, and the drop operation.
	 *
	 * @param dropOperation
	 * @param resources
	 * 		The list of items that were dragged
	 * @param target
	 * 		The target container onto which the items were dropped
	 * @return the appropriate import mask given the files dropped on the target
	 */
	private static int selectAppropriateMask(int dropOperation, IResource[] resources, IContainer target) {
		int mask = ImportTypeDialog.IMPORT_VIRTUAL_FOLDERS_AND_LINKS | ImportTypeDialog.IMPORT_LINK;
		if (!target.isVirtual() && (dropOperation != DND.DROP_LINK))
			mask |= ImportTypeDialog.IMPORT_COPY;
		if (areOnlyFiles(resources))
			mask |= ImportTypeDialog.IMPORT_FILES_ONLY;
		return mask;
	}
	
	/**
	 * Select the most appropriate mode that should be used for the dialog given
	 * the items dropped on the container, the container type, and the drop operation.
	 * 
	 * @param dropOperation
	 * @param names
	 * 		The list of items that were dragged
	 * @param target
	 * 		The target container onto which the items were dropped
	 * @return the appropriate import mask given the files dropped on the target
	 */
	private static int selectAppropriateMask(int dropOperation, String[] names, IContainer target) {
		int mask = ImportTypeDialog.IMPORT_VIRTUAL_FOLDERS_AND_LINKS | ImportTypeDialog.IMPORT_LINK;
		if (!target.isVirtual() && (dropOperation != DND.DROP_LINK))
			mask |= ImportTypeDialog.IMPORT_COPY;
		if (areOnlyFiles(names))
			mask |= ImportTypeDialog.IMPORT_FILES_ONLY;
		return mask;
	}
}
