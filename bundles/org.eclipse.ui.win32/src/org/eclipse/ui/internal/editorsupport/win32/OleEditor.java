/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editorsupport.win32;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 */
public class OleEditor extends EditorPart {

    /**
     * The resource listener updates the receiver when
     * a change has occurred.
     */
    private IResourceChangeListener resourceListener = new IResourceChangeListener() {

        /*
         * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
         */
        public void resourceChanged(IResourceChangeEvent event) {
            IResourceDelta mainDelta = event.getDelta();
            if (mainDelta == null)
                return;
            IResourceDelta affectedElement = mainDelta.findMember(resource
                    .getFullPath());
            if (affectedElement != null)
                processDelta(affectedElement);
        }

        /*
         * Process the delta for the receiver
         */
        private boolean processDelta(final IResourceDelta delta) {

            Runnable changeRunnable = null;

            switch (delta.getKind()) {
            case IResourceDelta.REMOVED:
                if ((IResourceDelta.MOVED_TO & delta.getFlags()) != 0) {
                    changeRunnable = new Runnable() {
                        public void run() {
                            IPath path = delta.getMovedToPath();
                            IFile newFile = delta.getResource().getWorkspace()
                                    .getRoot().getFile(path);
                            if (newFile != null) {
                                sourceChanged(newFile);
                            }
                        }
                    };
                } else {
                    changeRunnable = new Runnable() {
                        public void run() {
                            sourceDeleted = true;
                            getSite().getPage().closeEditor(OleEditor.this,
                                    true);
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

    private static final String RENAME_ERROR_TITLE = OleMessages
            .getString("OleEditor.errorSaving"); //$NON-NLS-1$

    private static final String OLE_EXCEPTION_TITLE = OleMessages
            .getString("OleEditor.oleExceptionTitle"); //$NON-NLS-1$

    private static final String OLE_EXCEPTION_MESSAGE = OleMessages
            .getString("OleEditor.oleExceptionMessage"); //$NON-NLS-1$

    private static final String OLE_CREATE_EXCEPTION_MESSAGE = OleMessages
            .getString("OleEditor.oleCreationExceptionMessage"); //$NON-NLS-1$

    private static final String OLE_CREATE_EXCEPTION_REASON = OleMessages
            .getString("OleEditor.oleCreationExceptionReason"); //$NON-NLS-1$

    private static final String SAVE_ERROR_TITLE = OleMessages
            .getString("OleEditor.savingTitle"); //$NON-NLS-1$

    private static final String SAVE_ERROR_MESSAGE = OleMessages
            .getString("OleEditor.savingMessage"); //$NON-NLS-1$

    /**
     * Return a new ole editor.
     */
    public OleEditor() {
        //Do nothing
    }

    private void activateClient(IWorkbenchPart part) {
        if (part == this) {
            oleActivate();
            this.clientActive = true;
        }
    }

    /**
     * createPartControl method comment.
     */
    public void createPartControl(Composite parent) {

        // Create a frame.
        clientFrame = new OleFrame(parent, SWT.CLIP_CHILDREN);
        clientFrame.setBackground(JFaceColors.getBannerBackground(clientFrame
                .getDisplay()));

        initializeWorkbenchMenus();

        createClientSite();
        updateDirtyFlag();
    }

    /**
     * Create the client site for the receiver
     */

    private void createClientSite() {
        //If there was an OLE Error or nothing has been created yet
        if (clientFrame == null || clientFrame.isDisposed())
            return;
        // Create a OLE client site.
        try {
            clientSite = new OleClientSite(clientFrame, SWT.NONE, source);
        } catch (SWTException exception) {

            IStatus errorStatus = new Status(IStatus.ERROR,
                    WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR,
                    OLE_CREATE_EXCEPTION_REASON, exception);
            //ErrorDialog.openError(null, OLE_EXCEPTION_TITLE, OLE_CREATE_EXCEPTION_REASON, errorStatus);
            ErrorDialog.openError(null, OLE_EXCEPTION_TITLE, OLE_CREATE_EXCEPTION_MESSAGE, errorStatus);
            return;
        }
        clientSite.setBackground(JFaceColors.getBannerBackground(clientFrame
                .getDisplay()));

    }

    private void deactivateClient(IWorkbenchPart part) {
        //Check the client active flag. Set it to false when we have deactivated
        //to prevent multiple de-activations.
        if (part == this && clientActive) {
            if (clientSite != null)
                clientSite.deactivateInPlaceClient();
            this.clientActive = false;
            this.oleActivated = false;
        }
    }

    /**
     * Display an error dialog with the supplied title and message.
     * @param title
     * @param message
     */
    private void displayErrorDialog(String title, String message) {
        Shell parent = null;
        if (getClientSite() != null)
            parent = getClientSite().getShell();
        MessageDialog.openError(parent, title, message);
    }

    /**
     * @see IWorkbenchPart#dispose
     */
    public void dispose() {
        if (resource != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                    resourceListener);
            resource = null;
        }

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
        if (clientSite == null)
            return;
        BusyIndicator.showWhile(clientSite.getDisplay(), new Runnable() {
            public void run() {
                clientSite.exec(OLE.OLECMDID_PRINT,
                        OLE.OLECMDEXECOPT_PROMPTUSER, null, null);
                // note: to check for success: above == SWTOLE.S_OK
            }
        });
    }

    /**
     *	Save the viewer's contents to the source file system file
     */
    public void doSave(final IProgressMonitor monitor) {
        if (clientSite == null)
            return;
        BusyIndicator.showWhile(clientSite.getDisplay(), new Runnable() {

            /*
             *  (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            public void run() {

                //Do not try and use the component provided save if the source has
                //changed in Eclipse
                if (!sourceChanged) {
                    int result = clientSite.queryStatus(OLE.OLECMDID_SAVE);
                    if ((result & OLE.OLECMDF_ENABLED) != 0) {
                        result = clientSite.exec(OLE.OLECMDID_SAVE,
                                OLE.OLECMDEXECOPT_PROMPTUSER, null, null);
                        if (result == OLE.S_OK) {
                            try {
                                resource.refreshLocal(IResource.DEPTH_ZERO,
                                        monitor);
                            } catch (CoreException ex) {
                                //Do nothing on a failed refresh
                            }
                            return;
                        }
                        displayErrorDialog(OLE_EXCEPTION_TITLE,
                                OLE_EXCEPTION_MESSAGE + String.valueOf(result));
                        return;
                    }
                }
                if (saveFile(source)) {
                    try {
                    	if (resource != null)
                    		resource.refreshLocal(IResource.DEPTH_ZERO, monitor);
                    } catch (CoreException ex) {
                        //Do nothing on a failed refresh
                    }
                } else
                    displayErrorDialog(SAVE_ERROR_TITLE, SAVE_ERROR_MESSAGE
                            + source.getName());
            }
        });
    }

    /**
     *	Save the viewer's contents into the provided resource.
     */
    public void doSaveAs() {
        if (clientSite == null)
            return;
        WorkspaceModifyOperation op = saveNewFileOperation();
        Shell shell = clientSite.getShell();
        try {
            new ProgressMonitorDialog(shell).run(false, true, op);
        } catch (InterruptedException interrupt) {
            //Nothing to reset so do nothing
        } catch (InvocationTargetException invocationException) {
            MessageDialog.openError(shell, RENAME_ERROR_TITLE,
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
        int[] appId = dispInterface
                .getIDsOfNames(new String[] { "Application" }); //$NON-NLS-1$
        if (appId != null) {
            Variant pVarResult = dispInterface.getProperty(appId[0]);
            if (pVarResult != null) {
                OleAutomation application = pVarResult.getAutomation();
                int[] dispid = application
                        .getIDsOfNames(new String[] { "DisplayScrollBars" }); //$NON-NLS-1$
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
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
    	
    	validatePathEditorInput(input);

        // Save input.
        setSite(site);
        setInputWithNotify(input);

        // Update titles.
        setPartName(input.getName());
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
     * Validates the given input
     * 
     * @param input the editor input to validate
     * @throws PartInitException if the editor input is not OK
     */
    private boolean validatePathEditorInput(IEditorInput input) throws PartInitException {
        // Check input type.
    	IPathEditorInput pathEditorInput = (IPathEditorInput)input.getAdapter(IPathEditorInput.class);
        if (pathEditorInput == null)
            throw new PartInitException(OleMessages.format(
                    "OleEditor.invalidInput", new Object[] { input })); //$NON-NLS-1$
        
        IPath path = pathEditorInput.getPath();

        //Cannot create this with a file and no physical location
        if (!(new File(path.toOSString()).exists()))
            throw new PartInitException(
                    OleMessages
                            .format(
                                    "OleEditor.noFileInput", new Object[] { path.toOSString() })); //$NON-NLS-1$
        return true;
    }
    
    /**
     *	Initialize the workbench menus for proper merging
     */
    protected void initializeWorkbenchMenus() {
        //If there was an OLE Error or nothing has been created yet
        if (clientFrame == null || clientFrame.isDisposed())
            return;
        // Get the browser menu bar.  If one does not exist then
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

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    public boolean isDirty() {
        /*Return only if we have a clientSite which is dirty 
         as this can be asked before anything is opened*/
        return clientSite != null && clientSite.isDirty();
    }

    /* 
     * (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return true;
    }

    /**
     *Since we don't know when a change has been made, always answer true
     * @return <code>false</code> if it was not opened and <code>true</code> 
     * only if it is dirty
     */
    public boolean isSaveNeeded() {
        return isDirty();
    }

    /**
     * Save the supplied file using the SWT API.
     * @param file java.io.File
     * @return <code>true</code> if the save was successful
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
        }
        // save failed so restore the backup
        tempFile.renameTo(file);
        return false;
    }

    /**
     * Save the new File using the client site.
     * @return WorkspaceModifyOperation
     */
    private WorkspaceModifyOperation saveNewFileOperation() {

        return new WorkspaceModifyOperation() {
            public void execute(final IProgressMonitor monitor)
                    throws CoreException {
                SaveAsDialog dialog = new SaveAsDialog(clientFrame.getShell());
                IFile sFile = ResourceUtil.getFile(getEditorInput());
                if (sFile != null) {
                    dialog.setOriginalFile(sFile);
                    dialog.open();
    
                    IPath newPath = dialog.getResult();
                    if (newPath == null)
                        return;
    
                    if (dialog.getReturnCode() == Window.OK) {
                        String projectName = newPath.segment(0);
                        newPath = newPath.removeFirstSegments(1);
                        IProject project = resource.getWorkspace().getRoot()
                                .getProject(projectName);
                        newPath = project.getLocation().append(newPath);
                        File newFile = newPath.toFile();
                        if (saveFile(newFile)) {
                            IFile newResource = resource.getWorkspace().getRoot()
                                    .getFileForLocation(newPath);
                            if (newResource != null) {
                                sourceChanged(newResource);
                                newResource.refreshLocal(IResource.DEPTH_ZERO,
                                        monitor);
                            }
                        } else {
                            displayErrorDialog(SAVE_ERROR_TITLE, SAVE_ERROR_MESSAGE
                                    + newFile.getName());
                            return;
                        }
                    }
                }
            }
        };

    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        //Do not take focus
    }

    /**
     * Make ole active so that the controls are rendered.
     */
    private void oleActivate() {
        //If there was an OLE Error or nothing has been created yet
        if (clientSite == null || clientFrame == null
                || clientFrame.isDisposed())
            return;

        if (!oleActivated) {
            clientSite.doVerb(OLE.OLEIVERB_SHOW);
            oleActivated = true;
            String progId = clientSite.getProgramID();
            if (progId != null && progId.startsWith("Word.Document")) { //$NON-NLS-1$
                handleWord();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#setInputWithNotify(org.eclipse.ui.IEditorInput)
     */
    protected void setInputWithNotify(IEditorInput input) {
    	IPathEditorInput pathEditorInput = (IPathEditorInput)input.getAdapter(IPathEditorInput.class);
    	if (pathEditorInput != null)
    		source = new File(pathEditorInput.getPath().toOSString());
    	
        if (input instanceof IFileEditorInput) {
        	if (resource == null)
        		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
        	resource = ((IFileEditorInput)input).getFile();
        } else if (resource != null) {
        	ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
        	resource = null;
        }
        
        super.setInputWithNotify(input);
    }

    /**
     * See if it is one of the known types that use OLE Storage.
     * @param progID the type to test
     * @return <code>true</code> if it is one of the known types
     */
    private static boolean usesStorageFiles(String progID) {
        return (progID != null && (progID.startsWith("Word.", 0) //$NON-NLS-1$
                || progID.startsWith("MSGraph", 0) //$NON-NLS-1$
                || progID.startsWith("PowerPoint", 0) //$NON-NLS-1$
        || progID.startsWith("Excel", 0))); //$NON-NLS-1$
    }

    /**
     * The source has changed to the newFile. Update
     * editors and set any required flags
     * @param newFile The file to get the new contents from.
     */
    private void sourceChanged(IFile newFile) {

        FileEditorInput newInput = new FileEditorInput(newFile);
        setInputWithNotify(newInput);
        sourceChanged = true;
        setPartName(newInput.getName());

    }

    /* 
     * See IEditorPart.isSaveOnCloseNeeded() 
     */
    public boolean isSaveOnCloseNeeded() {
        return !sourceDeleted && super.isSaveOnCloseNeeded();
    }

    /**
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

    private boolean isDirty = false;
    private void updateDirtyFlag() {
    	final Runnable dirtyFlagUpdater = new Runnable() {
			public void run() {
				if (clientSite == null || resource == null) return;
				boolean dirty = isDirty(); 
				if (isDirty != dirty) {
					isDirty = dirty;
					firePropertyChange(PROP_DIRTY);
				}
				clientSite.getDisplay().timerExec(1000, this);
			}
    	};
    	dirtyFlagUpdater.run();
    }
}
