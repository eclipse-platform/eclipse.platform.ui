package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * A cascading sub-menu that shows all launchers pertinent to this action's mode
 * (e.g., 'run' or 'debug').
 */
public class LaunchWithAction extends Action implements IMenuCreator, IWorkbenchWindowActionDelegate {
	
	private String fMode;
	
	/**
	 * @see IAction#run()
	 */
	public void run() {
		//do nothing 
		//this action just creates a cascading menu.
	}
	
	public LaunchWithAction(String mode) {
		super();
		setMode(mode);
		String text= mode.equals(ILaunchManager.DEBUG_MODE) ? DebugUIMessages.getString("LaunchWithAction.Debug_1") : DebugUIMessages.getString("LaunchWithAction.Run_2"); //$NON-NLS-2$ //$NON-NLS-1$
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
	
	private void createMenuForAction(Menu parent, Action action, int count) {
		//add the numerical accelerator
		StringBuffer label= new StringBuffer("&");
		label.append(count);
		label.append(' ');
		label.append(action.getText());
		action.setText(label.toString());
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	/**
	 * @see IMenuCreator#dispose()
	 */
	public void dispose() {
	}
	/**
	 * @see IMenuCreator#getMenu(Control)
	 */
	public Menu getMenu(Control parent) {
		return null;
	}
	/**
	 * @see IMenuCreator#getMenu(Menu)
	 */
	public Menu getMenu(Menu parent) {
		Object element= null;
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		ILauncher[] launchers= manager.getLaunchers(getMode());
		IStructuredSelection selection = resolveSelection(DebugUIPlugin.getActiveWorkbenchWindow());
		if (selection != null) {
			element= selection.getFirstElement();
		}
	
		Menu menu= new Menu(parent);
		for (int i= 0; i < launchers.length; i++) {
			if (DebugUIPlugin.getDefault().isVisible(launchers[i])) {
				LaunchSelectionAction newAction= new LaunchSelectionAction(launchers[i], element, getMode());
				createMenuForAction(menu, newAction, i+1);
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
	
	protected String getMode() {
		return fMode;
	}
	protected void setMode(String mode) {
		fMode = mode;
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (action instanceof Action) {
			((Action)action).setMenuCreator(this);
		} else {
			action.setEnabled(false);
		}
	}
}


