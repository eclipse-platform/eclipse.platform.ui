package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Standard action for pasting resources on the clipboard to the selected resource's location.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
/*package*/ class PasteAction extends SelectionListenerAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".PasteAction";//$NON-NLS-1$
	
	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;

	/**
	 * System clipboard
	 */
	private Clipboard clipboard;

/**
 * Creates a new action.
 *
 * @param shell the shell for any dialogs
 */
public PasteAction(Shell shell, Clipboard clipboard) {
	super(ResourceNavigatorMessages.getString("PasteAction.title")); //$NON-NLS-1$
	Assert.isNotNull(shell);
	Assert.isNotNull(clipboard);
	this.shell = shell;
	this.clipboard = clipboard;
	setToolTipText(ResourceNavigatorMessages.getString("PasteAction.toolTip")); //$NON-NLS-1$
	setId(PasteAction.ID);
	WorkbenchHelp.setHelp(this, INavigatorHelpContextIds.PASTE_ACTION);
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {
	// try a resource transfer
	ResourceTransfer resTransfer = ResourceTransfer.getInstance();
	IResource[] resourceData = (IResource[])clipboard.getContents(resTransfer);
	
	if (resourceData != null) {
		if (resourceData[0].getType() == IResource.PROJECT){
			CopyProjectOperation operation = new CopyProjectOperation(this.shell);
			operation.copyProject((IProject) resourceData[0]);
		} else {
			// enablement should ensure that we always have access to a container
			IContainer container = getContainer();
				
			CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(this.shell);
			operation.copyResources(resourceData, container);
		}
		return;
	}
	
	// try a file transfer
	FileTransfer fileTransfer = FileTransfer.getInstance();
	String[] fileData = (String[])clipboard.getContents(fileTransfer);
	
	if (fileData != null) {
		// enablement should ensure that we always have access to a container
		IContainer container = getContainer();
				
		CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(this.shell);
		operation.copyFiles(fileData, container);
	}
}

/**
 * Returns the container to hold the pasted resources.
 */
private IContainer getContainer() {
	List selection = getSelectedResources();
	if (selection.get(0) instanceof IFile)
		return ((IFile)selection.get(0)).getParent();
	else 
		return (IContainer)selection.get(0);
}

/**
 * The <code>PasteAction</code> implementation of this
 * <code>SelectionListenerAction</code> method enables this action if 
 * a resource compatible with what is on the clipboard is selected.
 */
protected boolean updateSelection(IStructuredSelection selection) {
	if (!super.updateSelection(selection)) 
		return false;
	
	// clipboard must have resources or files
	ResourceTransfer resTransfer = ResourceTransfer.getInstance();
	IResource[] resourceData = (IResource[])clipboard.getContents(resTransfer);
	FileTransfer fileTransfer = FileTransfer.getInstance();
	String[] fileData = (String[])clipboard.getContents(fileTransfer);
	if (resourceData == null && fileData == null)
		return false;

	// can paste an open project regardless of selection
	if (resourceData != null 
		&& resourceData.length == 1
		&& resourceData[0].getType() == IResource.PROJECT) {
		if (((IProject)resourceData[0]).isOpen())
			return true;
		else 
			return false;
	}

	// can paste files and folders to a single selection (project must be open)
	// or multiple file selection with the same parent
	if (getSelectedNonResources().size() > 0) 
		return false;
	List selectedResources = getSelectedResources();
	IResource targetResource = null;
	if (selectedResources.size() == 1) {
		targetResource = (IResource)selectedResources.get(0);
		if (targetResource instanceof IProject && !((IProject)targetResource).isOpen())
			return false;
	} else if (selectedResources.size() > 1) {
		for (int i = 0; i < selectedResources.size(); i++) {
			IResource resource = (IResource)selectedResources.get(i);
			if (resource.getType() != IResource.FILE)
				return false;
			if (targetResource == null)
				targetResource = resource.getParent();
			else if (!targetResource.equals(resource.getParent()))
				return false;
		}
	}
	if (targetResource != null && resourceData != null) {
		// don't try to copy to self
		for (int i = 0; i < resourceData.length; i++) {
			if (targetResource.equals(resourceData[i]))
				return false;
		}
	}
	return true;
}

}

