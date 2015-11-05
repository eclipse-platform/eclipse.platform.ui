/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.ActionDescriptor;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.ViewActionBuilder;
import org.eclipse.ui.internal.ViewReference;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.internal.testing.ContributionInfoMessages;
import org.eclipse.ui.testing.ContributionInfo;

public class CompatibilityView extends CompatibilityPart {

	private ViewReference reference;

	@Inject
	CompatibilityView(MPart part, ViewReference ref) {
		super(part);
		reference = ref;
	}

	public IViewPart getView() {
		return (IViewPart) getPart();
	}

	@Override
	public WorkbenchPartReference getReference() {
		return reference;
	}

	private MMenu getViewMenu() {
		for (MMenu menu : part.getMenus()) {
			if (menu.getTags().contains(StackRenderer.TAG_VIEW_MENU)) {
				return menu;
			}
		}
		return null;
	}

	@Override
	protected boolean createPartControl(IWorkbenchPart legacyPart, Composite parent) {
		clearMenuItems();
		part.getContext().set(IViewPart.class, (IViewPart) legacyPart);

		final IEclipseContext partContext = getModel().getContext();
		IRendererFactory rendererFactory = partContext.get(IRendererFactory.class);

		// Some views (i.e. Console) require that the actual ToolBar be
		// instantiated before they are
		final IActionBars actionBars = ((IViewPart) legacyPart).getViewSite().getActionBars();
		ToolBarManager tbm = (ToolBarManager) actionBars.getToolBarManager();
		Composite toolBarParent = new Composite(parent, SWT.NONE);
		tbm.createControl(toolBarParent);

		MenuManager mm = (MenuManager) actionBars.getMenuManager();
		MMenu menu = getViewMenu();
		if (menu == null) {
			menu = MenuFactoryImpl.eINSTANCE.createMenu();

			// If the id contains a ':' use the part before it as the descriptor
			// id
			String partId = part.getElementId();
			int colonIndex = partId.indexOf(':');
			String descId = colonIndex == -1 ? partId : partId.substring(0, colonIndex);
			menu.setElementId(descId);

			menu.getTags().add(StackRenderer.TAG_VIEW_MENU);
			menu.getTags().add(ContributionsAnalyzer.MC_MENU);
			part.getMenus().add(menu);

		}
		AbstractPartRenderer apr = rendererFactory.getRenderer(menu, parent);
		if (apr instanceof MenuManagerRenderer) {
			MenuManagerRenderer renderer = (MenuManagerRenderer) apr;
			renderer.linkModelToManager(menu, mm);
		}

		// Construct the toolbar (if necessary)
		MToolBar toolbar = part.getToolbar();
		if (toolbar == null) {
			toolbar = MenuFactoryImpl.eINSTANCE.createToolBar();

			// If the id contains a ':' use the part before it as the descriptor
			// id
			String partId = part.getElementId();
			int colonIndex = partId.indexOf(':');
			String descId = colonIndex == -1 ? partId : partId.substring(0, colonIndex);
			toolbar.setElementId(descId);

			part.setToolbar(toolbar);
		} else {
			// clear out the model entries so they can be re-created by
			// contributions
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=402561
			toolbar.getChildren().clear();
		}
		apr = rendererFactory.getRenderer(toolbar, parent);
		if (apr instanceof ToolBarManagerRenderer) {
			((ToolBarManagerRenderer) apr).linkModelToManager(toolbar, tbm);
		}

		super.createPartControl(legacyPart, parent);

		ViewDescriptor desc = reference.getDescriptor();
		if (desc != null && desc.getPluginId() != null) {
			parent.setData(new ContributionInfo(desc.getPluginId(),
					ContributionInfoMessages.ContributionInfo_View, null));
		}

		// dispose the tb, it will be re-created when the tab is shown
		toolBarParent.dispose();

		apr = rendererFactory.getRenderer(menu, parent);
		if (apr instanceof MenuManagerRenderer) {
			MenuManagerRenderer renderer = (MenuManagerRenderer) apr;
			// create opaque items for any contribution items that were added
			// directly to the manager
			renderer.reconcileManagerToModel(mm, menu);
		}

		apr = rendererFactory.getRenderer(toolbar, parent);
		if (apr instanceof ToolBarManagerRenderer) {
			// create opaque items for any contribution items that were added
			// directly to the manager
			((ToolBarManagerRenderer) apr).reconcileManagerToModel(tbm, toolbar);
		}
		final AtomicBoolean toolbarContributed = new AtomicBoolean();
		final IContextFunction func = new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (toolbarContributed.get()) {
					// fix for bug 448873: don't contribute to the toolbar twice
					return null;
				}
				toolbarContributed.set(true);
				final ViewActionBuilder actionBuilder = new ViewActionBuilder();
				actionBuilder.readActionExtensions(getView());
				ActionDescriptor[] actionDescriptors = actionBuilder.getExtendedActions();
				if (actionDescriptors != null) {
					IHandlerService hs = partContext.get(IHandlerService.class);
					for (int i = 0; i < actionDescriptors.length; i++) {
						ActionDescriptor actionDescriptor = actionDescriptors[i];

						if (actionDescriptor != null) {
							IAction action = actionDescriptors[i].getAction();

							if (action != null && action.getActionDefinitionId() != null) {
								hs.activateHandler(action.getActionDefinitionId(),
										new ActionHandler(action));
							}
						}
					}
				}
				actionBars.updateActionBars();
				final Runnable dispose = new Runnable() {

					@Override
					public void run() {
						actionBuilder.dispose();
					}
				};
				return dispose;
			}
		};
		if (toolbar.getWidget() == null) {
			toolbar.getTransientData().put(ToolBarManagerRenderer.POST_PROCESSING_FUNCTION, func);
		} else {
			toolbar.getTransientData().put(ToolBarManagerRenderer.POST_PROCESSING_DISPOSE,
					func.compute(partContext, null));
		}

		return true;
	}

	private void clearOpaqueMenuItems(MenuManagerRenderer renderer, MMenu menu) {
		for (Iterator<MMenuElement> it = menu.getChildren().iterator(); it.hasNext();) {
			MMenuElement child = it.next();
			IContributionItem contribution = renderer.getContribution(child);
			if (contribution != null) {
				renderer.clearModelToContribution(child, contribution);
			}

			if (OpaqueElementUtil.isOpaqueMenuSeparator(child)) {
				OpaqueElementUtil.clearOpaqueItem(child);
				it.remove();
			} else if (OpaqueElementUtil.isOpaqueMenuItem(child)) {
				OpaqueElementUtil.clearOpaqueItem(child);
				it.remove();
			} else if (child instanceof MMenu) {
				MMenu submenu = (MMenu) child;
				MenuManager manager = renderer.getManager(submenu);
				if (manager != null) {
					renderer.clearModelToManager(submenu, manager);
				}

				if (OpaqueElementUtil.isOpaqueMenu(child)) {
					it.remove();
				}
				clearOpaqueMenuItems(renderer, submenu);
			}
		}
	}

	@Override
	void disposeSite(PartSite site) {
		IEclipseContext context = getModel().getContext();
		IRendererFactory rendererFactory = context.get(IRendererFactory.class);
		IActionBars actionBars = site.getActionBars();

		for (MMenu menu : part.getMenus()) {
			if (menu.getTags().contains(StackRenderer.TAG_VIEW_MENU)) {
				AbstractPartRenderer apr = rendererFactory.getRenderer(menu, null);
				if (apr instanceof MenuManagerRenderer) {
					MenuManagerRenderer renderer = (MenuManagerRenderer) apr;
					MenuManager mm = (MenuManager) actionBars.getMenuManager();
					renderer.clearModelToManager(menu, mm);
					clearOpaqueMenuItems(renderer, menu);
				}
				break;
			}
		}

		MToolBar toolbar = part.getToolbar();
		if (toolbar != null) {
			AbstractPartRenderer apr = rendererFactory.getRenderer(toolbar, null);
			if (apr instanceof ToolBarManagerRenderer) {
				ToolBarManager tbm = (ToolBarManager) actionBars.getToolBarManager();
				ToolBarManagerRenderer tbmr = (ToolBarManagerRenderer) apr;
				tbmr.clearModelToManager(toolbar, tbm);
				// remove opaque mappings
				for (Iterator<MToolBarElement> it = toolbar.getChildren().iterator(); it.hasNext();) {
					MToolBarElement element = it.next();
					if (OpaqueElementUtil.isOpaqueToolItem(element)) {
						IContributionItem item = tbmr.getContribution(element);
						if (item != null) {
							tbmr.clearModelToContribution(element, item);
						}
						// clear the reference
						OpaqueElementUtil.clearOpaqueItem(element);
						// remove the opaque item
						it.remove();
					}
				}
			}
		}

		super.disposeSite(site);
	}
}
