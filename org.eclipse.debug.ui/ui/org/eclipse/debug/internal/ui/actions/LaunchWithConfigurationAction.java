package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * A cascading sub-menu that shows all launch configuration types pertinent to this action's mode
 * (e.g., 'run' or 'debug').
 */
public abstract class LaunchWithConfigurationAction extends Action implements IMenuCreator, 
																				   IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow fWorkbenchWindow;
	private List fActionItems;
	private IAction fAction;
	private ILaunchManager fLaunchManager;
	
	/**
	 * Comparator used to sort ILaunchConfigurationType objects into alphabetical order of their names.
	 */
	private class LaunchConfigurationTypeAlphabeticComparator implements Comparator {
		public int compare(Object obj1, Object obj2) {
			String name1 = ((ILaunchConfigurationType)obj1).getName();
			String name2 = ((ILaunchConfigurationType)obj2).getName();
			return name1.compareTo(name2);
		}
	}
	
	/**
	 * @see IAction#run()
	 */
	public void run() {
		//do nothing, this action just creates a cascading menu.
	}
	
	public LaunchWithConfigurationAction() {
		super();
		setText(getLabelText());
		setMenuCreator(this);
	}
	
	private void createMenuForAction(Menu parent, Action action, int count) {
		StringBuffer label= new StringBuffer();
		//add the numerical accelerator
		if (count < 10) {
			label.append('&');
			label.append(count);
			label.append(' ');
		}
		label.append(action.getText());
		action.setText(label.toString());
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
		if (getActionItems() != null) {
			//have to do our own menu updating
			getActionItems().add(item);
		}
	}
	
	/**
	 * @see IMenuCreator#dispose()
	 */
	public void dispose() {
		setActionItems(null);
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
		
		// Retrieve the current perspective and the registered shortcuts
		List shortcuts = null;
		String activePerspID = getActivePerspectiveID();
		if (activePerspID != null) {
			shortcuts = LaunchConfigurationManager.getDefault().getLaunchShortcuts(activePerspID);
		}
		
		// If NO shortcuts are listed in the current perspective, add ALL shortcuts
		// to avoid an empty cascading menu
		if (shortcuts == null || shortcuts.isEmpty()) {
			shortcuts = LaunchConfigurationManager.getDefault().getLaunchShortcuts();
		}
		
		// Sort the applicable config types alphabetically and add them to the menu
		Menu menu= new Menu(parent);
		int menuCount = 1;
		Iterator iter = shortcuts.iterator();
		while (iter.hasNext()) {
			LaunchShortcutExtension ext = (LaunchShortcutExtension) iter.next();
			if (ext.getModes().contains(getMode())) {
				populateMenu(ext, menu, menuCount);
				menuCount++;
			}
		}
				
		return menu;
	}
	
	/**
	 * Add the shortcut to the menu.
	 */
	protected void populateMenu(LaunchShortcutExtension ext, Menu menu, int menuCount) {
		LaunchShortcutAction action = new LaunchShortcutAction(getMode(), ext);
		createMenuForAction(menu, action, menuCount);
	}
	
	/**
	 * Return the ID of the currently active perspective, or <code>null</code>
	 * if there is none.
	 */
	protected String getActivePerspectiveID() {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IPerspectiveDescriptor persp = page.getPerspective();
				if (persp != null) {
					return persp.getId();
				}
			}
		}
		return null;
	}
	
	/**
	 * Determines and returns the selected element that provides context for the launch,
	 * or <code>null</code> if there is no selection.
	 */
	protected Object resolveSelectedElement(IWorkbenchWindow window) {
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
			return element;
		}
		return ((IStructuredSelection)selection).getFirstElement();
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		setWorkbenchWindow(window);
		setActionItems(new ArrayList(5));
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
			if (fAction == null) {
				((Action)action).setMenuCreator(this);
				fAction = action;				
			}
		} else {
			action.setEnabled(false);
		}
	}
	
	protected List getActionItems() {
		return fActionItems;
	}

	protected void setActionItems(List actionItems) {
		fActionItems = actionItems;
	}
	
	protected void setWorkbenchWindow(IWorkbenchWindow window) {
		fWorkbenchWindow = window;
	}
	
	protected IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}
	
	/**
	 * Lazily populate & return the current launch manager.  Lazy population is useful because this
	 * can be called many times.
	 */
	protected ILaunchManager getLaunchManager() {
		if (fLaunchManager == null) {
			fLaunchManager = DebugPlugin.getDefault().getLaunchManager();
		}
		return fLaunchManager;
	}
	
	/**
	 * Implemented to return one of the constants defined in <code>ILaunchManager</code>
	 * that specifies the launch mode. 
	 */
	public abstract String getMode();
	
	/**
	 * Return a String label for this action.
	 */
	public abstract String getLabelText();
}
