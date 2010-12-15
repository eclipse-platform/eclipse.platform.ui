/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import com.ibm.icu.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * A <code>ShowViewMenu</code> is used to populate a menu manager with Show
 * View actions. The visible views are determined by user preference from the
 * Perspective Customize dialog.
 */
public class ShowViewMenu extends ContributionItem {

	static class Pair {
		public final Object a;
		public final Object b;
		int hashCode = -1;

		/**
		 * @param a
		 *            must not be <code>null</code>
		 * @param b
		 *            can be <code>null</code>
		 */
		public Pair(Object a, Object b) {
			this.a = a;
			this.b = b;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			if (hashCode == -1) {
				final int prime = 31;
				hashCode = 1;
				hashCode = prime * hashCode + ((a == null) ? 0 : a.hashCode());
				hashCode = prime * hashCode + ((b == null) ? 0 : b.hashCode());
				if (hashCode == -1) {
					hashCode = a.hashCode();
				}
			}
			return hashCode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			Pair p = (Pair) obj;
			if (!a.equals(p.a)) {
				return false;
			}
			if (b == p.b) {
				return true;
			}
			if (b == null || p.b == null) {
				return false;
			}
			return b.equals(p.b);
		}
	}

	private IWorkbenchWindow window;

	private static final String NO_TARGETS_MSG = WorkbenchMessages.Workbench_showInNoTargets;

	private Comparator actionComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			if (collator == null) {
				collator = Collator.getInstance();
			}
			CommandContributionItemParameter a1 = (CommandContributionItemParameter) o1;
			CommandContributionItemParameter a2 = (CommandContributionItemParameter) o2;
			return collator.compare(a1.label, a2.label);
		}
	};

	private Action showDlgAction;

	private Map actions = new HashMap(21);

	protected boolean dirty = true;

	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};
	private boolean makeFast;

	private static Collator collator;


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
	 * @param makeFast use the fact view variant of the command
	 */
	public ShowViewMenu(IWorkbenchWindow window, String id,
			final boolean makeFast) {
		super(id);
		this.window = window;
		this.makeFast = makeFast;
		final IHandlerService handlerService = (IHandlerService) window
				.getService(IHandlerService.class);
		final ICommandService commandService = (ICommandService) window
				.getService(ICommandService.class);
		final ParameterizedCommand cmd = getCommand(commandService, makeFast);

		showDlgAction = new Action(WorkbenchMessages.ShowView_title) {
			public void run() {
				try {
					handlerService.executeCommand(cmd, null);
				} catch (final ExecutionException e) {
					// Do nothing.
				} catch (NotDefinedException e) {
					// Do nothing.
				} catch (NotEnabledException e) {
					// Do nothing.
				} catch (NotHandledException e) {
					// Do nothing.
				}
			}
		};

		window.getWorkbench().getHelpSystem().setHelp(showDlgAction,
				IWorkbenchHelpContextIds.SHOW_VIEW_OTHER_ACTION);
		// indicate that a show views submenu has been created
		if (window instanceof WorkbenchWindow) {
			((WorkbenchWindow) window)
					.addSubmenu(WorkbenchWindow.SHOW_VIEW_SUBMENU);
		}

		showDlgAction.setActionDefinitionId(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW);
		
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
		if (page == null) {
			return;
		}

		// If no active perspective disable all
		if (page.getPerspective() == null) {
			return;
		}

		// Get visible actions.
		List viewIds = getShortcuts(page);

		// add all open views
		viewIds = addOpenedViews(page, viewIds);

		List actions = new ArrayList(viewIds.size());
		for (Iterator i = viewIds.iterator(); i.hasNext();) {
			Pair id = (Pair) i.next();
			if (id.a.equals(IIntroConstants.INTRO_VIEW_ID)) {
				continue;
			}
			CommandContributionItemParameter item = getItem((String) id.a, (String) id.b);
			if (item != null) {
				actions.add(item);
			}
		}
		Collections.sort(actions, actionComparator);
		for (Iterator i = actions.iterator(); i.hasNext();) {
			CommandContributionItemParameter ccip = (CommandContributionItemParameter) i.next();
			if (WorkbenchActivityHelper.filterItem(ccip)) {
				continue;
			}
			CommandContributionItem item = new CommandContributionItem(ccip);
			innerMgr.add(item);
		}

		// We only want to add the separator if there are show view shortcuts,
		// otherwise, there will be a separator and then the 'Other...' entry
		// and that looks weird as the separator is separating nothing
		if (!innerMgr.isEmpty()) {
			innerMgr.add(new Separator());
		}
		
		// Add Other...
		innerMgr.add(showDlgAction);
	}

	private List getShortcuts(IWorkbenchPage page) {
		ArrayList list = new ArrayList();
		String[] shortcuts = page.getShowViewShortcuts();
		for (int i = 0; i < shortcuts.length; i++) {
			list.add(new Pair(shortcuts[i], null));
		}
		return list;
	}

	static class PluginCCIP extends CommandContributionItemParameter implements
			IPluginContribution {

		private String localId;
		private String pluginId;

		public PluginCCIP(IViewDescriptor v, IServiceLocator serviceLocator,
				String id, String commandId, int style) {
			super(serviceLocator, id, commandId, style);
			localId = ((ViewDescriptor) v).getLocalId();
			pluginId = ((ViewDescriptor) v).getPluginId();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IPluginContribution#getLocalId()
		 */
		public String getLocalId() {
			return localId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IPluginContribution#getPluginId()
		 */
		public String getPluginId() {
			return pluginId;
		}

	}

	private CommandContributionItemParameter getItem(String viewId, String secondaryId) {
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		IViewDescriptor desc = reg.find(viewId);
		if (desc==null) {
			return null;
		}
		String label = desc.getLabel();
		
		CommandContributionItemParameter parms = new PluginCCIP(desc,
				window, viewId, IWorkbenchCommandConstants.VIEWS_SHOW_VIEW,
				CommandContributionItem.STYLE_PUSH);
		parms.label = label;
		parms.icon = desc.getImageDescriptor();
		parms.parameters = new HashMap();

		parms.parameters.put(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID, viewId);
		if (makeFast) {
			parms.parameters.put(
					IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_FASTVIEW,
					"true"); //$NON-NLS-1$
		}
		if (secondaryId != null) {
			parms.parameters.put(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_SECONDARY_ID,
					secondaryId);
		}
		return parms;
	}

	private List addOpenedViews(IWorkbenchPage page, List actions) {
		ArrayList views = getParts(page);
		ArrayList result = new ArrayList(views.size() + actions.size());

		for (int i = 0; i < actions.size(); i++) {
			Object element = actions.get(i);
			if (result.indexOf(element) < 0) {
				result.add(element);
			}
		}
		for (int i = 0; i < views.size(); i++) {
			Object element = views.get(i);
			if (result.indexOf(element) < 0) {
				result.add(element);
			}
		}
		return result;
	}

	private ArrayList getParts(IWorkbenchPage page) {
		ArrayList parts = new ArrayList();
		IViewReference[] refs = page.getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			parts.add(new Pair(refs[i].getId(), refs[i].getSecondaryId()));
		}
		return parts;
	}

	public void fill(Menu menu, int index) {
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).addMenuListener(menuListener);
		}

		if (!dirty) {
			return;
		}

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

	/**
	 * @param commandService
	 * @param makeFast
	 */
	private ParameterizedCommand getCommand(ICommandService commandService,
			final boolean makeFast) {
		Command c = commandService.getCommand(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW);
		Parameterization[] parms = null;
		if (makeFast) {
			try {
				IParameter parmDef = c
						.getParameter(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_FASTVIEW);
				parms = new Parameterization[] { new Parameterization(parmDef,
						"true") //$NON-NLS-1$
				};
			} catch (NotDefinedException e) {
				// this should never happen
			}
		}
		return new ParameterizedCommand(c, parms);
	}
}
