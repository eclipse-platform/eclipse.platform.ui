package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.update.internal.ui.*;
import org.eclipse.ui.*;
import org.eclipse.core.runtime.IAdaptable;

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
		if (page == null || 
		   page.getPerspective().
		         getId().equals(UpdatePerspective.PERSPECTIVE_ID))
			return;
		IWorkbenchWindow window = page.getWorkbenchWindow();

		try {
			IAdaptable input = UpdateUIPlugin.getWorkspace();
			window.getWorkbench().openPage(UpdatePerspective.PERSPECTIVE_ID, input, 0);
		} catch (WorkbenchException e) {
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
