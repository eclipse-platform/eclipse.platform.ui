package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.Collator;
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;

/**
 * A <code>ShowViewMenu</code> is used to populate a menu manager with
 * Show View actions.  The visible views are determined by user preference
 * from the Perspective Customize dialog. 
 */
public class ShowViewMenu extends ShortcutMenu implements IPartListener {
	
	private Comparator actionComparator = new Comparator() {
		Collator collator = Collator.getInstance();
		public int compare(Object o1, Object o2) {
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
	public ShowViewMenu(
		IMenuManager innerMgr,
		IWorkbenchWindow window,
		boolean register) {
		super(innerMgr, window, register);
		fillMenu(); // Must be done after constructor to ensure field initialization.
	}
	
	/* (non-Javadoc)
	 * Fills the menu with views.
	 */
	protected void fillMenu() {
		// Remove all.
		IMenuManager innerMgr = getMenuManager();
		innerMgr.removeAll();

		// If no page disable all.
		IWorkbenchPage page = getWindow().getActivePage();
		if (page == null)
			return;

		// If no active perspective disable all
		if (page.getPerspective() == null)
			return;

		// Get visible actions.
		List viewIds = ((WorkbenchPage) page).getShowViewActions();
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
				action = new ShowViewAction(getWindow(), desc);
				actions.put(id, action);
			}
		}
		return action;
	}
	
	/**
	 * Opens the view selection dialog.
	 */
	private void showOther() {
		IWorkbenchWindow window = getWindow();
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
		IViewDescriptor desc = dlg.getSelection();
		if (desc != null) {
			try {
				page.showView(desc.getID());
			} catch (PartInitException e) {
				MessageDialog
					.openError(
						window.getShell(),
						WorkbenchMessages.getString("ShowView.errorTitle"), //$NON-NLS-1$
				//$NON-NLS-1$
				e.getMessage());
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

	public void partActivated(IWorkbenchPart part) {
	}
	
	public void partBroughtToTop(IWorkbenchPart part) {
	}
	
	public void partClosed(IWorkbenchPart part) {
	}
	
	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
		if (part instanceof IViewPart) {
			String id = ((IViewPart) part).getSite().getId();
			ArrayList parts = getParts(part.getSite().getPage());
			if (parts.indexOf(id) < 0) {
				parts.add(id);
				updateMenu();
			}
		}
	}
	public void pageClosed(IWorkbenchPage page) {
		openedViews.remove(page);
		super.pageClosed(page);
	}
	
	public void pageOpened(IWorkbenchPage page) {
		ArrayList parts = getParts(page); // adds an entry to openedViews
		IViewPart[] views = page.getViews();
		for (int i = 0; i < views.length; i++) {
			String id = views[i].getSite().getId();
			if (parts.indexOf(id) < 0)
				parts.add(id);
		}
		page.addPartListener(this);
		super.pageOpened(page);
	}

}