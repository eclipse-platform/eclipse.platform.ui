package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Opens a new window. The initial perspective
 * for the new window will be the same type as
 * the active perspective in the window which this
 * action is running in. The default input for the 
 * new window's page is the workspace root.
 */
public class OpenNewWindowAction extends Action {
	private IWorkbenchWindow workbenchWindow;
	private IAdaptable pageInput;

	/**
	 * Creates a new <code>OpenNewWindowAction</code>. Sets
	 * the new window page's input to be the workspace root
	 * by default.
	 * 
	 * @param window the workbench window containing this action
	 */
	public OpenNewWindowAction(IWorkbenchWindow window) {
		this(window, WorkbenchPlugin.getPluginWorkspace().getRoot());
	}

	/**
	 * Creates a new <code>OpenNewWindowAction</code>.
	 * 
	 * @param window the workbench window containing this action
	 * @param input the input for the new window's page
	 */
	public OpenNewWindowAction(IWorkbenchWindow window, IAdaptable input) {
		super(WorkbenchMessages.getString("OpenNewWindowAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("OpenNewWindowAction.toolTip")); //$NON-NLS-1$
		workbenchWindow = window;
		pageInput = input;
		WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.OPEN_NEW_WINDOW_ACTION});
	}

	/**
	 * Set the input to use for the new window's page.
	 */
	public void setPageInput(IAdaptable input) {
		pageInput = input;
	}
	
	/**
	 * The implementation of this <code>IAction</code> method
	 * opens a new window. The initial perspective
	 * for the new window will be the same type as
	 * the active perspective in the window which this
	 * action is running in.
	 */
	public void run() {
		try {
			String perspId;
			
			IWorkbenchPage page = workbenchWindow.getActivePage();
			if (page != null && page.getPerspective() != null)
				perspId = page.getPerspective().getId();
			else
				perspId = workbenchWindow.getWorkbench().getPerspectiveRegistry().getDefaultPerspective();

			workbenchWindow.getWorkbench().openWorkbenchWindow(perspId, pageInput);
		} catch (WorkbenchException e) {
			ErrorDialog.openError(
				workbenchWindow.getShell(),
				WorkbenchMessages.getString("OpenNewWindowAction.errorTitle"), //$NON-NLS-1$,
				e.getMessage(),
				e.getStatus());
		}
	}
}
