/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editorsupport.win32;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IInPlaceEditor;
import org.eclipse.ui.IInPlaceEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.part.EditorPart;

/**
 * The editor part responsible for an in-place editor on this platform.
 */
public class OleEditor extends EditorPart implements IInPlaceEditor {

	private OleFrame clientFrame;
	private OleClientSite clientSite;
	private IInPlaceEditorInput inPlaceInput;
	private File source;
	private Image oleTitleImage;
	// This flag makes sure that the receiver is not dirty when shutting down
	boolean sourceDeleted = false;
	// This flag indicates whether or not the save from the ole component
	// can be used or if the input changed
	boolean sourceChanged = false;

	/**
	 * Keep track of whether we have an active client so we do not
	 * deactivate multiple times
	 */
	private boolean clientActive = false;

	/**
	 * Keep track of whether we have activated OLE or not as some applications
	 * will only allow single activations.
	 */
	private boolean oleActivated = false;

	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			activateClient(part);
		}
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partClosed(IWorkbenchPart part) {
		}
		public void partOpened(IWorkbenchPart part) {
		}
		public void partDeactivated(IWorkbenchPart part) {
			deactivateClient(part);
		}
	};

	private static final String RENAME_ERROR_TITLE =
		WorkbenchMessages.getString("OleEditor.errorSaving"); //$NON-NLS-1$
	private static final String OLE_EXCEPTION_TITLE =
		WorkbenchMessages.getString("OleEditor.oleExceptionTitle"); //$NON-NLS-1$
	private static final String OLE_EXCEPTION_MESSAGE =
		WorkbenchMessages.getString("OleEditor.oleExceptionMessage"); //$NON-NLS-1$
	private static final String SAVE_ERROR_TITLE =
		WorkbenchMessages.getString("OleEditor.savingTitle"); //$NON-NLS-1$
	private static final String SAVE_ERROR_MESSAGE =
		WorkbenchMessages.getString("OleEditor.savingMessage"); //$NON-NLS-1$

	/**
	 * Return a new ole editor.
	 */
	public OleEditor() {
	}

	private void activateClient(IWorkbenchPart part) {
		if (part == this) {
			oleActivate();
			clientActive = true;
		}
	}
	
	/**
	 * createPartControl method comment.
	 */
	public void createPartControl(Composite parent) {
		// Create a frame.
		clientFrame = new OleFrame(parent, SWT.CLIP_CHILDREN);
		clientFrame.setBackground(JFaceColors.getBannerBackground(clientFrame.getDisplay()));

		initializeWorkbenchMenus();

		createClientSite();
	}

	/**
	 * Create the client site for the reciever
	 */
	private void createClientSite() {
		//If there was an OLE Error or nothing has been created yet
		if (clientFrame == null || clientFrame.isDisposed())
			return;
		// Create a OLE client site.
		clientSite = new OleClientSite(clientFrame, SWT.NONE, source);
		clientSite.setBackground(JFaceColors.getBannerBackground(clientFrame.getDisplay()));
	}

	private void deactivateClient(IWorkbenchPart part) {
		//Check the client active flag. Set it to false when we have deactivated
		//to prevent multiple deactivations.
		if (part == this && clientActive) {
			if (clientSite != null) {
				clientSite.deactivateInPlaceClient();
			}
			clientActive = false;
			oleActivated = false;
		}
	}
	
	/**
	 * Display an error dialog with the supplied title and message.
	 */
	private void displayErrorDialog(String title, String message) {
		Shell parent = null;
		if (getClientSite() != null) {	
			parent = getClientSite().getShell();
		}
		MessageDialog.openError(parent, title, message);
	}
	
	/**
	 * @see IWorkbenchPart#dispose
	 */
	public void dispose() {
		if (inPlaceInput != null) {
			inPlaceInput.setInPlaceEditor(null);
			inPlaceInput = null;
		}
		
		//can dispose the title image because it was created in init
		if (oleTitleImage != null) {
			oleTitleImage.dispose();
			oleTitleImage = null;
		}

		if (getSite() != null && getSite().getPage() != null) {
			getSite().getPage().removePartListener(partListener);
		}
	}
	
	/**
	 *	Print this object's contents
	 */
	public void doPrint() {
		if (clientSite == null)
			return;
		BusyIndicator.showWhile(clientSite.getDisplay(), new Runnable() {
			public void run() {
				clientSite.exec(OLE.OLECMDID_PRINT, OLE.OLECMDEXECOPT_PROMPTUSER, null, null);
				// note: to check for success: above == SWTOLE.S_OK
			}
		});
	}
	
	/**
	 *	Save the viewer's contents to the source file system file
	 */
	public void doSave(final IProgressMonitor monitor) {
		if(clientSite == null)
			return;
		BusyIndicator.showWhile(clientSite.getDisplay(), new Runnable() {
			public void run() {

				//Do not try and use the component provided save if the source has
				//changed in Eclipse
				if (!sourceChanged) {
					int result = clientSite.queryStatus(OLE.OLECMDID_SAVE);
					if ((result & OLE.OLECMDF_ENABLED) != 0) {
						result = clientSite.exec(OLE.OLECMDID_SAVE, OLE.OLECMDEXECOPT_PROMPTUSER, null, null);
						if (result == OLE.S_OK) {
							try {
								resource.refreshLocal(IResource.DEPTH_ZERO, monitor);
							} catch (CoreException ex) {
							}
						} else {
							displayErrorDialog(
								OLE_EXCEPTION_TITLE,
								OLE_EXCEPTION_MESSAGE + String.valueOf(result));
						}
						return;
					}
				}
				if (saveFile(source)) {
					try {
						resource.refreshLocal(IResource.DEPTH_ZERO, monitor);
					} catch (CoreException ex) {
					}
				} else
					displayErrorDialog(SAVE_ERROR_TITLE, SAVE_ERROR_MESSAGE + source.getName());
			}
		});
	}
	
	/**
	 *	Save the viewer's contents into the provided resource.
	 */
	public void doSaveAs() {
		if(clientSite == null)
			return;
		WorkspaceModifyOperation op = saveNewFileOperation();
		Shell shell = clientSite.getShell();
		try {
			new ProgressMonitorDialog(shell).run(false, true, op);
		} catch (InterruptedException interrupt) {
			//Nothing to reset so do nothing
		} catch (InvocationTargetException invocationException) {
			MessageDialog.openError(
				shell,
				RENAME_ERROR_TITLE,
				invocationException.getTargetException().getMessage());
		}

	}
	
	/**
	 *	Answer self's client site
	 *
	 *	@return org.eclipse.swt.ole.win32.OleClientSite
	 */
	public OleClientSite getClientSite() {
		return clientSite;
	}
	
	/**
	 *	Answer the file system representation of self's input element
	 *
	 *	@return java.io.File
	 */
	public File getSourceFile() {
		return source;
	}

	private void handleWord() {
		OleAutomation dispInterface = new OleAutomation(clientSite);
		// Get Application
		int[] appId = dispInterface.getIDsOfNames(new String[]{"Application"}); //$NON-NLS-1$
		if (appId != null) {
			Variant pVarResult = dispInterface.getProperty(appId[0]);
			if (pVarResult != null) {
				OleAutomation application = pVarResult.getAutomation();
				int[] dispid = application.getIDsOfNames(new String[] {"DisplayScrollBars"}); //$NON-NLS-1$
				if (dispid != null) {
					Variant rgvarg = new Variant(true);
					application.setProperty(dispid[0], rgvarg);
				}
				application.dispose();
			}
		}
		dispInterface.dispose();
	}

	/* (non-Javadoc)
	 * Initializes the editor when created from scratch.
	 * 
	 * This method is called soon after part construction and marks 
	 * the start of the extension lifecycle.  At the end of the
	 * extension lifecycle <code>shutdown</code> will be invoked
	 * to terminate the lifecycle.
	 *
	 * @param container an interface for communication with the part container
	 * @param input The initial input element for the editor.  In most cases
	 *    it is an <code>IFile</code> but other types are acceptable.
	 * @see IWorkbenchPart#shutdown
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// check input
		inPlaceInput = null;
		if (input instanceof IPathEditorInput) {
			inPlaceInput = (IInPlaceEditorInput) input;
		} else {
			inPlaceInput = (IInPlaceEditorInput) input.getAdapter(IInPlaceEditorInput.class);
		}
		if (inPlaceInput == null) {
			throw new PartInitException(
				WorkbenchMessages.format("OleEditor.invalidInput", new Object[] { input })); //$NON-NLS-1$
		}

		// input must have a physical file location
		IPath filePath = inPlaceInput.getPath();		
		if (filePath == null) {
			throw new PartInitException(
				WorkbenchMessages.format("OleEditor.noFileInput", new Object[] { filePath })); //$NON-NLS-1$
		}
		source = new File(filePath.toOSString());
		if (!source.exists()) {
			source = null;
			throw new PartInitException(
				WorkbenchMessages.format("OleEditor.noFileInput", new Object[] { filePath })); //$NON-NLS-1$
		}
		
		// Save input.
		setSite(site);
		setInput(input);
		inPlaceInput.setInPlaceEditor(this);

		// Update titles.
		setTitle(input.getName());
		setTitleToolTip(input.getToolTipText());
		ImageDescriptor desc = input.getImageDescriptor();
		if (desc != null) {
			oleTitleImage = desc.createImage();
			setTitleImage(oleTitleImage);
		}

		// Listen for part activation.
		site.getPage().addPartListener(partListener);
	}
	
	/**
	 *	Initialize the workbench menus for proper merging
	 */
	protected void initializeWorkbenchMenus() {
		//If there was an OLE Error or nothing has been created yet
		if (clientFrame == null || clientFrame.isDisposed())
			return;		
		// Get the browser menubar.  If one does not exist then
		// create it.
		Shell shell = clientFrame.getShell();
		Menu menuBar = shell.getMenuBar();
		if (menuBar == null) {
			menuBar = new Menu(shell, SWT.BAR);
			shell.setMenuBar(menuBar);
		}

		// Swap the file and window menus.
		MenuItem[] windowMenu = new MenuItem[1];
		MenuItem[] fileMenu = new MenuItem[1];
		Vector containerItems = new Vector();

		IWorkbenchWindow window = getSite().getWorkbenchWindow();

		for (int i = 0; i < menuBar.getItemCount(); i++) {
			MenuItem item = menuBar.getItem(i);
			String id = ""; //$NON-NLS-1$
			if (item.getData() instanceof IMenuManager)
				id = ((IMenuManager) item.getData()).getId();
			if (id.equals(IWorkbenchActionConstants.M_FILE))
				fileMenu[0] = item;
			else if (id.equals(IWorkbenchActionConstants.M_WINDOW))
				windowMenu[0] = item;
			else {
				if (window.isApplicationMenu(id)) {
					containerItems.addElement(item);
				}
			}
		}
		MenuItem[] containerMenu = new MenuItem[containerItems.size()];
		containerItems.copyInto(containerMenu);
		clientFrame.setFileMenus(fileMenu);
		clientFrame.setContainerMenus(containerMenu);
		clientFrame.setWindowMenus(windowMenu);
	}
	
	/* (non-Javadoc)
	 * Returns whether the contents of this editor have changed since the last save
	 * operation. As this is an external editor and we have no way of knowing return true
	 * if there is something to save to.
	 *
	 * @see IEditorPart
	 */
	public boolean isDirty() {
		/*Return only if we have a clientSite which is dirty 
		as this can be asked before anything is opened*/
		return this.clientSite != null;
	}
	
	/* (non-Javadoc)
	 * Returns whether the "save as" operation is supported by this editor. We assume we
	 * can always save a file whether it will be via OLE or not.
	 *
	 * @see IEditorPart
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/**
	 *	Since we don't know when a change has been made, always answer true
	 */
	public boolean isSaveNeeded() {
		//Answer false if it was not opened and true only if it is dirty
		return getClientSite() != null && isDirty();
	}
	
	/**
	 * Save the supplied file using the SWT API.
	 * @param file java.io.File
	 */
	private boolean saveFile(File file) {

		File tempFile = new File(file.getAbsolutePath() + ".tmp"); //$NON-NLS-1$
		file.renameTo(tempFile);
		boolean saved = false;
		if (OLE.isOleFile(file) || usesStorageFiles(clientSite.getProgramID())) {
			saved = clientSite.save(file, true);
		} else {
			saved = clientSite.save(file, false);
		}

		if (saved) {
			// save was successful so discard the backup
			tempFile.delete();
			return true;
		} else {
			// save failed so restore the backup
			tempFile.renameTo(file);
			return false;
		}
	}
	
	/**
	 * Save the new File using the client site.
	 */
	private WorkspaceModifyOperation saveNewFileOperation() {

		return new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor) throws CoreException {
				SaveAsDialog dialog = new SaveAsDialog(clientFrame.getShell());
				IFileEditorInput input = (IFileEditorInput)getEditorInput();
				IFile sFile = input.getFile();
				dialog.setOriginalFile(sFile);
				dialog.open();
				
				IPath newPath = dialog.getResult();
				if(newPath == null)
					return;
					
				if (dialog.getReturnCode() == Dialog.OK) {
					String projectName = newPath.segment(0);
					newPath = newPath.removeFirstSegments(1);
					IProject project = resource.getWorkspace().getRoot().getProject(projectName);
					newPath = project.getLocation().append(newPath);
					File newFile = newPath.toFile();
					if (saveFile(newFile)) {
						IFile newResource = resource.getWorkspace().getRoot().getFileForLocation(newPath);
						if (newResource != null) {
							sourceChanged(newResource);
							newResource.refreshLocal(IResource.DEPTH_ZERO, monitor);
						}
					} else {
						displayErrorDialog(SAVE_ERROR_TITLE, SAVE_ERROR_MESSAGE + newFile.getName());
						return;
					}
				}
			}
		};

	}
	
	/**
	 * Asks the part to take focus within the workbench.
	 */
	public void setFocus() {}
	
	/**
	 * Make ole active so that the controls are rendered.
	 */
	private void oleActivate() {
		//If there was an OLE Error or nothing has been created yet
		if (clientSite == null || clientFrame == null || clientFrame.isDisposed())
			return;

		if (!oleActivated) {
			clientSite.doVerb(OLE.OLEIVERB_SHOW);
			oleActivated = true;
			String progId = clientSite.getProgramID();
			if (progId != null && progId.startsWith("Word.Document")) {  //$NON-NLS-1$
				handleWord();
			}
		}
	}
	
	/**
	 * See if it is one of the known types that use OLE Storage.
	 */
	private static boolean usesStorageFiles(String progID) {
		return (progID != null && (progID.startsWith("Word.", 0) //$NON-NLS-1$
			|| progID.startsWith("MSGraph", 0) //$NON-NLS-1$
			|| progID.startsWith("PowerPoint", 0) //$NON-NLS-1$
			|| progID.startsWith("Excel", 0))); //$NON-NLS-1$
	}

	/* 
	 * See IEditorPart.isSaveOnCloseNeeded() 
	 */
	public boolean isSaveOnCloseNeeded() {
		return !sourceDeleted && super.isSaveOnCloseNeeded();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IInPlaceEditor#sourceDeleted()
	 */
	public void sourceDeleted() {
		sourceDeleted = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IInPlaceEditor#sourceChanged(org.eclipse.ui.IInPlaceEditorInput)
	 */
	public void sourceChanged(IInPlaceEditorInput input) {
		source = new File(input.getPath().toOSString());
		sourceChanged = true;
		if (inPlaceInput != null) {
			inPlaceInput.setInPlaceEditor(null);
		}
		setInput(input);
		inPlaceInput = input;
		if (inPlaceInput != null) {
			inPlaceInput.setInPlaceEditor(this);
		}
		firePropertyChange(IEditorPart.PROP_INPUT);
		setTitle(input.getName());
	}
}
