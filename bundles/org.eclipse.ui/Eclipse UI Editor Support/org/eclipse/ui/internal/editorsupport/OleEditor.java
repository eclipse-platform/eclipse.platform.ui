package org.eclipse.ui.internal.editorsupport;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.model.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.widgets.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 */
public class OleEditor extends EditorPart {
	private OleFrame clientFrame;
	private OleClientSite clientSite;
	private File source;
	private IFile resource;
	private Image oleTitleImage;

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

	private static final String FILE_PROMPTER_TITLE = WorkbenchMessages.getString("OleEditor.renameTitle"); //$NON-NLS-1$
	private static final String FILE_PROMPTER_MESSAGE = WorkbenchMessages.getString("OleEditor.renameMessage"); //$NON-NLS-1$
	private static final String RENAME_ERROR_TITLE = WorkbenchMessages.getString("OleEditor.errorSaving"); //$NON-NLS-1$
	private static final String OLE_EXCEPTION_TITLE = WorkbenchMessages.getString("OleEditor.oleExceptionTitle"); //$NON-NLS-1$
	private static final String OLE_EXCEPTION_MESSAGE = WorkbenchMessages.getString("OleEditor.oleExceptionMessage"); //$NON-NLS-1$
	private static final String SAVE_ERROR_TITLE = WorkbenchMessages.getString("OleEditor.savingTitle"); //$NON-NLS-1$
	private static final String SAVE_ERROR_MESSAGE = WorkbenchMessages.getString("OleEditor.savingMessage"); //$NON-NLS-1$
/**
 * Return a new ole editor.
 */
public OleEditor() {
}
private void activateClient(IWorkbenchPart part) {
	if (part == this) {
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
		clientFrame.getDisplay().getSystemColor(SWT.COLOR_WHITE));
	initializeWorkbenchMenus();

	// Set the input file.
	IEditorInput input = getEditorInput();
	if (input instanceof IFileEditorInput) {
		setResource(((IFileEditorInput) input).getFile());
	}

	// Create a OLE client site.
	clientSite = new OleClientSite(clientFrame, SWT.NONE, source);
	clientSite.setBackground(
		clientFrame.getDisplay().getSystemColor(SWT.COLOR_WHITE));
}
private void deactivateClient(IWorkbenchPart part) {
	//Check the client active flag. Set it to false when we have deactivated
	//to prevent multiple deactivations.
	if (part == this && clientActive){
		clientSite.deactivateInPlaceClient();
		this.clientActive = false;
		this.oleActivated = false;
	}
}
/**
 * Display an error dialog with the supplied title and message.
 */
private void displayErrorDialog(String title, String message) {

	MessageDialog.openError(getClientSite().getShell(), title, message);
}
/**
 * @see IWorkbenchPart#dispose
 */
public void dispose() {
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
	BusyIndicator.showWhile(clientSite.getDisplay(), 
		new Runnable() {
			public void run() {
				clientSite.exec(OLE.OLECMDID_PRINT, OLE.OLECMDEXECOPT_PROMPTUSER,null,null);
				// note: to check for success: above == SWTOLE.S_OK
			}
		});
}
/**
 *	Save the viewer's contents to the source file system file
 */
public void doSave(final IProgressMonitor monitor) {
	BusyIndicator.showWhile(clientSite.getDisplay(), new Runnable() {
		public void run() {

			int result = clientSite.queryStatus(OLE.OLECMDID_SAVE);
			if ((result & OLE.OLECMDF_ENABLED) == OLE.OLECMDF_ENABLED) {
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
	throws PartInitException 
{
	// Check input.
	if (!(input instanceof IFileEditorInput))
		throw new PartInitException(WorkbenchMessages.format("OleEditor.invalidInput", new Object[]{input})); //$NON-NLS-1$

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
		String id = "";//$NON-NLS-1$
		if (item.getData() instanceof IMenuManager)
			id = ((IMenuManager) item.getData()).getId();
		if (id.equals(IWorkbenchActionConstants.M_FILE))
			fileMenu[0] = item;
		else
			if (id.equals(IWorkbenchActionConstants.M_WINDOW))
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

	int result = clientSite.queryStatus(OLE.OLECMDID_SAVE);
	if ((result & OLE.OLECMDF_ENABLED) == OLE.OLECMDF_ENABLED) {
		result =
			clientSite.exec(OLE.OLECMDID_SAVE, OLE.OLECMDEXECOPT_PROMPTUSER, null, null);
		if (result == OLE.S_OK)
			return true;
	}

	File tempFile = new File(file.getAbsolutePath() + ".tmp");
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
 * Save the new File using the client site
 * @param file java.io.File
 */
private WorkspaceModifyOperation saveNewFileOperation() {

	return new WorkspaceModifyOperation() {
		public void execute(final IProgressMonitor monitor) throws CoreException {
			int result = clientSite.queryStatus(OLE.OLECMDID_SAVEAS);
			if ((result & OLE.OLECMDF_ENABLED) == OLE.OLECMDF_ENABLED) {
				result =
					clientSite.exec(OLE.OLECMDID_SAVEAS, OLE.OLECMDEXECOPT_PROMPTUSER, null, null);
			} else {
				InputDialog dialog =
					new InputDialog(
						clientFrame.getShell(),
						FILE_PROMPTER_TITLE,
						FILE_PROMPTER_MESSAGE,
						source.getName(),
						null);
				dialog.setBlockOnOpen(true);
				dialog.open();

				if (dialog.getReturnCode() == dialog.OK) {
					File newFile = new File(source.getParentFile(), dialog.getValue());

					if (!saveFile(newFile)) {
						displayErrorDialog(SAVE_ERROR_TITLE, SAVE_ERROR_MESSAGE + newFile.getName());
						return;
					}
				}
			}
			resource.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
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
	
	if(!oleActivated){
		clientSite.doVerb(OLE.OLEIVERB_SHOW);
		oleActivated = true;
	}
	clientFrame.setFocus();
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
	if (progID.startsWith("Word.", 0)
		|| progID.startsWith("MSGraph", 0)
		|| progID.startsWith("PowerPoint", 0)
		|| progID.startsWith("Excel", 0))
		return true;

	return false;
}
}
