package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.DialogUtil;

/**
 * Standard action for opening an editor on the currently selected file 
 * resource(s).
 * <p>
 * Note that there is a different action for opening closed projects:
 * <code>OpenResourceAction</code>.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class OpenFileAction extends OpenSystemEditorAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenFileAction";//$NON-NLS-1$

	/**
	 * The editor to open.
	 */
	private IEditorDescriptor editorDescriptor;
/**
 * Creates a new action that will open editors on the then-selected file 
 * resources. Equivalent to <code>OpenFileAction(page,null)</code>.
 *
 * @param page the workbench page in which to open the editor
 */
public OpenFileAction(IWorkbenchPage page) {
	this(page, null);
}
/**
 * Creates a new action that will open instances of the specified editor on 
 * the then-selected file resources.
 *
 * @param page the workbench page in which to open the editor
 * @param descriptor the editor descriptor, or <code>null</code> if unspecified
 */
public OpenFileAction(IWorkbenchPage page, IEditorDescriptor descriptor) {
	super(page);
	setText(descriptor == null ? WorkbenchMessages.getString("OpenFileAction.text") : descriptor.getLabel()); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.OPEN_FILE_ACTION);
	setToolTipText(WorkbenchMessages.getString("OpenFileAction.toolTip")); //$NON-NLS-1$
	setId(ID);
	this.editorDescriptor = descriptor;
}
/**
 * Ensures that the contents of the given file resource are local.
 *
 * @param file the file resource
 * @return <code>true</code> if the file is local, and <code>false</code> if
 *   it could not be made local for some reason
 */
boolean ensureFileLocal(final IFile file) {
	//Currently fails due to Core PR.  Don't do it for now
	//1G5I6PV: ITPCORE:WINNT - IResource.setLocal() attempts to modify immutable tree
	//file.setLocal(true, IResource.DEPTH_ZERO);
	return true;
}
/**
 * Opens an editor on the given file resource.
 *
 * @param file the file resource
 */
void openFile(IFile file) {
	if (getWorkbenchPage() == null) {
		IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("OpenFileAction.openFileError"), null); //$NON-NLS-1$
		WorkbenchPlugin.log(WorkbenchMessages.getString("OpenFileAction.openFileErrorTitle"), status); //$NON-NLS-1$
		return;
	}
	try {
		boolean activate = OpenStrategy.getOpenMethod() == OpenStrategy.DOUBLE_CLICK;
		if (editorDescriptor == null)
			getWorkbenchPage().openEditor(file,null,activate);
		else {
			if (ensureFileLocal(file))
				getWorkbenchPage().openEditor(file, editorDescriptor.getId(),activate);
		}
	} catch (PartInitException e) {
		DialogUtil.openError(
			getWorkbenchPage().getWorkbenchWindow().getShell(),
			WorkbenchMessages.getString("OpenFileAction.openFileShellTitle"), //$NON-NLS-1$
			e.getMessage(),
			e);
	}
}

	/**
	 * Handle a double click.
	 * 
	 * @deprecated this class should not assume it's the default action;
	 *   just use selectionChanged and run() if so
	 */
	public void handleDoubleClick(IStructuredSelection selection) {
		
		Object element = selection.getFirstElement();
		if (element instanceof IFile) {
			selectionChanged(selection);
			run();
		}
	}

}
