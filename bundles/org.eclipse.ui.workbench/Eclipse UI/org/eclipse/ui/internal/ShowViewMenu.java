/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPluginContribution;
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
 * A <code>ShowViewMenu</code> is used to populate a menu manager with Show View
 * actions. The visible views are determined by user preference from the
 * Perspective Customize dialog.
 */
public class ShowViewMenu extends ContributionItem {

	private IWorkbenchWindow window;

	private static final String NO_TARGETS_MSG = WorkbenchMessages.Workbench_showInNoTargets;

	private Comparator<CommandContributionItemParameter> actionComparator = (o1, o2) -> {
		if (collator == null) {
			collator = Collator.getInstance();
		}
		return collator.compare(o1.label, o2.label);
	};

	private Action showDlgAction;

	private Map<String, ?> actions = new HashMap<>(21);

	// Maps pages to a list of opened views
	private Map<IWorkbenchPage, ArrayList<String>> openedViews = new HashMap<>();

	private MenuManager menuManager;

	private IMenuListener menuListener = IMenuManager::markDirty;
	private boolean makeFast;

	private static Collator collator;

	/**
	 * Creates a Show View menu.
	 *
	 * @param window the window containing the menu
	 * @param id     the id
	 */
	public ShowViewMenu(IWorkbenchWindow window, String id) {
		this(window, id, false);
	}

	/**
	 * Creates a Show View menu.
	 *
	 * @param window   the window containing the menu
	 * @param id       the id
	 * @param makeFast use the fact view variant of the command
	 */
	public ShowViewMenu(IWorkbenchWindow window, String id, final boolean makeFast) {
		super(id);
		this.window = window;
		this.makeFast = makeFast;
		final IHandlerService handlerService = window.getService(IHandlerService.class);
		final ICommandService commandService = window.getService(ICommandService.class);
		final ParameterizedCommand cmd = getCommand(commandService, makeFast);

		showDlgAction = new Action(WorkbenchMessages.ShowView_title) {
			@Override
			public void run() {
				try {
					handlerService.executeCommand(cmd, null);
				} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
					// Do nothing.
				}
			}
		};

		window.getWorkbench().getHelpSystem().setHelp(showDlgAction, IWorkbenchHelpContextIds.SHOW_VIEW_OTHER_ACTION);
		// indicate that a show views submenu has been created
		if (window instanceof WorkbenchWindow) {
			((WorkbenchWindow) window).addSubmenu(WorkbenchWindow.SHOW_VIEW_SUBMENU);
		}

		showDlgAction.setActionDefinitionId(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW);

	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	@Override
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
		List<String> viewIds = Arrays.asList(page.getShowViewShortcuts());

		// add all open views
		viewIds = addOpenedViews(page, viewIds);

		List<CommandContributionItemParameter> actions = new ArrayList<>(viewIds.size());
		for (Iterator<String> i = viewIds.iterator(); i.hasNext();) {
			String id = i.next();
			if (id.equals(IIntroConstants.INTRO_VIEW_ID)) {
				continue;
			}
			CommandContributionItemParameter item = getItem(id);
			if (item != null) {
				actions.add(item);
			}
		}
		actions.sort(actionComparator);
		for (Iterator<CommandContributionItemParameter> i = actions.iterator(); i.hasNext();) {
			CommandContributionItemParameter ccip = i.next();
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

	static class PluginCCIP extends CommandContributionItemParameter implements IPluginContribution {

		private String localId;
		private String pluginId;

		public PluginCCIP(IViewDescriptor v, IServiceLocator serviceLocator, String id, String commandId, int style) {
			super(serviceLocator, id, commandId, style);
			localId = ((ViewDescriptor) v).getLocalId();
			pluginId = ((ViewDescriptor) v).getPluginId();
		}

		public PluginCCIP(MPartDescriptor partDesc, IServiceLocator serviceLocator, String id, String commandId,
				int style) {
			super(serviceLocator, id, commandId, style);
			localId = partDesc.getElementId();
			pluginId = null;
		}

		@Override
		public String getLocalId() {
			return localId;
		}

		@Override
		public String getPluginId() {
			return pluginId;
		}

	}

	@SuppressWarnings("unchecked")
	private CommandContributionItemParameter getItem(String viewId) {
		MApplication application = window.getService(MApplication.class);
		List<MPartDescriptor> descriptors = application.getDescriptors();
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();

		MPartDescriptor partDesc = descriptors.stream().filter(descriptor -> descriptor.getElementId().equals(viewId))
				.findFirst().orElse(null);

		if (partDesc == null) {
			return null;
		}
		String label = partDesc.getLabel();

		CommandContributionItemParameter parms;

		IViewDescriptor desc = reg.find(viewId);
		if (desc != null) {
			parms = new PluginCCIP(desc, window, viewId,
					IWorkbenchCommandConstants.VIEWS_SHOW_VIEW, CommandContributionItem.STYLE_PUSH);
		} else {
			parms = new PluginCCIP(partDesc, window, viewId,
					IWorkbenchCommandConstants.VIEWS_SHOW_VIEW, CommandContributionItem.STYLE_PUSH);
		}

		parms.label = label;
		parms.icon = getImage(partDesc);
		parms.parameters = new HashMap<String, String>();

		parms.parameters.put(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID, viewId);
		if (makeFast) {
			parms.parameters.put(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_FASTVIEW, "true"); //$NON-NLS-1$
		}
		return parms;
	}

	private ImageDescriptor getImage(MPartDescriptor element) {
		String iconURI = element.getIconURI();
		if (iconURI != null && !iconURI.isBlank()) {
			ISWTResourceUtilities resUtils = (ISWTResourceUtilities) window.getService(IResourceUtilities.class);
			return resUtils.imageDescriptorFromURI(URI.createURI(iconURI));
		}
		return null;
	}

	private List<String> addOpenedViews(IWorkbenchPage page, List<String> actions) {
		ArrayList<String> views = getParts(page);
		ArrayList<String> result = new ArrayList<>(views.size() + actions.size());

		for (String element : actions) {
			if (result.indexOf(element) < 0) {
				result.add(element);
			}
		}
		for (int i = 0; i < views.size(); i++) {
			String element = views.get(i);
			if (result.indexOf(element) < 0) {
				result.add(element);
			}
		}
		return result;
	}

	private ArrayList<String> getParts(IWorkbenchPage page) {
		ArrayList<String> parts = openedViews.get(page);
		if (parts == null) {
			parts = new ArrayList<>();
			openedViews.put(page, parts);
		}
		return parts;
	}

	@Override
	public void fill(Menu menu, int index) {
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).addMenuListener(menuListener);
		}

		if (menuManager != null) {
			menuManager.dispose();
			menuManager = null;
		}

		menuManager = new MenuManager();
		fillMenu(menuManager);
		IContributionItem items[] = menuManager.getItems();
		if (items.length == 0) {
			MenuItem item = new MenuItem(menu, SWT.NONE, index++);
			item.setText(NO_TARGETS_MSG);
			item.setEnabled(false);
		} else {
			for (IContributionItem item : items) {
				item.fill(menu, index++);
			}
		}
	}

	// for dynamic UI
	protected void removeAction(String viewId) {
		actions.remove(viewId);
	}

	/**
	 * @param commandService
	 * @param makeFast
	 */
	private ParameterizedCommand getCommand(ICommandService commandService, final boolean makeFast) {
		Command c = commandService.getCommand(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW);
		Parameterization[] parms = null;
		if (makeFast) {
			try {
				IParameter parmDef = c.getParameter(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_FASTVIEW);
				parms = new Parameterization[] { new Parameterization(parmDef, "true") //$NON-NLS-1$
				};
			} catch (NotDefinedException e) {
				// this should never happen
			}
		}
		return new ParameterizedCommand(c, parms);
	}

	@Override
	public void dispose() {
		if (menuManager != null) {
			menuManager.dispose();
			menuManager = null;
		}
		openedViews.clear();
		window = null;
		super.dispose();
	}
}
