/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440810, 444070, 472654
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 451214
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.WorkbenchSourceProvider;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.menus.MenuUtil;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * A <code>ShowInMenu</code> is used to populate a menu manager with Show In
 * actions. The items to show are determined from the active perspective and
 * active part.
 */
public class ShowInMenu extends ContributionItem implements
		IWorkbenchContribution {

	private static final String NO_TARGETS_MSG = WorkbenchMessages.Workbench_showInNoTargets;

	private IWorkbenchWindow window;

	private boolean dirty = true;

	private IMenuListener menuListener = manager -> {
		manager.markDirty();
		dirty = true;
	};

	private IServiceLocator locator;

	private MenuManager currentManager;

	public ShowInMenu() {

	}

	/**
	 * Creates a Show In menu.
	 *
	 * @param window
	 *            the window containing the menu
	 * @param id
	 *            The ID for this contribution
	 */
	public ShowInMenu(IWorkbenchWindow window, String id) {
		super(id);
		this.window = window;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public void fill(Menu menu, int index) {
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).addMenuListener(menuListener);
		}

		if (!dirty) {
			return;
		}

		if (currentManager!=null && currentManager.getSize() > 0) {
			// IMenuService service = (IMenuService) locator
			// .getService(IMenuService.class);
			// service.releaseContributions(currentManager);
			currentManager.removeAll();
		}

		currentManager = new MenuManager();
		fillMenu(currentManager);
		int itemCount = menu.getItemCount();
		IContributionItem[] items = currentManager.getItems();
		if (items.length == 0) {
			MenuItem item = new MenuItem(menu, SWT.NONE, index == -1 ? itemCount : index);
			item.setText(NO_TARGETS_MSG);
			item.setEnabled(false);
		} else {
			for (IContributionItem item : items) {
				if (item.isVisible()) {
					if (index == -1) {
						item.fill(menu, -1);
					} else {
						item.fill(menu, index);
						int newItemCount = menu.getItemCount();
						index += newItemCount - itemCount;
						itemCount = newItemCount;
					}
				}
			}
		}
		dirty = false;
	}

	/**
	 * Fills the menu with Show In actions.
	 */
	private void fillMenu(IMenuManager innerMgr) {
		IWorkbenchPage page = locator.getService(IWorkbenchPage.class);
		if (page == null) {
			return;
		}
		WorkbenchPartReference r = (WorkbenchPartReference) page.getActivePartReference();
		if (page != null && r != null && r.getModel() != null) {
			((WorkbenchPage) page).updateShowInSources(r.getModel());
		}

		// Remove all.
		innerMgr.removeAll();

		IWorkbenchPart sourcePart = getSourcePart();
		ShowInContext context = getContext(sourcePart);
		if (context == null) {
			return;
		}
		if (context.getInput() == null
				&& (context.getSelection() == null || context.getSelection()
						.isEmpty())) {
			return;
		}

		IViewDescriptor[] viewDescs = getViewDescriptors(sourcePart);
		for (IViewDescriptor viewDesc : viewDescs) {
			IContributionItem cci = getContributionItem(viewDesc);
			if (cci != null) {
				innerMgr.add(cci);
			}
		}
		if (sourcePart != null && innerMgr instanceof MenuManager) {
			ISourceProviderService sps = locator
					.getService(ISourceProviderService.class);
			ISourceProvider sp = sps
					.getSourceProvider(ISources.SHOW_IN_SELECTION);
			if (sp instanceof WorkbenchSourceProvider) {
				((WorkbenchSourceProvider) sp).checkActivePart(true);
			}

			// add contributions targeting popup:org.eclipse.ui.menus.showInMenu
			String location = MenuUtil.SHOW_IN_MENU_ID;
			location = location.substring(location.indexOf(':') + 1);
			WorkbenchWindow workbenchWindow = (WorkbenchWindow) getWindow();
			MApplication application = workbenchWindow.getModel().getContext()
					.get(MApplication.class);

			MMenu menuModel = MenuFactoryImpl.eINSTANCE.createMenu();
			final ArrayList<MMenuContribution> toContribute = new ArrayList<>();
			final ArrayList<MMenuElement> menuContributionsToRemove = new ArrayList<>();
			ExpressionContext eContext = new ExpressionContext(workbenchWindow.getModel()
					.getContext());
			ContributionsAnalyzer.gatherMenuContributions(menuModel,
					application.getMenuContributions(), location, toContribute, eContext, true);
			ContributionsAnalyzer.addMenuContributions(menuModel, toContribute,
					menuContributionsToRemove);

			ICommandImageService imgService = workbenchWindow.getService(ICommandImageService.class);

			for (MMenuElement menuElement : menuModel.getChildren()) {
				if (menuElement instanceof MHandledMenuItem) {
					MCommand command = ((MHandledMenuItem) menuElement).getCommand();
					String commandId = command.getElementId();
					CommandContributionItemParameter ccip = new CommandContributionItemParameter(
							workbenchWindow, commandId, commandId,
							CommandContributionItem.STYLE_PUSH);
					String label = menuElement.getLabel();
					if (label != null && label.length() > 0) {
						ccip.label = label;
						String mnemonics = menuElement.getMnemonics();
						if (mnemonics != null && mnemonics.length() == 1) {
							ccip.mnemonic = mnemonics;
						} else {
							ccip.mnemonic = label.substring(0, 1);
						}
					}
					String iconURI = menuElement.getIconURI();
					try {
						if (iconURI != null && !iconURI.isEmpty()) {
							ccip.icon = ImageDescriptor.createFromURL(new URL(iconURI));
						} else {
							ccip.icon = imgService.getImageDescriptor(commandId);
						}
					} catch (MalformedURLException e) {
						ccip.icon = imgService.getImageDescriptor(commandId);
					}
					innerMgr.add(new CommandContributionItem(ccip));
				}
			}
		}
	}

	/**
	 * Return the appropriate command contribution item for the parameter.
	 * @param viewDescriptor
	 * @return the show in command contribution item
	 */
	protected IContributionItem getContributionItem(IViewDescriptor viewDescriptor) {
		CommandContributionItemParameter parm = new CommandContributionItemParameter(
				locator, viewDescriptor.getId(), IWorkbenchCommandConstants.NAVIGATE_SHOW_IN,
				CommandContributionItem.STYLE_PUSH);
		HashMap<String, String> targetId = new HashMap<>();
		targetId.put(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET,
				viewDescriptor.getId());
		parm.parameters = targetId;
		parm.label = viewDescriptor.getLabel();
		if (parm.label.length() > 0) {
			parm.mnemonic = parm.label.substring(0, 1);
		}
		parm.icon = viewDescriptor.getImageDescriptor();
		return new CommandContributionItem(parm);
	}

	/**
	 * Returns the Show In... target part ids for the given source part. Merges
	 * the contributions from the current perspective and the source part.
	 */
	private ArrayList<Object> getShowInPartIds(IWorkbenchPart sourcePart) {
		ArrayList<Object> targetIds = new ArrayList<>();
		WorkbenchPage page = (WorkbenchPage) getWindow().getActivePage();
		if (page != null) {
			String srcId = sourcePart == null ? null : sourcePart.getSite().getId();
			ArrayList<?> pagePartIds = page.getShowInPartIds();
			for (Object pagePartId : pagePartIds) {
				// Don't add own view, except when explicitly requested with
				// IShowInTargetList below
				if (!pagePartId.equals(srcId)) {
					targetIds.add(pagePartId);
				}
			}
		}
		IShowInTargetList targetList = Adapters.adapt(sourcePart, IShowInTargetList.class);
		if (targetList != null) {
			String[] partIds = targetList.getShowInTargetIds();
			if (partIds != null) {
				for (int i = 0; i < partIds.length; ++i) {
					if (!targetIds.contains(partIds[i])) {
						targetIds.add(partIds[i]);
					}
				}
			}
		}
		page.sortShowInPartIds(targetIds);
		return targetIds;
	}

	/**
	 * Returns the source part, or <code>null</code> if there is no applicable
	 * source part
	 * <p>
	 * This implementation returns the current part in the window. Subclasses
	 * may extend or reimplement.
	 *
	 * @return the source part or <code>null</code>
	 */
	protected IWorkbenchPart getSourcePart() {
		IWorkbenchWindow window = getWindow();

		if (window == null)
			return null;

		IWorkbenchPage page = window.getActivePage();
		return page != null ? page.getActivePart() : null;
	}

	/**
	 * Returns the <code>ShowInContext</code> to show in the selected target,
	 * or <code>null</code> if there is no valid context to show.
	 * <p>
	 * This implementation obtains the context from the
	 * <code>IShowInSource</code> of the source part (if provided), or, if the
	 * source part is an editor, it creates the context from the editor's input
	 * and selection.
	 * <p>
	 * Subclasses may extend or reimplement.
	 *
	 * @return the <code>ShowInContext</code> to show or <code>null</code>
	 */
	protected ShowInContext getContext(IWorkbenchPart sourcePart) {
		if (sourcePart != null) {
			IShowInSource source = Adapters.adapt(sourcePart, IShowInSource.class);
			if (source != null) {
				ShowInContext context = source.getShowInContext();
				if (context != null) {
					return context;
				}
			} else if (sourcePart instanceof IEditorPart) {
				Object input = ((IEditorPart) sourcePart).getEditorInput();
				ISelectionProvider sp = sourcePart.getSite().getSelectionProvider();
				ISelection sel = sp == null ? null : sp.getSelection();
				return new ShowInContext(input, sel);
			}
		}
		return null;
	}

	/**
	 * Returns the view descriptors to show in the dialog.
	 */
	private IViewDescriptor[] getViewDescriptors(IWorkbenchPart sourcePart) {
		ArrayList<Object> ids = getShowInPartIds(sourcePart);
		ArrayList<IViewDescriptor> descs = new ArrayList<>();
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		for (Object object : ids) {
			String id = (String) object;
			IViewDescriptor desc = reg.find(id);
			if (desc != null) {
				descs.add(desc);
			}
		}
		return descs.toArray(new IViewDescriptor[descs
				.size()]);
	}

	@Override
	public void initialize(IServiceLocator serviceLocator) {
		locator = serviceLocator;
	}

	protected IWorkbenchWindow getWindow() {
		if (locator == null)
			return null;

		IWorkbenchLocationService wls = locator
				.getService(IWorkbenchLocationService.class);

		if (window == null) {
			window = wls.getWorkbenchWindow();
		}
		if (window == null) {
			IWorkbench wb = wls.getWorkbench();
			if (wb != null) {
				window = wb.getActiveWorkbenchWindow();
			}
		}
		return window;
	}

	@Override
	public void dispose() {
		if (currentManager != null && currentManager.getSize() > 0) {
			// IMenuService service = (IMenuService) locator
			// .getService(IMenuService.class);
			// if (service != null) {
			// service.releaseContributions(currentManager);
			// }
			currentManager.removeAll();
			currentManager = null;
		}
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).removeMenuListener(menuListener);
		}
		window=null;
		locator=null;
	}
}
