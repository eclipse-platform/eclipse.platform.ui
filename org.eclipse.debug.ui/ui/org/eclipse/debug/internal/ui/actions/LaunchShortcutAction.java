package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Launch shortcut action (proxy to a launch shortcut extension)
 */
public class LaunchShortcutAction extends Action {
	
	private String fMode;
	private LaunchShortcutExtension fShortcut; 


	/**
	 * Constructor for LaunchShortcutAction.
	 */
	public LaunchShortcutAction(String mode, LaunchShortcutExtension shortcut) {
		super(shortcut.getLabel(), shortcut.getImageDescriptor());
		fMode = mode;
		fShortcut = shortcut;
	}
	
	

	/**
	 * Runs with either the active editor or workbench selection.
	 * 
	 * @see IAction#run()
	 */
	public void run() {
		IWorkbenchWindow wb = DebugUIPlugin.getActiveWorkbenchWindow();
		if (wb != null) {
			IWorkbenchPage page = wb.getActivePage();
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				fShortcut.launch(selection, fMode);
			} else {
				IEditorPart editor = page.getActiveEditor();
				if (editor != null) {
					fShortcut.launch(editor, fMode);
				}
			}
		}
	}

}
