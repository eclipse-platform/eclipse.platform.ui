/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.io.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.dnd.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.*;

import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.help.DialogPageContextComputer;


import org.eclipse.core.runtime.*;

import org.eclipse.compare.internal.Utilities;


/* package */ class InputPatchPage extends WizardPage {

	// constants
	protected static final int SIZING_TEXT_FIELD_WIDTH= 250;
	protected static final int COMBO_HISTORY_LENGTH= 5;

	// dialog store id constants
	private final static String PAGE_NAME= "PatchWizardPage"; //$NON-NLS-1$
	private final static String STORE_PATCH_FILE_ID= PAGE_NAME + ".PATCH_FILE";	//$NON-NLS-1$
	private final static String STORE_PATCH_FILES_ID= PAGE_NAME + ".PATCH_FILES";	//$NON-NLS-1$
	private final static String STORE_USE_CLIPBOARD_ID= PAGE_NAME + ".USE_CLIPBOARD";	//$NON-NLS-1$
	
	// help IDs
	private final static String PATCH_HELP_CONTEXT_ID= "PatchWizardHelpId";	
	
	// SWT widgets
	private Button fUseClipboardButton;
	private Combo fPatchFileNameField;
	private Button fPatchFileBrowseButton;
	private Button fUsePatchFileButton;
	private Composite fPatchFileGroup;
	private PatchWizard fPatchWizard;


	InputPatchPage(PatchWizard pw) {
		super("Select Patch Input", "Select Patch Input", null);
		fPatchWizard= pw;
		//setPageComplete(false);
	}
	
	/**
	 * Get a path from the supplied text widget.
	 * @return org.eclipse.core.runtime.IPath
	 */
	protected IPath getPathFromText(Text textField) {
		return (new Path(textField.getText())).makeAbsolute();
	}

	/* package */ String getPatchName() {
		if (getUseClipboard())
			return "Clipboard";
		return getPatchFilePath();
	}
	
	public void createControl(Composite parent) {
				
		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);
		
		fUsePatchFileButton= new Button(composite, SWT.RADIO);
		fUsePatchFileButton.setText("Select Patch File");
		
		fPatchFileGroup= createPatchFileGroup(composite);

		new Label(composite, SWT.NONE);	// a spacer

		fUseClipboardButton= new Button(composite, SWT.RADIO);
		fUseClipboardButton.setText("Extract Patch from Clipboard");
	
		// set up handlers
		
		fUsePatchFileButton.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Utilities.setEnableComposite(fPatchFileGroup, !getUseClipboard());
					updateWidgetEnablements();
				}
			}
		);
	
		restoreWidgetValues();

		updateWidgetEnablements();
		//updatePageCompletion();
		
		WorkbenchHelp.setHelp(composite, new DialogPageContextComputer(this, PATCH_HELP_CONTEXT_ID));								
	}
	
	/* (non-JavaDoc)
	 * Method declared in IWizardPage.
	 */
	public IWizardPage getNextPage() {
		if (true) {
			Diff[] diffs= null;
			Reader reader= null;
			if (getUseClipboard()) {
				Control c= getControl();
				if (c != null) {
					Clipboard clipboard= new Clipboard(c.getDisplay());
					Object o= clipboard.getContents(TextTransfer.getInstance());
					if (o instanceof String)
						reader= new StringReader((String)o);
				}
			} else {
				String patchFilePath= getPatchFilePath();
				if (patchFilePath != null) {
					try {
						reader= new FileReader(patchFilePath);
					} catch (FileNotFoundException ex) {
						MessageDialog.openError(null, "Error", "Patch file not found: " + patchFilePath);
					}
				}		
			}
			
			if (reader != null) {
				PatchParser pp= new PatchParser();
				try {
					diffs= pp.parse(new BufferedReader(reader));
				} catch (IOException ex) {
					MessageDialog.openError(null, "Error", "Error while parsing patch");
				}
				
				try {
					reader.close();
				} catch (IOException x) {
				}
			}
			
			if (diffs == null || diffs.length == 0) {
				MessageDialog.openError(null, "Error", "No diffs found in " + getPatchName());
				return this;
			}
			
			IWizard w= getWizard();
			if (w instanceof PatchWizard) {
				PatchWizard pw= (PatchWizard) w;
				pw.setDiffs(diffs);
			}

		}
		return super.getNextPage();
	}
		
	/* (non-JavaDoc)
	 * Method declared in IWizardPage.
	 */
	public boolean canFlipToNextPage() {
		if (true) {
			// we can't call getNextPage to determine if flipping is allowed since computing
			// the next page is quite expensive (checking preconditions and creating a
			// change). So we say yes if the page is complete.
			return isPageComplete();
		} else {
			return super.canFlipToNextPage();
		}
	}
	
	/**
	 *	Create the group for selecting the patch file
	 */
	private Composite createPatchFileGroup(Composite parent) {
		
		Composite sourceContainerGroup= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		sourceContainerGroup.setLayout(layout);
		sourceContainerGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	
		new Label(sourceContainerGroup, SWT.NONE).setText("Patch File:");
	
		// source name entry field
		fPatchFileNameField= new Combo(sourceContainerGroup, SWT.BORDER);
		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint= SIZING_TEXT_FIELD_WIDTH;
		fPatchFileNameField.setLayoutData(data);
		fPatchFileNameField.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setSourceName(fPatchFileNameField.getText());
					//Update enablements when this is selected
					updateWidgetEnablements();
				}
			}
		);
		fPatchFileNameField.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updateWidgetEnablements();
				}
			}
		);
	
		// patch file browse button
		fPatchFileBrowseButton= new Button(sourceContainerGroup, SWT.PUSH);
		fPatchFileBrowseButton.setText("Browse...");
		fPatchFileBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		//SWTUtil.setButtonDimensionHint(fDestinationBrowseButton);
		fPatchFileBrowseButton.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handlePatchFileBrowseButtonPressed();
					updateWidgetEnablements();
				}
			}
		);
	
		fPatchFileNameField.setFocus();
		
		return sourceContainerGroup;
	}

	/**
	 * Updates the enable state of this page's controls.
	 */
	protected void updateWidgetEnablements() {
		boolean enabled= false;
		String error= null;
		if (getUseClipboard()) {
			enabled= true;
		} else {
			String path= fPatchFileNameField.getText();
			if (path.length() > 0) {
				try {
					new FileReader(path);
					enabled= true;
				} catch (FileNotFoundException ex) {
					error= ex.getMessage();
				}
			}
		}	
		setPageComplete(enabled);
		setErrorMessage(error);
	}
	
	protected void handlePatchFileBrowseButtonPressed() {
		FileDialog dialog= new FileDialog(getShell(), SWT.NONE);
		dialog.setText("Select Patch File");		
		dialog.setFilterPath(getPatchFilePath());
		String res= dialog.open();
		if (res == null)
			return;
		
		String patchFilePath= dialog.getFileName();
		IPath filterPath= new Path(dialog.getFilterPath());
		IPath path= filterPath.append(patchFilePath).makeAbsolute();	
		patchFilePath= path.toOSString();
		//fDialogSettings.put(IUIConstants.DIALOGSTORE_LASTEXTJAR, filterPath.toOSString());
		
		fPatchFileNameField.setText(patchFilePath);
		//setSourceName(patchFilePath);
	}
	
	/**
	 * Sets the source name of the import to be the supplied path.
	 * Adds the name of the path to the list of items in the
	 * source combo and selects it.
	 *
	 * @param path the path to be added
	 */
	protected void setSourceName(String path) {
	
		if (path.length() > 0) {
	
			String[] currentItems= fPatchFileNameField.getItems();
			int selectionIndex= -1;
			for (int i= 0; i < currentItems.length; i++)
				if (currentItems[i].equals(path))
					selectionIndex= i;
			
			if (selectionIndex < 0) {	// not found in history
				int oldLength= currentItems.length;
				String[] newItems= new String[oldLength + 1];
				System.arraycopy(currentItems, 0, newItems, 0, oldLength);
				newItems[oldLength]= path;
				fPatchFileNameField.setItems(newItems);
				selectionIndex= oldLength;
			}
			fPatchFileNameField.select(selectionIndex);
	
			//resetSelection();
		}
	}
	
	/**
	 *	The Finish button was pressed. Try to do the required work now and answer
	 *	a boolean indicating success. If false is returned then the wizard will
	 *	not close.
	 *
	 * @return boolean
	 */
	public boolean finish() {
//		if (!ensureSourceIsValid())
//			return false;
	
		saveWidgetValues();
	
//		Iterator resourcesEnum = getSelectedResources().iterator();
//		List fileSystemObjects = new ArrayList();
//		while (resourcesEnum.hasNext()) {
//			fileSystemObjects.add(
//				((FileSystemElement) resourcesEnum.next()).getFileSystemObject());
//		}
//	
//		if (fileSystemObjects.size() > 0)
//			return importResources(fileSystemObjects);
//	
//		MessageDialog
//			.openInformation(
//				getContainer().getShell(),
//				DataTransferMessages.getString("DataTransfer.information"), //$NON-NLS-1$
//				DataTransferMessages.getString("FileImport.noneSelected")); //$NON-NLS-1$
//	
//		return false;

		return true;
	}
	
	/**
	 *	Use the dialog store to restore widget values to the values that they held
	 *	last time this wizard was used to completion
	 */
	private void restoreWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			
			// set 'Use Clipboard' radio button
			setUseClipboard(settings.getBoolean(STORE_USE_CLIPBOARD_ID));

			// set filenames history
			String[] sourceNames= settings.getArray(STORE_PATCH_FILES_ID);
			if (sourceNames != null)
				for (int i= 0; i < sourceNames.length; i++)
					if (sourceNames[i] != null && sourceNames[i].length() > 0)
						fPatchFileNameField.add(sourceNames[i]);
			
			// set patch file path
			String patchFilePath= settings.get(STORE_PATCH_FILES_ID);
			if (patchFilePath != null)
				setSourceName(patchFilePath);	
		} else
			System.out.println("restoreWidgetValues: no dialog settings");
	}
	
	/**
	 * 	Since Finish was pressed, write widget values to the dialog store so that they
	 *	will persist into the next invocation of this wizard page
	 */
	void saveWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {
			
			settings.put(STORE_USE_CLIPBOARD_ID, getUseClipboard());
			settings.put(STORE_PATCH_FILES_ID, getPatchFilePath());
			
			// update source names history
			String[] sourceNames= settings.getArray(STORE_PATCH_FILES_ID);
			if (sourceNames == null)
				sourceNames= new String[0];
	
			sourceNames= addToHistory(sourceNames, getPatchFilePath());
			settings.put(STORE_PATCH_FILES_ID, sourceNames);
		}
	}
	
	// static helpers
		
	private void setUseClipboard(boolean useClipboard) {
		if (useClipboard)
			fUseClipboardButton.setSelection(true);
		else
			fUsePatchFileButton.setSelection(true);
		Utilities.setEnableComposite(fPatchFileGroup, !useClipboard);
	}
	
	private boolean getUseClipboard() {
		if (fUseClipboardButton != null)
			return fUseClipboardButton.getSelection();
		return false;
	}

	private String getPatchFilePath() {
		if (fPatchFileNameField != null)
			return fPatchFileNameField.getText();
		return "";
	}

	/**
	 * Creates a new label with a bold font.
	 *
	 * @param parent the parent control
	 * @param text the label text
	 * @return the new label control
	 */
	private static Label buildPlainLabel(Composite parent, String text) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(text);
		GridData data= new GridData();
		data.verticalAlignment= GridData.FILL;
		data.horizontalAlignment= GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Adds an entry to a history, while taking care of duplicate history items
	 * and excessively long histories. The assumption is made that all histories
	 * should be of length <code>COMBO_HISTORY_LENGTH</code>.
	 *
	 * @param history the current history
	 * @param newEntry the entry to add to the history
	 */
	protected static String[] addToHistory(String[] history, String newEntry) {
		java.util.ArrayList l= new java.util.ArrayList(java.util.Arrays.asList(history));

		l.remove(newEntry);
		l.add(0,newEntry);
	
		// since only one new item was added, we can be over the limit
		// by at most one item
		if (l.size() > COMBO_HISTORY_LENGTH)
			l.remove(COMBO_HISTORY_LENGTH);
		
		return (String[]) l.toArray(new String[l.size()]);
	}
}

