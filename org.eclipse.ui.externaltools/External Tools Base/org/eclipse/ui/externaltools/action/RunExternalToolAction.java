package org.eclipse.ui.externaltools.action;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.externaltools.internal.menu.FavoritesManager;
import org.eclipse.ui.externaltools.internal.model.DefaultRunnerContext;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.ui.LogConsoleDocument;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to run an external tool.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class RunExternalToolAction extends Action {
	private boolean alwaysPromptForArguments = false;
	private IWorkbenchWindow window;
	private ExternalTool tool;
	
	/**
	 * Create an action to run an external tool
	 */
	public RunExternalToolAction(IWorkbenchWindow window) {
		this(window, false);
		setText(ToolMessages.getString("RunExternalToolAction.text")); //$NON-NLS-1$
		setToolTipText(ToolMessages.getString("RunExternalToolAction.toolTip")); //$NON-NLS-1$
		setHoverImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/clcl16/run_tool.gif")); //$NON-NLS-1$
		setImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/elcl16/run_tool.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/dlcl16/run_tool.gif")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.RUN_TOOL_ACTION);
	}

	/**
	 * Create an action to run an external tool
	 */
	protected RunExternalToolAction(IWorkbenchWindow window, boolean alwaysPromptForArguments) {
		super();
		this.window = window;
		this.alwaysPromptForArguments = alwaysPromptForArguments;
	}

	/**
	 * Returns the last external tool to be run
	 * or <code>null</code> if none.
	 */
	public static final ExternalTool getLastTool() {
		return FavoritesManager.getInstance().getLastTool();	
	}

	/**
	 * Returns the external tool that will be run
	 * or <code>null</code> if none.
	 */
	public final ExternalTool getTool() {
		return tool;
	}
	
	/**
	 * Returns the workbench window this action
	 * is to be run in.
	 */
	protected final IWorkbenchWindow getWindow() {
		return window;
	}
	
	/**
	 * Opens the console to which messages captured
	 * from the running tool's output will be logged
	 * to.
	 */
	protected final void openLogConsole() {
		IWorkbenchPage page = window.getActivePage();
		try {
			if (page != null) {
				page.showView(IExternalToolConstants.LOG_CONSOLE_VIEW_ID);
			}
		} catch (PartInitException e) {
			ExternalToolsPlugin.getDefault().getLog().log(e.getStatus());
		}
		LogConsoleDocument.getInstance().clearOutput();
	}


	/**
	 * Opens the perspective as spedified by the id.
	 * 
	 * @param perspectiveId the perspective id to open
	 */
	protected final void openPerspective(String perspectiveId) {
		try {
			window.getWorkbench().showPerspective(perspectiveId, window);
		} catch (WorkbenchException e) {
			ErrorDialog.openError(
				window.getShell(), 
				ToolMessages.getString("RunExternalToolAction.openPerspTitle"), //$NON-NLS-1$;
				ToolMessages.getString("RunExternalToolAction.openPerspProblem"), //$NON-NLS-1$;
				e.getStatus());
			ExternalToolsPlugin.getDefault().log(ToolMessages.getString("RunExternalToolAction.openPerspProblem"), e);
		}
	}

	/**
	 * Prompts the user to change the tool's arguments before
	 * running the tool.
	 * 
	 * @return <code>true</code> to continue, or <code>false</code> to stop
	 */
	protected final boolean promptForArguments() {
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on Action.
	 */
	public void run() {
		if (tool == null)
			return;

		// Get the selection now to avoid invalid thread access and current
		// active part changes when perspective and/or console is shown.
		ISelection sel = window.getSelectionService().getSelection();
		IWorkbenchPart activePart = window.getPartService().getActivePart();

		if (alwaysPromptForArguments || tool.getPromptForArguments()) {
			if (!promptForArguments())
				return;
		}

		if (tool.getSaveDirtyEditors())
			saveDirtyEditors();

		if (tool.getOpenPerspective() != null)
			openPerspective(tool.getOpenPerspective());

		if (tool.getShowConsole())
			openLogConsole();
			
		try {
			ToolRunnable runnable = new ToolRunnable(tool, window);
			runnable.selection = sel;
			runnable.activePart = activePart;
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(window.getShell());
			dialog.run(true, true, runnable);
		} catch (InterruptedException e) {
			// Do nothing.
		} catch (InvocationTargetException e) {
			IStatus status = null;
			if (e.getTargetException() instanceof CoreException) {
				status = ((CoreException)e.getTargetException()).getStatus();
			} else {
				String msg = ToolMessages.getString("RunExternalToolAction.internalError"); //$NON-NLS-1$;
				status = ExternalToolsPlugin.newErrorStatus(msg, e.getTargetException());
			}
			ErrorDialog.openError(
				window.getShell(), 
				ToolMessages.getString("RunExternalToolAction.runErrorTitle"), //$NON-NLS-1$;
				ToolMessages.getString("RunExternalToolAction.runProblem"), //$NON-NLS-1$;
				status);
		}

		// Keep track of the most recently run tool.
		FavoritesManager.getInstance().setLastTool(tool);
	}

	/**
	 * Saves all dirty editors.
	 */
	protected final void saveDirtyEditors() {
		IWorkbenchWindow[] windows = window.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				pages[j].saveAllEditors(false);
			}
		}
	}

	/**
	 * Sets the external tool to run or
	 * <code>null</code> to not run any tool.
	 */
	public final void setTool(ExternalTool tool) {
		this.tool = tool;
		setEnabled(tool != null);
	}
	
	
	/**
	 * Helper class that implements the runnable interface
	 * and will run the external tool.
	 */
	private static final class ToolRunnable implements IRunnableWithProgress {
		private IWorkbenchWindow window;
		private ExternalTool tool;
		private IResource selectedResource = null;
		private DefaultRunnerContext context;
		private MultiStatus status;
		protected ISelection selection;
		protected IWorkbenchPart activePart;
		
		public ToolRunnable(ExternalTool tool, IWorkbenchWindow window) {
			super();
			this.tool = tool;
			this.window = window;
		}

		private void determineSelectedResource() {
			if (selection instanceof IStructuredSelection) {
				Object result = ((IStructuredSelection)selection).getFirstElement();
				if (result instanceof IResource) {
					selectedResource = (IResource) result;
				} else if (result instanceof IAdaptable) {
					selectedResource = (IResource)((IAdaptable) result).getAdapter(IResource.class);
				}
			}
			
			if (selectedResource == null) {
				// If the active part is an editor, get the file resource used as input.
				if (activePart instanceof IEditorPart) {
					IEditorPart editorPart = (IEditorPart) activePart;
					IEditorInput input = editorPart.getEditorInput();
					selectedResource = (IResource) input.getAdapter(IResource.class);
				} 
			}
		}
		
		private void displayErrorStatus() {
			ErrorDialog.openError(
				window.getShell(), 
				ToolMessages.getString("RunExternalToolAction.runErrorTitle"), //$NON-NLS-1$;
				null,
				status);
		}
		
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			determineSelectedResource();
			context = new DefaultRunnerContext(tool, selectedResource);
			status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, ToolMessages.getString("RunExternalToolAction.runProblem"), null); //$NON-NLS-1$;
			
			if (tool.getRunInBackground()) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						context.run(new NullProgressMonitor(), status);
						if (!status.isOK()) {
							if (window.getShell() != null && !window.getShell().isDisposed()) {
								window.getShell().getDisplay().syncExec(new Runnable() { 
									public void run() {
										displayErrorStatus();
									}
								});
							}
						}
					}
				});
				thread.start();
			} else {
				context.run(monitor, status);
				if (!status.isOK())
					if (window.getShell() != null && !window.getShell().isDisposed()) {
						window.getShell().getDisplay().syncExec(new Runnable() { 
							public void run() {
								displayErrorStatus();
							}
						});
					}
			}
		};
	}
}
