/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.text.Collator;
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;
import org.eclipse.ui.internal.registry.*;

/**
 * A <code>ShowViewMenu</code> is used to populate a menu manager with
 * Show View actions.  The visible views are determined by user preference
 * from the Perspective Customize dialog. 
 */
public class ShowViewMenu extends ContributionItem {
	
	private IWorkbenchWindow window;
		
	private Comparator actionComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			if(collator == null)
		 		collator = Collator.getInstance();		
			IAction a1 = (IAction) o1;
			IAction a2 = (IAction) o2;
			return collator.compare(a1.getText(), a2.getText());
		}
	};

	private Action showDlgAction =
		new Action(WorkbenchMessages.getString("ShowView.title")) {//$NON-NLS-1$
			public void run() {
				showOther();
			}
		};

	private Map actions = new HashMap(21);

	//Maps pages to a list of opened views
	private Map openedViews = new HashMap();
	
	private boolean dirty = true;
	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};
	
	private static Collator collator;	

	/**
	 * Create a show view menu.
	 * <p>
	 * If the menu will appear on a semi-permanent basis, for instance within
	 * a toolbar or menubar, the value passed for <code>register</code> should be true.
	 * If set, the menu will listen to perspective activation and update itself
	 * to suit.  In this case clients are expected to call <code>deregister</code> 
	 * when the menu is no longer needed.  This will unhook any perspective
	 * listeners.
	 * </p>
	 *
	 * @param innerMgr the location for the shortcut menu contents
	 * @param window the window containing the menu
	 * @param register if <code>true</code> the menu listens to perspective changes in
	 * 		the window
	 */
	public ShowViewMenu(IWorkbenchWindow window) {
		this.window = window;
	}
	
	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDirty() {
		return dirty;
	}	
	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDynamic() {
		return true;
	}
	
	/* (non-Javadoc)
	 * Fills the menu with views.
	 */
	private void fillMenu(IMenuManager innerMgr) {
		// Remove all.
		innerMgr.removeAll();

		// If no page disable all.
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return;

		// If no active perspective disable all
		if (page.getPerspective() == null)
			return;

		// Get visible actions.
		List viewIds = ((WorkbenchPage) page).getShowViewActionIds();
		viewIds = addOpenedViews(page, viewIds);
		List actions = new ArrayList(viewIds.size());
		for (Iterator i = viewIds.iterator(); i.hasNext();) {
			String id = (String) i.next();
			IAction action = getAction(id);
			if (action != null) {
				actions.add(action);
			}
		}
		Collections.sort(actions, actionComparator);
		for (Iterator i = actions.iterator(); i.hasNext();) {
			innerMgr.add((IAction) i.next());
		}

		// Add Other ..
		innerMgr.add(new Separator());
		innerMgr.add(showDlgAction);
	}

	private List addOpenedViews(IWorkbenchPage page, List actions) {
		ArrayList views = getParts(page);
		ArrayList result = new ArrayList(views.size() + actions.size());

		for (int i = 0; i < actions.size(); i++) {
			Object element = actions.get(i);
			if (result.indexOf(element) < 0)
				result.add(element);
		}
		for (int i = 0; i < views.size(); i++) {
			Object element = views.get(i);
			if (result.indexOf(element) < 0)
				result.add(element);
		}
		return result;
	}
	/**
	 * Returns the action for the given view id, or null if not found.
	 */
	private IAction getAction(String id) {
		// Keep a cache, rather than creating a new action each time,
		// so that image caching in ActionContributionItem works.
		IAction action = (IAction) actions.get(id);
		if (action == null) {
			IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
			IViewDescriptor desc = reg.find(id);
			if (desc != null) {
				action = new ShowViewAction(window, desc);
				actions.put(id, action);
			}
		}
		return action;
	}
	
	/**
	 * Opens the view selection dialog.
	 */
	private void showOther() {
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return;
		ShowViewDialog dlg =
			new ShowViewDialog(
				window.getShell(),
				WorkbenchPlugin.getDefault().getViewRegistry());
		dlg.open();
		if (dlg.getReturnCode() == Window.CANCEL)
			return;
		IViewDescriptor[] descs = dlg.getSelection();
		for (int i = 0; i < descs.length; ++i) {
			try {
				page.showView(descs[i].getID());
			} catch (PartInitException e) {
				ErrorDialog
					.openError(
						window.getShell(),
						WorkbenchMessages.getString("ShowView.errorTitle"), //$NON-NLS-1$
						e.getMessage(),
						e.getStatus());
			}
		}
	}

	private ArrayList getParts(IWorkbenchPage page) {
		ArrayList parts = (ArrayList) openedViews.get(page);
		if (parts == null) {
			parts = new ArrayList();
			openedViews.put(page, parts);
		}
		return parts;
	}
	
public void fill(Menu menu, int index) {
	if(getParent() instanceof MenuManager)
		((MenuManager)getParent()).addMenuListener(menuListener);

	if(!dirty)
		return;

	MenuManager manager = new MenuManager();
	fillMenu(manager);
	IContributionItem items[] = manager.getItems();
	for (int i = 0; i < items.length; i++) {
		items[i].fill(menu,index++);
	}
	dirty = false;
}
	
}