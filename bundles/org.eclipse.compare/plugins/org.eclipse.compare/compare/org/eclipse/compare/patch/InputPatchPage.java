/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.io.*;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;

import org.eclipse.ui.help.*;
import org.eclipse.ui.model.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.compare.internal.Utilities;


/* package */ class InputPatchPage extends WizardPage {

	// constants
	protected static final int SIZING_TEXT_FIELD_WIDTH= 250;
	protected static final int COMBO_HISTORY_LENGTH= 5;
	
	private final static int SIZING_SELECTION_WIDGET_WIDTH= 400;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT= 150;

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
	private Group fPatchFileGroup;
	private CheckboxTreeAndListGroup fInputGroup;
	private PatchWizard fPatchWizard;


	InputPatchPage(PatchWizard pw) {
		super("Select Patch Input", "Select Patch Input", null);
		fPatchWizard= pw;
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
		composite.setLayoutData(new GridData(/* GridData.VERTICAL_ALIGN_FILL | */ GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);
		
		Label l= new Label(composite, SWT.NONE);	// a spacer
		l.setText("Select the resources to patch:");
		buildInputGroup(composite);
		
		new Label(composite, SWT.NONE);	// a spacer		
		
		buildPatchFileGroup(composite);		
			
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
			
			Patcher patcher= ((PatchWizard) getWizard()).getPatcher();
			
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
				try {
					patcher.parse(new BufferedReader(reader));
				} catch (IOException ex) {
					MessageDialog.openError(null, "Error", "Error while parsing patch");
				}
				
				try {
					reader.close();
				} catch (IOException x) {
				}
			}
			
			Diff[] diffs= patcher.getDiffs();
			if (diffs == null || diffs.length == 0) {
				MessageDialog.openError(null, "Error", "No diffs found in " + getPatchName());
				return this;
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
	
	
	private void setEnablePatchFile(boolean enable) {
		fPatchFileNameField.setEnabled(enable);
		fPatchFileBrowseButton.setEnabled(enable);
	}

	public void buildPatchFileGroup(Composite parent) {
		
		fPatchFileGroup= new Group(parent, SWT.NONE);
		fPatchFileGroup.setText("Select Patch");
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		fPatchFileGroup.setLayout(layout);
		fPatchFileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// 1st row
		fUsePatchFileButton= new Button(fPatchFileGroup, SWT.RADIO);
		fUsePatchFileButton.setText("File: ");
		
		fPatchFileNameField= new Combo(fPatchFileGroup, SWT.BORDER);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent= 8;
		gd.widthHint= SIZING_TEXT_FIELD_WIDTH; // SWTUtil.convertWidthInCharsToPixels(30, fWorkingSetText);
		fPatchFileNameField.setLayoutData(gd);
		
		fPatchFileBrowseButton= new Button(fPatchFileGroup, SWT.PUSH);
		fPatchFileBrowseButton.setText("Choose...");
		fPatchFileBrowseButton.setLayoutData(new GridData());
		//SWTUtil.setButtonDimensionHint(fPatchFileBrowseButton);
		
		// 2nd row
		fUseClipboardButton= new Button(fPatchFileGroup, SWT.RADIO);
		fUseClipboardButton.setText("Clipboard");


		// Add listeners
		fUsePatchFileButton.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setEnablePatchFile(!getUseClipboard());
					updateWidgetEnablements();
				}
			}
		);
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
		fPatchFileBrowseButton.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handlePatchFileBrowseButtonPressed();
					updateWidgetEnablements();
				}
			}
		);
	}

	/**
	 *	Create the group for selecting the patch file
	 */
//	private void buildPatchFileGroup2(Composite parent) {
//		
//		fPatchFileGroup= new Composite(parent, SWT.NONE);
//		GridLayout layout= new GridLayout();
//		layout.numColumns= 3;
//		fPatchFileGroup.setLayout(layout);
//		fPatchFileGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
//	
//		fUsePatchFileButton= new Button(fPatchFileGroup, SWT.RADIO);
//		fUsePatchFileButton.setText("Patch File: ");
//	
//		// source name entry field
//		fPatchFileNameField= new Combo(fPatchFileGroup, SWT.BORDER);
//		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
//		data.widthHint= SIZING_TEXT_FIELD_WIDTH;
//		fPatchFileNameField.setLayoutData(data);
//		fPatchFileNameField.addSelectionListener(
//			new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					setSourceName(fPatchFileNameField.getText());
//					//Update enablements when this is selected
//					updateWidgetEnablements();
//				}
//			}
//		);
//		fPatchFileNameField.addModifyListener(
//			new ModifyListener() {
//				public void modifyText(ModifyEvent e) {
//					updateWidgetEnablements();
//				}
//			}
//		);
//	
//		// patch file browse button
//		fPatchFileBrowseButton= new Button(fPatchFileGroup, SWT.PUSH);
//		fPatchFileBrowseButton.setText("Browse...");
//		fPatchFileBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
//		//SWTUtil.setButtonDimensionHint(fDestinationBrowseButton);
//		fPatchFileBrowseButton.addSelectionListener(
//			new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					handlePatchFileBrowseButtonPressed();
//					updateWidgetEnablements();
//				}
//			}
//		);
//	
//		fPatchFileNameField.setFocus();
//	}

	protected void buildInputGroup(Composite parent) {
		
		IWorkspaceRoot root= null;
		IResource[] selection= null;
		PatchWizard pw= (PatchWizard) getWizard();
		ISelection s= pw.getSelection();
		if (s != null && !s.isEmpty()) {
			selection= Utilities.getResources(s);
			IWorkspace workspace= selection[0].getWorkspace();
			root= workspace.getRoot();
		}
				
		fInputGroup=
			new CheckboxTreeAndListGroup(
				parent,
				root,
				getResourceProvider(IResource.FOLDER | IResource.PROJECT | IResource.ROOT),
				new WorkbenchLabelProvider(),
				getResourceProvider(IResource.FILE),
				new WorkbenchLabelProvider(),
				SWT.NONE,
				SIZING_SELECTION_WIDGET_WIDTH,	
				SIZING_SELECTION_WIDGET_HEIGHT);
	
		WorkbenchViewerSorter sorter= new WorkbenchViewerSorter();
		fInputGroup.setTreeSorter(sorter);
		fInputGroup.setListSorter(sorter);
		
		for (int i= 0; i < selection.length; i++) {
			if (/* selection[i] instanceof ICompilationUnit || */ selection[i] instanceof IFile)
				fInputGroup.initialCheckListItem(selection[i]);
			else
				fInputGroup.initialCheckTreeItem(selection[i]);
		}
		
		MouseAdapter ma= new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				updateWidgetEnablements();
			}
		};
		fInputGroup.getTree().addMouseListener(ma);
		fInputGroup.getTable().addMouseListener(ma);
	}
		
	/**
	 * Returns a content provider for <code>IResource</code>s that returns 
	 * only children of the given resource type.
	 */
	private ITreeContentProvider getResourceProvider(final int resourceType) {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof IContainer) {
					try {
						ArrayList results= new ArrayList();
						IResource[] members= ((IContainer)o).members();
						for (int i= 0; i < members.length; i++)
							// filter out the desired resource types
							if ((members[i].getType() & resourceType) != 0)
								results.add(members[i]);
						return results.toArray();
					} catch (CoreException e) {
					}
				}
				// just return an empty set of children
				return new Object[0];
			}
		};
	}

	/**
	 * Updates the enable state of this page's controls.
	 */
	private void updateWidgetEnablements() {
		
		String error= null;

		boolean anySelected= fInputGroup.getCheckedElementCount() > 0;
		if (!anySelected)
			error= "nothing selected to apply patch to";

		boolean gotPatch= false;
		if (getUseClipboard()) {
			Control c= getControl();
			if (c != null) {
				Clipboard clipboard= new Clipboard(c.getDisplay());
				Object o= clipboard.getContents(TextTransfer.getInstance());
				if (o instanceof String) {
					String s= ((String) o).trim();
					if (s.length() > 0)
						gotPatch= true;
					else
						error= "clipboard is empty";
				} else
					error= "clipboard does not contain text";					
			} else
				error= "couldn't retrieve clipboard contents";					
		} else {
			String path= fPatchFileNameField.getText();
			if (path != null && path.length() > 0) {
				File file= new File(path);
				gotPatch= file.exists() && file.isFile() && file.length() > 0;
				if (!gotPatch)
					error= "can't locate path file";
			} else {
				error= "no file name";
			}
		}
		
		setPageComplete(anySelected && gotPatch);
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
		setEnablePatchFile(!useClipboard);
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

