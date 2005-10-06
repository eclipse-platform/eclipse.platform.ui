/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;

import org.eclipse.compare.internal.ICompareContextIds;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/* package */ class InputPatchPage extends WizardPage {

	// constants
	protected static final int SIZING_TEXT_FIELD_WIDTH= 250;
	protected static final int COMBO_HISTORY_LENGTH= 5;
	
	// dialog store id constants
	private final static String PAGE_NAME= "PatchWizardPage1"; //$NON-NLS-1$  
	private final static String STORE_PATCH_FILES_ID= PAGE_NAME+".PATCH_FILES"; //$NON-NLS-1$
	private final static String STORE_INPUT_METHOD_ID= PAGE_NAME+".INPUT_METHOD"; //$NON-NLS-1$
	//patch input constants
	protected final static int CLIPBOARD= 1;
	protected final static int FILE= 2;
	protected final static int WORKSPACE= 3;

	static final char SEPARATOR= System.getProperty("file.separator").charAt(0); //$NON-NLS-1$

	private boolean fShowError= false;
	
	// SWT widgets
	private Button fUseClipboardButton;

	private Combo fPatchFileNameField;
	private Button fPatchFileBrowseButton;
	private Button fUsePatchFileButton;

	private Button fUseWorkspaceButton;
	private TreeViewer fTreeViewer;

	private PatchWizard fPatchWizard;

	protected final static String INPUTPATCHPAGE_NAME= "InputPatchPage"; //$NON-NLS-1$

	InputPatchPage(PatchWizard pw) {
		super(INPUTPATCHPAGE_NAME, PatchMessages.InputPatchPage_title, null);
		fPatchWizard= pw;
		setMessage(PatchMessages.InputPatchPage_message); 
	}
	
	/*
	 * Get a path from the supplied text widget.
	 * @return org.eclipse.core.runtime.IPath
	 */
	protected IPath getPathFromText(Text textField) {
		return (new Path(textField.getText())).makeAbsolute();
	}

	/* package */ String getPatchName() {
		if (getInputMethod()==CLIPBOARD)
			return PatchMessages.InputPatchPage_Clipboard;
		return getPatchFilePath();
	}
	
	public void createControl(Composite parent) {
				
		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		buildPatchFileGroup(composite);

		restoreWidgetValues();

		updateWidgetEnablements();
		
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICompareContextIds.PATCH_INPUT_WIZARD_PAGE);
	}

	/**
	 * Returns the next page depending on what type of patch is being applied:
	 * 	 i) If the patch is a Workspace patch then it will proceed right to the PreviewPatchPage
	 *  ii) If the patch is a single project patch then it will proceed to the PatchTargetPage, which
	 *      allows the user to specify where to root the patch 
	 * @return PreviewPatchPage if multi-project patch, PatchTargetPage if single project patch
	 */
	public IWizardPage getNextPage() {

		WorkspacePatcher patcher= ((PatchWizard) getWizard()).getPatcher();

		String source= ""; //$NON-NLS-1$

		// Create a reader for the input
		Reader reader= null;
		try {
			int inputMethod= getInputMethod();
			if (inputMethod==CLIPBOARD) {
				Control c= getControl();
				if (c != null) {
					Clipboard clipboard= new Clipboard(c.getDisplay());
					Object o= clipboard.getContents(TextTransfer.getInstance());
					clipboard.dispose();
					if (o instanceof String)
						reader= new StringReader((String)o);
				}
				source= PatchMessages.InputPatchPage_Clipboard_title;
			} else if (inputMethod==FILE) {
				String patchFilePath= getPatchFilePath();
				if (patchFilePath != null) {
					try {
						reader= new FileReader(patchFilePath);
					} catch (FileNotFoundException ex) {
						MessageDialog.openError(null,
							PatchMessages.InputPatchPage_PatchErrorDialog_title,	
							PatchMessages.InputPatchPage_PatchFileNotFound_message); 
					}
				}
				source= PatchMessages.InputPatchPage_PatchFile_title;
			} else if (inputMethod==WORKSPACE) {
				//Get the selected patch file (tree will only allow for one selection)
				IResource[] resources= Utilities.getResources(fTreeViewer.getSelection());
				IResource patchFile= resources[0];
				if (patchFile!=null) {
					try {
						reader= new FileReader(patchFile.getRawLocation().toFile());
					} catch (FileNotFoundException ex) {
						MessageDialog.openError(null, PatchMessages.InputPatchPage_PatchErrorDialog_title, PatchMessages.InputPatchPage_PatchFileNotFound_message);
					} catch (NullPointerException nex) {
						//in case the path doesn't exist
						MessageDialog.openError(null, PatchMessages.InputPatchPage_PatchErrorDialog_title, PatchMessages.InputPatchPage_PatchFileNotFound_message);
					}
				}
				source= PatchMessages.InputPatchPage_WorkspacePatch_title;
			}
			
			// parse the input
			if (reader != null) {
				try {
					patcher.parse(new BufferedReader(reader));
				} catch (IOException ex) {
					MessageDialog.openError(null,
						PatchMessages.InputPatchPage_PatchErrorDialog_title, 
						PatchMessages.InputPatchPage_ParseError_message); 
				}
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException x) {
					// silently ignored
				}
			}
		}
		
		Diff[] diffs= patcher.getDiffs();
		if (diffs == null || diffs.length == 0) {
			String format= PatchMessages.InputPatchPage_NoDiffsFound_format;	
			String message= MessageFormat.format(format, new String[] { source });
			MessageDialog.openInformation(null,
				PatchMessages.InputPatchPage_PatchErrorDialog_title, message); 
			return this;
		}

		// guess prefix count
		int guess= 0; // guessPrefix(diffs);
		patcher.setStripPrefixSegments(guess);

		//If this is a workspace patch we don't need to set a target as the targets will be figured out from 
		//all of the projects that make up the patch and continue on to final preview page 
		//else go on to target selection page
		if (patcher.isWorkspacePatch()) {
			return fPatchWizard.getPage(PreviewPatchPage.PREVIEWPATCHPAGE_NAME);
		}

		return super.getNextPage();
	}
			
	/* (non-JavaDoc)
	 * Method declared in IWizardPage.
	 */
	public boolean canFlipToNextPage() {
		// we can't call getNextPage to determine if flipping is allowed since computing
		// the next page is quite expensive. So we say yes if the page is complete.
		return isPageComplete();
	}
	
	private void setEnablePatchFile(boolean enable) {
		fPatchFileNameField.setEnabled(enable);
		fPatchFileBrowseButton.setEnabled(enable);
	}

	private void setEnableWorkspacePatch(boolean enable) {
		fTreeViewer.getTree().setEnabled(enable);
	}

	/*
	 *	Create the group for selecting the patch file
	 */
	private void buildPatchFileGroup(Composite parent) {

		final Composite composite= new Composite(parent, SWT.NULL);
		GridLayout gridLayout= new GridLayout();
		gridLayout.numColumns= 3;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// 1st row
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan= 3;
		fUseClipboardButton= new Button(composite, SWT.RADIO);
		fUseClipboardButton.setText(PatchMessages.InputPatchPage_UseClipboardButton_text);
		fUseClipboardButton.setLayoutData(gd);

		// 2nd row
		fUsePatchFileButton= new Button(composite, SWT.RADIO);
		fUsePatchFileButton.setText(PatchMessages.InputPatchPage_FileButton_text);

		fPatchFileNameField= new Combo(composite, SWT.BORDER);
		//gd.horizontalIndent= 8;
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= SIZING_TEXT_FIELD_WIDTH;
		fPatchFileNameField.setLayoutData(gd);

		fPatchFileBrowseButton= new Button(composite, SWT.PUSH);
		fPatchFileBrowseButton.setText(PatchMessages.InputPatchPage_ChooseFileButton_text);
		fPatchFileBrowseButton.setLayoutData(new GridData());

		//3rd row
		fUseWorkspaceButton= new Button(composite, SWT.RADIO);
		fUseWorkspaceButton.setText(PatchMessages.InputPatchPage_UseWorkspaceButton_text);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		fUseWorkspaceButton.setLayoutData(gd);

		addWorkspaceControls(parent);

		// Add listeners
		fUsePatchFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fShowError= true;
				int state= getInputMethod();
				setEnablePatchFile(state==FILE);
				setEnableWorkspacePatch(state==WORKSPACE);
				updateWidgetEnablements();
			}
		});
		fPatchFileNameField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setSourceName(fPatchFileNameField.getText());
				updateWidgetEnablements();
			}
		});
		fPatchFileNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fShowError= true;
				updateWidgetEnablements();
			}
		});
		fPatchFileBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handlePatchFileBrowseButtonPressed();
				updateWidgetEnablements();
			}
		});
		fUseWorkspaceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fShowError= true;
				int state= getInputMethod();
				setEnablePatchFile(state==FILE);
				setEnableWorkspacePatch(state==WORKSPACE);
				updateWidgetEnablements();
			}
		});

		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateWidgetEnablements();
			}
		});
	}

	private void addWorkspaceControls(Composite composite) {

		new Label(composite, SWT.LEFT).setText(PatchMessages.InputPatchPage_WorkspaceSelectPatch_text);

		fTreeViewer= new TreeViewer(composite, SWT.BORDER);
		final GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint= 0;
		gd.heightHint= 0;
		fTreeViewer.getTree().setLayoutData(gd);

		fTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
		fTreeViewer.setContentProvider(new WorkbenchContentProvider());
		fTreeViewer.setSorter(new WorkbenchViewerSorter());
		fTreeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Updates the enable state of this page's controls.
	 */
	private void updateWidgetEnablements() {
		
		String error= null;

		boolean gotPatch= false;
		int inputMethod= getInputMethod();
		if (inputMethod==CLIPBOARD) {
			Control c= getControl();
			if (c != null) {
				Clipboard clipboard= new Clipboard(c.getDisplay());
				Object o= clipboard.getContents(TextTransfer.getInstance());
				clipboard.dispose();
				if (o instanceof String) {
					String s= ((String) o).trim();
					if (s.length() > 0)
						gotPatch= true;
					else
						error= PatchMessages.InputPatchPage_ClipboardIsEmpty_message; 
				} else
					error= PatchMessages.InputPatchPage_NoTextInClipboard_message;					 
			} else
				error= PatchMessages.InputPatchPage_CouldNotReadClipboard_message;
		} else if (inputMethod==FILE) {
			String path= fPatchFileNameField.getText();
			if (path != null && path.length() > 0) {
				File file= new File(path);
				gotPatch= file.exists() && file.isFile() && file.length() > 0;
				if (!gotPatch)
					error= PatchMessages.InputPatchPage_CannotLocatePatch_message + path; 
			} else {
				error= PatchMessages.InputPatchPage_NoFileName_message; 
			}
		} else if (inputMethod==WORKSPACE) {
			//Get the selected patch file (tree will only allow for one selection)
			IResource[] resources= Utilities.getResources(fTreeViewer.getSelection());
			if (resources!=null&&resources.length>0) {
				IResource patchFile= resources[0];
				if (patchFile!=null&&patchFile.getType()==IResource.FILE) {
					File actualFile= patchFile.getRawLocation().toFile();
					gotPatch= actualFile.exists()&&actualFile.isFile()&&actualFile.length()>0;
					if (!gotPatch)
						error= PatchMessages.InputPatchPage_FileSelectedNotPatch_message;
				}
			} else {
				error= PatchMessages.InputPatchPage_NoFileName_message;
			}
		}

		setPageComplete(gotPatch);

		if (fShowError)
			setErrorMessage(error);
	}
	
	protected void handlePatchFileBrowseButtonPressed() {
		FileDialog dialog= new FileDialog(getShell(), SWT.NONE);
		dialog.setText(PatchMessages.InputPatchPage_SelectPatchFileDialog_title);		 
		String patchFilePath= getPatchFilePath();
		if (patchFilePath != null) {
			int lastSegment= patchFilePath.lastIndexOf(SEPARATOR);
			if (lastSegment > 0) {
				patchFilePath= patchFilePath.substring(0, lastSegment);
			}
		}
		dialog.setFilterPath(patchFilePath);
		String res= dialog.open();
		if (res == null)
			return;
		
		patchFilePath= dialog.getFileName();
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

		int inputMethod= CLIPBOARD;

		IDialogSettings settings= getDialogSettings();
		if (settings != null) {

			try {
				inputMethod= settings.getInt(STORE_INPUT_METHOD_ID);
			} catch (NumberFormatException ex) {
				//OK - no value stored in settings; just use CLIPBOARD
			}

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
		}

		// set radio buttons state
		setInputButtonState(inputMethod);
	}
	
	/**
	 * 	Since Finish was pressed, write widget values to the dialog store so that they
	 *	will persist into the next invocation of this wizard page
	 */
	void saveWidgetValues() {
		IDialogSettings settings= getDialogSettings();
		if (settings != null) {

			settings.put(STORE_INPUT_METHOD_ID, getInputMethod());
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

	private void setInputButtonState(int state) {

		switch (state) {
			case CLIPBOARD :
				fUseClipboardButton.setSelection(true);
				break;

			case FILE :
				fUsePatchFileButton.setSelection(true);
				break;

			case WORKSPACE :
				fUsePatchFileButton.setSelection(true);
				break;
		}

		setEnablePatchFile(state==FILE);
		setEnableWorkspacePatch(state==WORKSPACE);
	}

	protected int getInputMethod() {
		if (fUseClipboardButton.getSelection())
			return CLIPBOARD;
		else if (fUsePatchFileButton.getSelection())
			return FILE;
		else
			return WORKSPACE;
	}

	private String getPatchFilePath() {
		if (fPatchFileNameField != null)
			return fPatchFileNameField.getText();
		return ""; //$NON-NLS-1$
	} 

	/*
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

