package org.eclipse.ui.internal.editorsupport.win32;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 */
public class OleEditor extends EditorPart {

	/**
	 * The resource listener updates the receiver when
	 * a change has occured.
	 */
	private IResourceChangeListener resourceListener =
		new IResourceChangeListener() {

		/*
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta mainDelta = event.getDelta();
			if(mainDelta == null)
				return;
			IResourceDelta affectedElement =
				mainDelta.findMember(resource.getFullPath());
			if (affectedElement != null)
				try {
					processDelta(affectedElement);
				} catch (CoreException exception) {
					//Failed so close the receiver
					getSite().getPage().closeEditor(OleEditor.this, true);
				}
		}

		/*
		 * Process the delta for the receiver
		 */
		private boolean processDelta(final IResourceDelta delta) throws CoreException {

			Runnable changeRunnable = null;

			switch (delta.getKind()) {
				case IResourceDelta.REMOVED :
					if ((IResourceDelta.MOVED_TO & delta.getFlags()) != 0) {
						changeRunnable = new Runnable() {
							public void run() {
								IPath path = delta.getMovedToPath();
								IFile newFile = delta.getResource().getWorkspace().getRoot().getFile(path);
								if (newFile != null) {
									sourceChanged(newFile);
								}
							}
						};
					} else {
						changeRunnable = new Runnable() {
							public void run() {
								sourceDeleted = true;
								getSite().getPage().closeEditor(OleEditor.this, true);
							}
						};

					}

					break;
			}

			if (changeRunnable != null)
				update(changeRunnable);

			return true; // because we are sitting on files anyway
		}

	};

	private OleFrame clientFrame;
	private OleClientSite clientSite;
	private File source;
	private IFile resource;
	private Image oleTitleImage;
	//The sourceDeleted flag makes sure that the receiver is not
	//dirty when shutting down
	boolean sourceDeleted = false;
	//The sourceChanged flag indicates whether or not the save from the ole component
	//can be used or if the input changed
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

	private static final String FILE_PROMPTER_TITLE =
		WorkbenchMessages.getString("OleEditor.renameTitle"); //$NON-NLS-1$
	//$NON-NLS-1$
	private static final String FILE_PROMPTER_MESSAGE =
		WorkbenchMessages.getString("OleEditor.renameMessage"); //$NON-NLS-1$
	//$NON-NLS-1$
	private static final String RENAME_ERROR_TITLE =
		WorkbenchMessages.getString("OleEditor.errorSaving"); //$NON-NLS-1$
	//$NON-NLS-1$
	private static final String OLE_EXCEPTION_TITLE =
		WorkbenchMessages.getString("OleEditor.oleExceptionTitle"); //$NON-NLS-1$
	//$NON-NLS-1$
	private static final String OLE_EXCEPTION_MESSAGE =
		WorkbenchMessages.getString("OleEditor.oleExceptionMessage"); //$NON-NLS-1$
	//$NON-NLS-1$
	private static final String SAVE_ERROR_TITLE =
		WorkbenchMessages.getString("OleEditor.savingTitle"); //$NON-NLS-1$
	//$NON-NLS-1$
	private static final String SAVE_ERROR_MESSAGE =
		WorkbenchMessages.getString("OleEditor.savingMessage"); //$NON-NLS-1$
	//$NON-NLS-1$
	/**
	 * Return a new ole editor.
	 */
	public OleEditor() {
	}

	private void activateClient(IWorkbenchPart part) {
		if (part == this) {
			//Do a deactivation as some OLE controls will not update menus
			if(clientSite != null)
				clientSite.deactivateInPlaceClient();
			setFocus();
			this.clientActive = true;
		}
	}
	/**
	 * createPartControl method comment.
	 */
	public void createPartControl(Composite parent) {

		// Create a frame.
		clientFrame = new OleFrame(parent, SWT.CLIP_CHILDREN);
		clientFrame.setBackground(
			JFaceColors.getBannerBackground(clientFrame.getDisplay()));

		initializeWorkbenchMenus();

		// Set the input file.
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput) {
			setResource(((IFileEditorInput) input).getFile());
			resource.getWorkspace().addResourceChangeListener(resourceListener);
		}

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
		clientSite.setBackground(
			JFaceColors.getBannerBackground(clientFrame.getDisplay()));

	}

	private void deactivateClient(IWorkbenchPart part) {
		//Check the client active flag. Set it to false when we have deactivated
		//to prevent multiple deactivations.
		if (part == this && clientActive) {
			if(clientSite != null)
				clientSite.deactivateInPlaceClient();
			this.clientActive = false;
			this.oleActivated = false;
		}
	}
	/**
	 * Display an error dialog with the supplied title and message.
	 */
	private void displayErrorDialog(String title, String message) {
		Shell parent = null;
		if(getClientSite() != null)	
			parent = getClientSite().getShell();
		MessageDialog.openError(parent, title, message);
	}
	/**
	 * @see IWorkbenchPart#dispose
	 */
	public void dispose() {
		if (resource != null)
			resource.getWorkspace().removeResourceChangeListener(resourceListener);

		//can dispose the title image because it was created in init
		if (oleTitleImage != null) {
			oleTitleImage.dispose();
			oleTitleImage = null;
		}

		if (getSite() != null && getSite().getPage() != null)
			getSite().getPage().removePartListener(partListener);

	}
	/**
	 *	Print this object's contents
	 */
	public void doPrint() {
		if(clientSite == null)
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
						result =
							clientSite.exec(OLE.OLECMDID_SAVE, OLE.OLECMDEXECOPT_PROMPTUSER, null, null);
						if (result == OLE.S_OK) {
							try {
								resource.refreshLocal(resource.DEPTH_ZERO, monitor);
							} catch (CoreException ex) {
							}
							return;
						} else {
							displayErrorDialog(
								OLE_EXCEPTION_TITLE,
								OLE_EXCEPTION_MESSAGE + String.valueOf(result));
							return;
						}
					}
				}
				if (saveFile(source)) {
					try {
						resource.refreshLocal(resource.DEPTH_ZERO, monitor);
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
	/* (non-Javadoc)
	 * Sets the cursor and selection state for this editor to the passage defined
	 * by the given marker.
	 *
	 * @see IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
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
	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {
		// Check input.
		if (!(input instanceof IFileEditorInput))
			throw new PartInitException(
				WorkbenchMessages.format("OleEditor.invalidInput", new Object[] { input })); //$NON-NLS-1$
		//$NON-NLS-1$

		// Save input.
		setSite(site);
		setInput(input);

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
					
				if (dialog.getReturnCode() == dialog.OK) {
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
	public void setFocus() {
		//If there was an OLE Error or nothing has been created yet
		if (clientFrame == null || clientFrame.isDisposed())
			return;
		oleActivate();
		clientFrame.setFocus();
	}
	
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
		}
	}
	
	/**
	 *	Set the file resource that this object is displaying
	 */
	protected void setResource(IFile file) {
		resource = file;
		source = new File(file.getLocation().toOSString());
	}
	/*
	* See if it is one of the known types that use OLE Storage
	* @return boolean
	*/

	private static boolean usesStorageFiles(String progID) {
		if (progID.startsWith("Word.", 0) //$NON-NLS-1$
			|| progID.startsWith("MSGraph", 0) //$NON-NLS-1$
			|| progID.startsWith("PowerPoint", 0) //$NON-NLS-1$
			|| progID.startsWith("Excel", 0)) //$NON-NLS-1$
			return true;

		return false;
	}

	/**
	 * The source has changed to the newFile. Update
	 * editors and set any required flags
	 */
	private void sourceChanged(IFile newFile) {

		FileEditorInput newInput = new FileEditorInput(newFile);
		setInput(newInput);
		setResource(newFile);
		sourceChanged = true;
		setTitle(newInput.getName());

	}

	/* 
	 * See IEditorPart.isSaveOnCloseNeeded() 
	 */
	public boolean isSaveOnCloseNeeded() {
		return !sourceDeleted && super.isSaveOnCloseNeeded();
	}

	/*
		 * Posts the update code "behind" the running operation.
		 *
		 * @param runnable the update code
		 */
	private void update(Runnable runnable) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		if (windows != null && windows.length > 0) {
			Display display = windows[0].getShell().getDisplay();
			display.asyncExec(runnable);
		} else
			runnable.run();
	}

}