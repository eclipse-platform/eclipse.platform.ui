/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.handlers.ShowViewHandler;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * A <code>ShowViewMenu</code> is used to populate a menu manager with Show
 * View actions. The visible views are determined by user preference from the
 * Perspective Customize dialog.
 */
public class ShowViewMenu extends ContributionItem {

	private IWorkbenchWindow window;

	private static final String NO_TARGETS_MSG = WorkbenchMessages.Workbench_showInNoTargets;

	private Comparator actionComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			if (collator == null)
				collator = Collator.getInstance();
			IAction a1 = (IAction) o1;
			IAction a2 = (IAction) o2;
			return collator.compare(a1.getText(), a2.getText());
		}
	};

	private Action showDlgAction;

	private Map actions = new HashMap(21);

	// Maps pages to a list of opened views
	private Map openedViews = new HashMap();

	protected boolean dirty = true;

	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};

	protected static Collator collator;
    private boolean makeFast;

    /**
     * Creates a Show View menu.
     * 
     * @param window
     *            the window containing the menu
     * @param id
     *            the id
     */
    public ShowViewMenu(IWorkbenchWindow window, String id) {
        this(window, id, false);
    }
    
	/**
	 * Creates a Show View menu.
	 * 
	 * @param window
	 *            the window containing the menu
	 * @param id
	 *            the id
	 */
	public ShowViewMenu(IWorkbenchWindow window, String id, final boolean makeFast) {
		super(id);
		this.window = window;
        showDlgAction = new Action(WorkbenchMessages.ShowView_title) {
            final ShowViewHandler handler = new ShowViewHandler(makeFast);

            public void run() {
                try {
                    handler.execute(new ExecutionEvent());
                } catch (final ExecutionException e) {
                    // Do nothing.
                }
            }
        };
        
        window.getWorkbench().getHelpSystem().setHelp(showDlgAction,
				IWorkbenchHelpContextIds.SHOW_VIEW_OTHER_ACTION);
		// indicate that a show views submenu has been created
		((WorkbenchWindow) window)
				.addSubmenu(WorkbenchWindow.SHOW_VIEW_SUBMENU);
        
		showDlgAction.setActionDefinitionId("org.eclipse.ui.views.showView"); //$NON-NLS-1$
        this.makeFast = makeFast;
	}

	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Fills the menu with Show View actions.
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
		List viewIds = Arrays.asList(page.getShowViewShortcuts());

		// add all open views
		viewIds = addOpenedViews(page, viewIds);

		List actions = new ArrayList(viewIds.size());
		for (Iterator i = viewIds.iterator(); i.hasNext();) {
			String id = (String) i.next();
			if (id.equals(IIntroConstants.INTRO_VIEW_ID))
				continue;
			IAction action = getAction(id);
			if (action != null) {
				if (WorkbenchActivityHelper.filterItem(action))
					continue;
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
				action = new ShowViewAction(window, desc, makeFast);
				action.setActionDefinitionId(id);
				actions.put(id, action);
			}
		}
		return action;
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
		if (getParent() instanceof MenuManager)
			((MenuManager) getParent()).addMenuListener(menuListener);

		if (!dirty)
			return;

		MenuManager manager = new MenuManager();
		fillMenu(manager);
		IContributionItem items[] = manager.getItems();
		if (items.length == 0) {
			MenuItem item = new MenuItem(menu, SWT.NONE, index++);
			item.setText(NO_TARGETS_MSG);
			item.setEnabled(false);
		} else {
			for (int i = 0; i < items.length; i++) {
				items[i].fill(menu, index++);
			}
		}
		dirty = false;
	}

	// for dynamic UI
	protected void removeAction(String viewId) {
		actions.remove(viewId);
	}

}
