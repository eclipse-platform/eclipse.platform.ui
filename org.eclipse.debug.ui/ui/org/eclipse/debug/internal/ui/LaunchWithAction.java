package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * A cascading sub-menu that shows all launchers pertinent to this action's mode
 * (e.g., 'run' or 'debug').
 */
public class LaunchWithAction extends Action implements IMenuCreator {
	
	private static final String PREFIX= "launch_with_action.";	
	protected String fMode;

	/**
	 * @see IAction
	 */
	public void run() {
	}

	public LaunchWithAction(String mode) {
		super();
		fMode= mode;
		String text= mode.equals(ILaunchManager.DEBUG_MODE) ? DebugUIUtils.getResourceString(PREFIX + TEXT + ".debug") : DebugUIUtils.getResourceString(PREFIX + TEXT + ".run");
		setText(text);
		ImageDescriptor descriptor= null;
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG);
		} else {
			descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN);
		}

		if (descriptor != null) {
			setImageDescriptor(descriptor);
		}
		setMenuCreator(this);
	}
	
	private void createMenuForAction(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	/**
	 * @see IMenuCreator
	 */
	public void dispose() {
	}

	/**
	 * @see IMenuCreator
	 */
	public Menu getMenu(Control parent) {
		return null;
	}

	/**
	 * @see IMenuCreator
	 */
	public Menu getMenu(Menu parent) {
		Object element= null;
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		ILauncher[] launchers= manager.getLaunchers(fMode);
		IStructuredSelection selection = resolveSelection(DebugUIPlugin.getActiveWorkbenchWindow());
		if (selection != null) {
			element= selection.getFirstElement();
		}
	
		Menu menu= new Menu(parent);
		for (int i= 0; i < launchers.length; i++) {
			if (DebugUIPlugin.getDefault().isVisible(launchers[i])) {
				LaunchSelectionAction newAction= new LaunchSelectionAction(launchers[i], element, fMode);
				createMenuForAction(menu, newAction);
			}
		}
		return menu;
	}
	
	/**
	 * Determines and returns the selection that provides context for the launch,
	 * or <code>null</code> if there is no selection.
	 */
	protected IStructuredSelection resolveSelection(IWorkbenchWindow window) {
		if (window == null) {
			return null;
		}
		ISelection selection= window.getSelectionService().getSelection();
		if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			// there is no obvious selection - go fishing
			selection= null;
			IWorkbenchPage p= window.getActivePage();
			if (p == null) {
				//workspace is closed
				return null;
			}
			IEditorPart editor= p.getActiveEditor();
			Object element= null;

			// first, see if there is an active editor, and try its input element
			if (editor != null) {
				element= editor.getEditorInput();
			}

			if (selection == null && element != null) {
				selection= new StructuredSelection(element);
			}
		}
		return (IStructuredSelection)selection;
	}
}


