package org.eclipse.update.ui.internal.manager;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.update.ui.internal.*;
import org.eclipse.ui.*;

/**
 * Insert the type's description here.
 * @see IWorkbenchWindowActionDelegate
 */
public class UpdateManagerAction implements IWorkbenchWindowActionDelegate {
	/**
	 * The constructor.
	 */
	public UpdateManagerAction() {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action)  {
		IWorkbenchPage page = UpdateUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (page == null)
			return;
		page.setEditorAreaVisible(true);

		// see if we already have an update manager
		IEditorPart[] editors = page.getEditors();
		for (int i = 0; i < editors.length; i++){
			if (editors[i] instanceof UpdateManager) {
				page.bringToTop(editors[i]);
				return;
			}
		}
	
		try {
			page.openEditor(new UpdateManagerInput(), UpdateUIPlugin.UPDATE_MANAGER_ID);
		} catch (PartInitException e) {
		/*
			IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("QuickStartAction.openEditorException"), e); //$NON-NLS-1$
			ErrorDialog.openError(
				workbench.getActiveWorkbenchWindow().getShell(),
				WorkbenchMessages.getString("QuickStartAction.errorDialogTitle"),  //$NON-NLS-1$
				WorkbenchMessages.getString("QuickStartAction.errorDialogMessage"),  //$NON-NLS-1$
				status);
		*/
			System.out.println(e);
		}
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction arg0, ISelection arg1)  {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose()  {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow arg0)  {
	}
}
