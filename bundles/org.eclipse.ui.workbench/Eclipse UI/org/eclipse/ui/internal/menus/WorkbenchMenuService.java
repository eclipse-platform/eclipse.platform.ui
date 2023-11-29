/*******************************************************************************
 * Copyright (c) 2010, 2019 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.ContributionRecord;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRendererFilter;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarContributionRecord;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IMenuServiceWorkaround;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityView;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;

/**
 * @since 3.5
 */
public class WorkbenchMenuService implements IMenuService, IMenuServiceWorkaround {

	private static final String POPULATED_TOOL_BARS = "populatedToolBars"; //$NON-NLS-1$
	private static final String POPULATED_MENUS = "populatedMenus"; //$NON-NLS-1$
	private IEclipseContext e4Context;
	private ServiceLocator serviceLocator;
	private ExpressionContext legacyContext;
	private MenuPersistence persistence;
	private Map<AbstractContributionFactory, Object> factoriesToContributions = new HashMap<>();
	private EModelService modelService;

	public WorkbenchMenuService(ServiceLocator serviceLocator, IEclipseContext e4Context) {
		this.serviceLocator = serviceLocator;
		this.e4Context = e4Context;
		modelService = e4Context.get(EModelService.class);

		persistence = new MenuPersistence(e4Context.get(MApplication.class), e4Context);
	}

	@Override
	public void addSourceProvider(ISourceProvider provider) {
		// TODO Auto-generated method

	}

	@Override
	public void removeSourceProvider(ISourceProvider provider) {
	}

	@Override
	public void dispose() {
		persistence.dispose();
	}

	private boolean inToolbar(MenuLocationURI location) {
		return location.getScheme().startsWith("toolbar"); //$NON-NLS-1$
	}

	@Override
	public void addContributionFactory(final AbstractContributionFactory factory) {
		MenuLocationURI location = new MenuLocationURI(factory.getLocation());
		if (location.getPath() == null || location.getPath().isEmpty()) {
			WorkbenchPlugin.log("WorkbenchMenuService.addContributionFactory: Invalid menu URI: " + location); //$NON-NLS-1$
			return;
		}

		if (inToolbar(location)) {
			if (MenuAdditionCacheEntry.isInWorkbenchTrim(location)) {
				// processTrimChildren(trimContributions, toolBarContributions,
				// configElement);
			} else {
				String query = location.getQuery();
				if (query == null || query.isEmpty()) {
					query = "after=additions"; //$NON-NLS-1$
				}
				processToolbarChildren(factory, location, location.getPath(), query);
			}
			return;
		}
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		menuContribution.getPersistedState().put(org.eclipse.e4.ui.workbench.IWorkbench.PERSIST_STATE,
				Boolean.FALSE.toString());
		menuContribution.setElementId(factory.getNamespace() + ":" + factory.hashCode()); //$NON-NLS-1$

		if ("org.eclipse.ui.popup.any".equals(location.getPath())) { //$NON-NLS-1$
			menuContribution.setParentId("popup"); //$NON-NLS-1$
		} else {
			menuContribution.setParentId(location.getPath());
		}
		String query = location.getQuery();
		if (query == null || query.isEmpty()) {
			query = "after=additions"; //$NON-NLS-1$
		}
		menuContribution.setPositionInParent(query);
		menuContribution.getTags().add("scheme:" + location.getScheme()); //$NON-NLS-1$
		String filter = ContributionsAnalyzer.MC_MENU;
		if ("popup".equals(location.getScheme())) { //$NON-NLS-1$
			filter = ContributionsAnalyzer.MC_POPUP;
		}
		menuContribution.getTags().add(filter);
		ContextFunction generator = new ContributionFactoryGenerator(factory, 0);
		menuContribution.getTransientData().put(ContributionRecord.FACTORY, generator);
		factoriesToContributions.put(factory, menuContribution);
		MApplication app = e4Context.get(MApplication.class);
		app.getMenuContributions().add(menuContribution);

	}

	private void processToolbarChildren(AbstractContributionFactory factory, MenuLocationURI location, String parentId,
			String position) {
		MToolBarContribution toolBarContribution = MenuFactoryImpl.eINSTANCE.createToolBarContribution();
		toolBarContribution.getPersistedState().put(org.eclipse.e4.ui.workbench.IWorkbench.PERSIST_STATE,
				Boolean.FALSE.toString());
		toolBarContribution.setElementId(factory.getNamespace() + ":" + factory.hashCode()); //$NON-NLS-1$
		toolBarContribution.setParentId(parentId);
		toolBarContribution.setPositionInParent(position);
		toolBarContribution.getTags().add("scheme:" + location.getScheme()); //$NON-NLS-1$

		ContextFunction generator = new ContributionFactoryGenerator(factory, 1);
		toolBarContribution.getTransientData().put(ToolBarContributionRecord.FACTORY, generator);
		factoriesToContributions.put(factory, toolBarContribution);
		MApplication app = e4Context.get(MApplication.class);
		app.getToolBarContributions().add(toolBarContribution);
	}

	@Override
	public void removeContributionFactory(AbstractContributionFactory factory) {
		Object contribution;
		if ((contribution = factoriesToContributions.remove(factory)) != null) {
			MApplication app = e4Context.get(MApplication.class);
			if (app == null)
				return;
			if (contribution instanceof MMenuContribution) {
				app.getMenuContributions().remove(contribution);
			} else if (contribution instanceof MToolBarContribution) {
				app.getToolBarContributions().remove(contribution);
			}
		}

	}

	protected IWorkbenchWindow getWindow() {
		if (serviceLocator == null)
			return null;

		IWorkbenchLocationService wls = serviceLocator.getService(IWorkbenchLocationService.class);

		IWorkbenchWindow window = null;
		if (window == null) {
			window = wls.getWorkbenchWindow();
		}
		if (window == null) {
			IWorkbench wb = wls.getWorkbench();
			if (wb != null) {
				window = wb.getActiveWorkbenchWindow();
			}
		}
		if (window == null) {
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		return window;
	}

	@Override
	public void populateContributionManager(ContributionManager mgr, String location) {
		MApplicationElement model = getPartToExtend();
		if (model == null) {
			final IWorkbenchWindow window = getWindow();
			if (window instanceof WorkbenchWindow) {
				model = ((WorkbenchWindow) window).getModel();
			}
		}
		populateContributionManager(model, mgr, location);
	}

	public void populateContributionManager(MApplicationElement model, ContributionManager mgr, String location) {
		MenuLocationURI uri = new MenuLocationURI(location);

		// Now handle registering dynamic additions by querying E4 model
		if (mgr instanceof MenuManager) {
			MenuManager menu = (MenuManager) mgr;
			MMenu mMenu = getMenuModel(model, menu, uri);
			if (mMenu == null) {
				return;
			}

			IRendererFactory factory = e4Context.get(IRendererFactory.class);
			AbstractPartRenderer obj = factory.getRenderer(mMenu, null);
			if (obj instanceof MenuManagerRenderer) {
				MenuManagerRenderer renderer = (MenuManagerRenderer) obj;
				mMenu.setRenderer(renderer);
				renderer.reconcileManagerToModel(menu, mMenu);
				renderer.processContributions(mMenu, uri.getPath(), false, "popup".equals(uri.getScheme())); //$NON-NLS-1$
				// double cast because we're bad people
				renderer.processContents((MElementContainer<MUIElement>) ((Object) mMenu));
				final IEclipseContext evalContext;
				if (mMenu instanceof MContext) {
					evalContext = ((MContext) mMenu).getContext();
				} else {
					evalContext = modelService.getContainingContext(mMenu);
				}
				MenuManagerRendererFilter.updateElementVisibility(mMenu, renderer, menu, evalContext, 2, true);
			}
		} else if (mgr instanceof ToolBarManager) {
			ToolBarManager toolbar = (ToolBarManager) mgr;
			MToolBar mToolBar = getToolbarModel(model, toolbar, uri);
			if (mToolBar == null) {
				return;
			}

			IRendererFactory factory = e4Context.get(IRendererFactory.class);
			AbstractPartRenderer obj = factory.getRenderer(mToolBar, null);
			if (obj instanceof ToolBarManagerRenderer) {
				ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) obj;
				mToolBar.setRenderer(renderer);
				renderer.reconcileManagerToModel(toolbar, mToolBar);
				renderer.processContribution(mToolBar, uri.getPath());
				// double cast because we're bad people
				renderer.processContents((MElementContainer<MUIElement>) ((Object) mToolBar));
			}
		} else {
			WorkbenchPlugin.log("populateContributionManager: Unhandled manager: " + mgr); //$NON-NLS-1$
		}
	}

	protected MToolBar getToolbarModel(MApplicationElement model, ToolBarManager toolbarManager,
			MenuLocationURI location) {
		final IRendererFactory factory = e4Context.get(IRendererFactory.class);
		final AbstractPartRenderer obj = factory.getRenderer(MenuFactoryImpl.eINSTANCE.createToolBar(), null);
		if (!(obj instanceof ToolBarManagerRenderer)) {
			return null;
		}
		ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) obj;
		MToolBar mToolBar = renderer.getToolBarModel(toolbarManager);
		if (mToolBar != null) {
			String tag = "toolbar:" + location.getPath(); //$NON-NLS-1$
			if (!mToolBar.getTags().contains(tag)) {
				mToolBar.getTags().add(tag);
			}
			return mToolBar;
		}

		if (mToolBar == null) {
			mToolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
			mToolBar.setElementId(location.getPath());
			mToolBar.getTags().add(ContributionsAnalyzer.MC_TOOLBAR);
			String tag = "toolbar:" + location.getPath(); //$NON-NLS-1$
			mToolBar.getTags().add(tag);
			addToolbar(model, mToolBar, ((MContext) model).getContext());
		}

		renderer.linkModelToManager(mToolBar, toolbarManager);

		return mToolBar;
	}

	private void addToolbar(MApplicationElement model, MToolBar tb, IEclipseContext ctx) {
		ArrayList<MToolBar> toolbars = (ArrayList<MToolBar>) model.getTransientData().get(POPULATED_TOOL_BARS);
		if (toolbars == null) {
			toolbars = new ArrayList<>();
			model.getTransientData().put(POPULATED_TOOL_BARS, toolbars);
		}
		if (toolbars.contains(tb)) {
			return;
		}
		toolbars.add(tb);
		tb.getTransientData().put(ModelUtils.CONTAINING_PARENT, model);
		((Notifier) tb).eAdapters().add(ctx.get(UIEventPublisher.class));
	}

	private void addMenu(MApplicationElement model, MMenu menu, IEclipseContext ctx) {
		ArrayList<MMenu> menus = (ArrayList<MMenu>) model.getTransientData().get(POPULATED_MENUS);
		if (menus == null) {
			menus = new ArrayList<>();
			model.getTransientData().put(POPULATED_MENUS, menus);
		}
		if (menus.contains(menu)) {
			return;
		}
		menus.add(menu);
		menu.getTransientData().put(ModelUtils.CONTAINING_PARENT, model);
		((Notifier) menu).eAdapters().add(ctx.get(UIEventPublisher.class));
	}

	protected MMenu getMenuModel(MApplicationElement model, MenuManager menuManager, MenuLocationURI location) {

		final IRendererFactory factory = e4Context.get(IRendererFactory.class);
		final AbstractPartRenderer obj = factory.getRenderer(((WorkbenchWindow) getWindow()).getModel().getMainMenu(),
				null);
		if (!(obj instanceof MenuManagerRenderer)) {
			return null;
		}
		MenuManagerRenderer renderer = (MenuManagerRenderer) obj;
		MMenu mMenu = renderer.getMenuModel(menuManager);
		if (mMenu != null) {
			final String tag;
			if ("popup".equals(location.getScheme())) { //$NON-NLS-1$
				tag = "popup:" + location.getPath(); //$NON-NLS-1$
			} else {
				tag = "menu:" + location.getPath(); //$NON-NLS-1$
			}
			if (!mMenu.getTags().contains(tag)) {
				mMenu.getTags().add(tag);
			}
			return mMenu;
		}

		if (mMenu == null) {
			mMenu = MenuFactoryImpl.eINSTANCE.createMenu();
			mMenu.setElementId(menuManager.getId());
			if (mMenu.getElementId() == null) {
				mMenu.setElementId(location.getPath());
			}
			final String tag;
			if ("popup".equals(location.getScheme())) { //$NON-NLS-1$
				mMenu.getTags().add(ContributionsAnalyzer.MC_POPUP);
				tag = "popup:" + location.getPath(); //$NON-NLS-1$
			} else {
				mMenu.getTags().add(ContributionsAnalyzer.MC_MENU);
				tag = "menu:" + location.getPath(); //$NON-NLS-1$
			}
			mMenu.getTags().add(tag);
			mMenu.setLabel(menuManager.getMenuText());
			addMenu(model, mMenu, ((MContext) model).getContext());
		}

		renderer.linkModelToManager(mMenu, menuManager);

		return mMenu;
	}

	private MPart getPartToExtend() {
		return (MPart) e4Context.getActiveLeaf().get(IServiceConstants.ACTIVE_PART);
	}

	@Override
	public void releaseContributions(ContributionManager mgr) {
		if (mgr instanceof MenuManager) {
			MenuManager menu = (MenuManager) mgr;
			releaseContributionManager(menu);
		} else if (mgr instanceof ToolBarManager) {
			ToolBarManager toolbar = (ToolBarManager) mgr;
			releaseContributionManager(toolbar);
		} else {
			WorkbenchPlugin.log("releaseContributions: Unhandled manager: " + mgr); //$NON-NLS-1$
		}
	}

	private void releaseContributionManager(ToolBarManager toolbarManager) {
		final IRendererFactory factory = e4Context.get(IRendererFactory.class);
		final AbstractPartRenderer obj = factory.getRenderer(MenuFactoryImpl.eINSTANCE.createToolBar(), null);
		if (!(obj instanceof ToolBarManagerRenderer)) {
			return;
		}
		ToolBarManagerRenderer renderer = (ToolBarManagerRenderer) obj;
		MToolBar mToolBar = renderer.getToolBarModel(toolbarManager);
		if (mToolBar == null) {
			return;
		}
		MApplicationElement model = (MApplicationElement) mToolBar.getTransientData().get(ModelUtils.CONTAINING_PARENT);
		if (model != null) {
			((Notifier) mToolBar).eAdapters().clear();
			ArrayList<MToolBar> toolbars = (ArrayList<MToolBar>) model.getTransientData().get(POPULATED_TOOL_BARS);
			if (toolbars != null) {
				toolbars.remove(mToolBar);
			}
		}
		final ToolBar widget = toolbarManager.getControl();
		if (widget != null && !widget.isDisposed() && widget.getData(AbstractPartRenderer.OWNING_ME) == null) {
			widget.setData(AbstractPartRenderer.OWNING_ME, mToolBar);
		}
		final IPresentationEngine engine = e4Context.get(IPresentationEngine.class);
		engine.removeGui(mToolBar);
		mToolBar.getTransientData().remove(ModelUtils.CONTAINING_PARENT);
	}

	private void releaseContributionManager(MenuManager menuManager) {
		final IRendererFactory factory = e4Context.get(IRendererFactory.class);
		final AbstractPartRenderer obj = factory.getRenderer(((WorkbenchWindow) getWindow()).getModel().getMainMenu(),
				null);
		if (!(obj instanceof MenuManagerRenderer)) {
			return;
		}
		MenuManagerRenderer renderer = (MenuManagerRenderer) obj;
		MMenu mMenu = renderer.getMenuModel(menuManager);
		if (mMenu == null) {
			return;
		}
		MApplicationElement model = (MApplicationElement) mMenu.getTransientData().get(ModelUtils.CONTAINING_PARENT);
		if (model != null) {
			((Notifier) mMenu).eAdapters().clear();
			ArrayList<MMenu> menus = (ArrayList<MMenu>) model.getTransientData().get(POPULATED_MENUS);
			if (menus != null) {
				menus.remove(mMenu);
			}
		}
		final Menu widget = menuManager.getMenu();
		if (widget != null && !widget.isDisposed() && widget.getData(AbstractPartRenderer.OWNING_ME) == null) {
			widget.setData(AbstractPartRenderer.OWNING_ME, mMenu);
		}
		final IPresentationEngine engine = e4Context.get(IPresentationEngine.class);
		engine.removeGui(mMenu);
		mMenu.getTransientData().remove(ModelUtils.CONTAINING_PARENT);
	}

	/**
	 * Disposes contributions created by service for given part. See bug 537046.
	 */
	@Override
	public void clearContributions(PartSite site, MPart part) {
		List<MToolBar> toolbars = getContributedToolbars(part);
		IEclipseContext context = part.getContext();
		IRendererFactory rendererFactory = context.get(IRendererFactory.class);
		IActionBars actionBars = site.getActionBars();
		if (toolbars != null) {
			for (MToolBar mToolBar : toolbars) {
				((Notifier) mToolBar).eAdapters().clear();
				AbstractPartRenderer apr = rendererFactory.getRenderer(mToolBar, null);
				if (apr instanceof ToolBarManagerRenderer) {
					ToolBarManager tbm = (ToolBarManager) actionBars.getToolBarManager();
					ToolBarManagerRenderer tbmr = (ToolBarManagerRenderer) apr;
					tbmr.clearModelToManager(mToolBar, tbm);
					CompatibilityView.clearOpaqueToolBarItems(tbmr, mToolBar);
				}
				mToolBar.getTransientData().remove(ToolBarManagerRenderer.POST_PROCESSING_FUNCTION);
				final IPresentationEngine engine = context.get(IPresentationEngine.class);
				engine.removeGui(mToolBar);
				mToolBar.getTransientData().remove(ModelUtils.CONTAINING_PARENT);
			}
		}
		List<MMenu> menus = getContributedMenus(part);
		if (menus != null) {
			for (MMenu mMenu : menus) {
				((Notifier) mMenu).eAdapters().clear();
				AbstractPartRenderer apr = rendererFactory.getRenderer(mMenu, null);
				if (apr instanceof MenuManagerRenderer) {
					MenuManager tbm = (MenuManager) actionBars.getMenuManager();
					MenuManagerRenderer tbmr = (MenuManagerRenderer) apr;
					tbmr.clearModelToManager(mMenu, tbm);
					CompatibilityView.clearOpaqueMenuItems(tbmr, mMenu);
				}
				final IPresentationEngine engine = context.get(IPresentationEngine.class);
				engine.removeGui(mMenu);
				mMenu.getTransientData().remove(ModelUtils.CONTAINING_PARENT);
			}
		}
	}

	private List<MMenu> getContributedMenus(MPart part) {
		return (List<MMenu>) part.getTransientData().get(POPULATED_MENUS);
	}

	private List<MToolBar> getContributedToolbars(MPart part) {
		return (List<MToolBar>) part.getTransientData().get(POPULATED_TOOL_BARS);
	}

	@Override
	public IEvaluationContext getCurrentState() {
		if (legacyContext == null) {
			legacyContext = new ExpressionContext(e4Context);
		}
		return legacyContext;
	}

	/**
	 * read in the menu contributions and turn them into model menu contributions
	 */
	public void readRegistry() {
		persistence.read();
	}

}
