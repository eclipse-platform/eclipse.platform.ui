/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.preferences;

import com.ibm.icu.text.MessageFormat;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.ant.core.IAntClasspathEntry;
import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.externaltools.internal.ui.TreeAndListGroup;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;

public class AddCustomDialog extends StatusDialog {
	
	private ZipFileStructureProvider providerCache;
	private IImportStructureProvider currentProvider;
	
	//A boolean to indicate if the user has typed anything
	private boolean entryChanged = false;

	private Combo sourceNameField;
	private List libraryEntries;
	private List existingNames;
	
	private String noNameErrorMsg;
	private String alreadyExistsErrorMsg;
	
	private TreeAndListGroup selectionGroup;
	
	private Text nameField;
	
	private String name= IAntCoreConstants.EMPTY_STRING;
	private IAntClasspathEntry library= null;
	private String className= IAntCoreConstants.EMPTY_STRING;
	
	private boolean editing= false;
	
	private String helpContext;

	/**
	 * Creates a new dialog with the given shell and title.
	 */
	public AddCustomDialog(Shell parent, List libraryEntries, List existingNames, String helpContext) {
		super(parent);
		this.libraryEntries = libraryEntries;
		this.existingNames= existingNames;
		this.helpContext= helpContext;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite topComposite= (Composite) super.createDialogArea(parent);
		topComposite.setSize(topComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite topGroup = new Composite(topComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		topGroup.setLayout(layout);
		topGroup.setFont(topComposite.getFont());
		topGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
		createNameGroup(topGroup);
		createRootDirectoryGroup(topGroup);
		createFileSelectionGroup(topComposite);
		
		if (library != null) {
			setSourceName(library.getLabel());
		}
		return topComposite;
	}
	
	private void createNameGroup(Composite topComposite) {		
		Label label = new Label(topComposite, SWT.NONE);
		label.setFont(topComposite.getFont());
		label.setText(AntPreferencesMessages.AddCustomDialog__Name__3);
		
		nameField = new Text(topComposite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
	
		nameField.setLayoutData(data);
		nameField.setFont(topComposite.getFont());
		nameField.setText(name);
		nameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateStatus();
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, helpContext);
	}
	
	/**
	 * Clears the cached structure provider after first finalizing
	 * it properly.
	 */
	private void clearProviderCache() {
		if (providerCache != null) {
			closeZipFile(providerCache.getZipFile());
			providerCache = null;
		}
	}
	
	/**
	 * Attempts to close the passed zip file, and answers a boolean indicating success.
	 */
	private boolean closeZipFile(ZipFile file) {
		try {
			file.close();
		} catch (IOException e) {
			AntUIPlugin.log(MessageFormat.format(AntPreferencesMessages.AddCustomDialog_Could_not_close_zip_file__0__4, new String[]{file.getName()}), e);
			return false;
		}

		return true;
	}

	/**
	 *	Create the group for creating the root directory
	 */
	private void createRootDirectoryGroup(Composite parent) {
		Label groupLabel = new Label(parent, SWT.NONE);
		groupLabel.setText(AntPreferencesMessages.AddCustomDialog__Location);
		groupLabel.setFont(parent.getFont());

		// source name entry field
		sourceNameField = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		sourceNameField.setLayoutData(data);
		sourceNameField.setFont(parent.getFont());
		
		sourceNameField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateFromSourceField();
			}
		});

		Iterator libraries= libraryEntries.iterator();
		while (libraries.hasNext()) {
			ClasspathEntry entry = (ClasspathEntry) libraries.next();
			sourceNameField.add(entry.getLabel());
		}
		
		sourceNameField.addKeyListener(new KeyAdapter() {
			/*
			 * @see KeyListener.keyPressed
			 */
			public void keyPressed(KeyEvent e) {
				//If there has been a key pressed then mark as dirty
				entryChanged = true;
			}
		});

		sourceNameField.addFocusListener(new FocusAdapter() {
			/*
			 * @see FocusListener.focusLost(FocusEvent)
			 */
			public void focusLost(FocusEvent e) {
				//Clear the flag to prevent constant update
				if (entryChanged) {
					entryChanged = false;
					updateFromSourceField();
				}
			}
		});
	}
	
	/**
	 * Update the receiver from the source name field.
	 */
	private void updateFromSourceField(){
		setSourceName(sourceNameField.getText());
		updateStatus();
	}
	
	
	/**
	 * Check the field values and display a message in the status if needed.
	 */
	private void updateStatus() {
		StatusInfo status= new StatusInfo();
		String customName= nameField.getText().trim();
		if (customName.length() == 0) {
			status.setError(noNameErrorMsg);
		} else if (!editing){
			Iterator names= existingNames.iterator();
			while (names.hasNext()) {
				String aName = (String) names.next();
				if(aName.equals(customName)) {
					status.setError(MessageFormat.format(alreadyExistsErrorMsg, new String[]{customName}));
					updateStatus(status);
					return;
				}
			}
		} 
		if (selectionGroup.getListTableSelection().isEmpty()) {
			status.setError(AntPreferencesMessages.AddCustomDialog_mustSelect);
		}
		updateStatus(status);
	}

	
	/**
	 * Sets the source name of the import to be the supplied path.
	 * Adds the name of the path to the list of items in the
	 * source combo and selects it.
	 *
	 * @param path the path to be added
	 */
	private void setSourceName(String path) {

		if (path.length() > 0) {

			String[] currentItems = this.sourceNameField.getItems();
			int selectionIndex = -1;
			for (int i = 0; i < currentItems.length; i++) {
				if (currentItems[i].equals(path)) {
					selectionIndex = i;
					break;
				}
			}
			if (selectionIndex < 0) {
				int oldLength = currentItems.length;
				String[] newItems = new String[oldLength + 1];
				System.arraycopy(currentItems, 0, newItems, 0, oldLength);
				newItems[oldLength] = path;
				this.sourceNameField.setItems(newItems);
				selectionIndex = oldLength;
			}
			this.sourceNameField.select(selectionIndex);

			resetSelection();
		}
	}

	/*
	* Create the file selection widget
	*/
	private void createFileSelectionGroup(Composite parent) {
		//Just create with a dummy root.
		FileSystemElement dummyRoot= new FileSystemElement("Dummy", null, true); //$NON-NLS-1$
		this.selectionGroup = new TreeAndListGroup(parent, dummyRoot, 
				getFolderProvider(), new WorkbenchLabelProvider(), getFileProvider(),
				new WorkbenchLabelProvider(), SWT.NONE, 400, 150, false);

		ISelectionChangedListener listener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateStatus();
			}
		};

		WorkbenchViewerComparator comparator = new WorkbenchViewerComparator();
		this.selectionGroup.setTreeComparator(comparator);
		this.selectionGroup.setListSorter(comparator);
		this.selectionGroup.addSelectionChangedListener(listener);
		selectionGroup.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (getButton(IDialogConstants.OK_ID).isEnabled()) {
					buttonPressed(IDialogConstants.OK_ID);
				}
			}
		});
	}
	
	/**
	 *	Returns whether the specified source currently exists
	 *	and is valid (ie.- proper format)
	 */
	protected boolean ensureSourceIsValid() {
		ZipFile specifiedFile = getSpecifiedSourceFile();

		if (specifiedFile == null){
			return false;
		}

		return closeZipFile(specifiedFile);
	}
	/**
	*	Answer the root FileSystemElement that represents the contents of the
	*	currently-specified .zip file.  If this FileSystemElement is not
	*	currently defined then create and return it.
	*/
	private MinimizedFileSystemElement getFileSystemTree() {
		IImportStructureProvider provider= null;
		MinimizedFileSystemElement element= null;
		ZipFile sourceFile = getSpecifiedSourceFile();
		if (sourceFile == null) {
			File file= new File(sourceNameField.getText());
			if (file.exists()) {
				provider = FileSystemStructureProvider.INSTANCE;
				element= selectFiles(file, provider);
			} 
		} else {
			//zip file set as location
			provider = getStructureProvider(sourceFile);
			element= selectFiles(((ZipFileStructureProvider)provider).getRoot(), provider);
		}
		this.currentProvider = provider;
		return element;
	}
	
	/**
	 * Invokes a file selection operation using the specified file system and
	 * structure provider.  If the user specifies files then this selection is
	 * cached for later retrieval and is returned.
	 */
	private MinimizedFileSystemElement selectFiles(final Object rootFileSystemObject, final IImportStructureProvider structureProvider) {

		final MinimizedFileSystemElement[] results = new MinimizedFileSystemElement[1];

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				//Create the root element from the supplied file system object
				results[0] = createRootElement(rootFileSystemObject, structureProvider);
			}
		});

		return results[0];
	}
	
	/**
	 * Creates and returns a <code>MinimizedFileSystemElement</code> if the specified
	 * file system object merits one.
	 */
	private MinimizedFileSystemElement createRootElement(Object fileSystemObject, IImportStructureProvider provider) {
		boolean isContainer = provider.isFolder(fileSystemObject);
		String elementLabel = provider.getLabel(fileSystemObject);

		// Use an empty label so that display of the element's full name
		// doesn't include a confusing label
		MinimizedFileSystemElement dummyParent =
			new MinimizedFileSystemElement(IAntCoreConstants.EMPTY_STRING, null, true);
		dummyParent.setPopulated();
		MinimizedFileSystemElement result =
			new MinimizedFileSystemElement(elementLabel, dummyParent, isContainer);
		result.setFileSystemObject(fileSystemObject);

		//Get the files for the element so as to build the first level
		result.getFiles(provider);

		return dummyParent;
	}
	
	/**
	 *	Answer a handle to the zip file currently specified as being the source.
	 *	Return <code>null</code> if this file does not exist or is not of valid format.
	 */
	private ZipFile getSpecifiedSourceFile() {
		try {
			String expanded = sourceNameField.getText();
			expanded= VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expanded);
			return new ZipFile(expanded);
		} catch (ZipException e) {
			StatusInfo status= new StatusInfo();
			status.setError(AntPreferencesMessages.AddCustomDialog_Bad_Format);
			updateStatus(status);
		} catch (IOException e) {
			StatusInfo status= new StatusInfo();
			status.setError(AntPreferencesMessages.AddCustomDialog_Unreadable);
			updateStatus(status);
		} catch (CoreException e) {
			StatusInfo status= new StatusInfo();
			status.setError(AntPreferencesMessages.AddCustomDialog_13);
			updateStatus(status);
		}

		sourceNameField.setFocus();
		return null;
	}
	/**
	 * Returns a structure provider for the specified zip file.
	 */
	private ZipFileStructureProvider getStructureProvider(ZipFile targetZip) {
		if (providerCache == null) {
			providerCache = new ZipFileStructureProvider(targetZip);
		} else if (!providerCache.getZipFile().getName().equals(targetZip.getName())) {
			clearProviderCache();
			// ie.- new value, so finalize & remove old value
			providerCache = new ZipFileStructureProvider(targetZip);
		} else if (!providerCache.getZipFile().equals(targetZip)) {
			closeZipFile(targetZip); // ie.- duplicate handle to same .zip
		}

		return providerCache;
	}

	/**
	 *	Repopulate the view based on the currently entered directory.
	 */
	private void resetSelection() {
		MinimizedFileSystemElement currentRoot = getFileSystemTree();
		selectionGroup.setRoot(currentRoot);
		
		if (className.length() != 0) {
			StringTokenizer tokenizer= new StringTokenizer(className, "."); //$NON-NLS-1$
			selectClass(currentRoot, tokenizer);
		}
	}
	
	private void selectClass(MinimizedFileSystemElement currentParent, StringTokenizer tokenizer) {
		if (!tokenizer.hasMoreTokens()) {
			return;
		}
		List folders= currentParent.getFolders(currentProvider);
		if (folders.size() == 1) {
			MinimizedFileSystemElement element = (MinimizedFileSystemElement)folders.get(0);
			if (element.getLabel(null).equals("/")) { //$NON-NLS-1$
				selectionGroup.selectAndRevealFolder(element);
				selectClass(element, tokenizer);
				return;
			}
		}
		String currentName= tokenizer.nextToken();
		if (tokenizer.hasMoreTokens()) {
			Iterator allFolders= folders.iterator();
			while (allFolders.hasNext()) {
				MinimizedFileSystemElement folder = (MinimizedFileSystemElement) allFolders.next();
				if (folder.getLabel(null).equals(currentName)) {
					selectionGroup.selectAndRevealFolder(folder);
					selectClass(folder, tokenizer);
					return;
				}
			}	
		} else {
			List files= currentParent.getFiles(currentProvider);
			Iterator iter= files.iterator();
			while (iter.hasNext()) {
				MinimizedFileSystemElement file = (MinimizedFileSystemElement) iter.next();
				if (file.getLabel(null).equals(currentName + ".class")) { //$NON-NLS-1$
					selectionGroup.selectAndRevealFile(file);
					return;
				}
			}
		}
	}

	/**
	 * Returns a content provider for <code>MinimizedFileSystemElement</code>s that returns
	 * only files as children.
	 */
	private ITreeContentProvider getFileProvider() {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement) {
					MinimizedFileSystemElement element = (MinimizedFileSystemElement) o;
					return element.getFiles(currentProvider).toArray();
				}
				return new Object[0];
			}
		};
	}

	/**
	 * Returns a content provider for <code>MinimizedFileSystemElement</code>s that returns
	 * only folders as children.
	 */
	private ITreeContentProvider getFolderProvider() {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement) {
					MinimizedFileSystemElement element = (MinimizedFileSystemElement) o;
					return element.getFolders(currentProvider).toArray();
				}
				return new Object[0];
			}
			public boolean hasChildren(Object o) {
				if (o instanceof MinimizedFileSystemElement) {
					MinimizedFileSystemElement element = (MinimizedFileSystemElement) o;
					if (element.isPopulated()) {
						return getChildren(element).length > 0;
					}
					
					//If we have not populated then wait until asked
					return true;
				}
				return false;
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		clearProviderCache();
		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		clearProviderCache();
		name= nameField.getText().trim();
		library= (ClasspathEntry)libraryEntries.get(sourceNameField.getSelectionIndex());
		IStructuredSelection selection= this.selectionGroup.getListTableSelection();
		MinimizedFileSystemElement element= (MinimizedFileSystemElement)selection.getFirstElement();
		if (element == null) {
			super.okPressed();
			return;
		}
		Object file= element.getFileSystemObject();
		if (file instanceof ZipEntry) {
			className= ((ZipEntry)file).getName();
		} else {
			className= ((File)file).getAbsolutePath();
			IPath classPath= new Path(className);
			IPath libraryPath= new Path(library.getEntryURL().getPath());
			int matching= classPath.matchingFirstSegments(libraryPath);
			classPath= classPath.removeFirstSegments(matching);
			classPath= classPath.setDevice(null);
			className= classPath.toString();
		}
		int index= className.lastIndexOf('.');
		className= className.substring(0, index);
		className= className.replace('/', '.'); 
		super.okPressed();
	}
	
	protected String getName() {
		return name;
	}
	
	protected void setName(String name) {
		this.name= name;
	} 
	
	protected void setLibraryEntry(IAntClasspathEntry library) {
		this.library= library;
		editing= true;
	}
	
	protected IAntClasspathEntry getLibraryEntry() {
		return this.library;
	}
	
	protected String getClassName() {
		return className;
	}
	
	protected void setClassName(String className) {
		this.className= className;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		getButton(IDialogConstants.OK_ID).setEnabled(!(library == null));
	}
	
	protected void setAlreadyExistsErrorMsg(String alreadyExistsErrorMsg) {
		this.alreadyExistsErrorMsg = alreadyExistsErrorMsg;
	}

	protected void setNoNameErrorMsg(String noNameErrorMsg) {
		this.noNameErrorMsg = noNameErrorMsg;
	}
}
