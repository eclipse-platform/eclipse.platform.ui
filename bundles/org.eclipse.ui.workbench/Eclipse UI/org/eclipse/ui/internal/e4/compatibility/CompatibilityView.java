/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
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
	void updateImages(MPart part) {
		EModelService ms = part.getContext().get(EModelService.class);
		MWindow topWin = ms.getTopLevelWindowFor(part);
		List<MPlaceholder> partRefs = ms.findElements(topWin, part.getElementId(),
				MPlaceholder.class, null);
		for (MUIElement ref : partRefs) {
			updateTabImages(ref);
		}
		part.getTransientData().put(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY,
				wrapped.getTitleImage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.CompatibilityPart#getReference()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.CompatibilityPart#createPartControl
	 * (org.eclipse.ui.IWorkbenchPart, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected boolean createPartControl(IWorkbenchPart legacyPart, Composite parent) {
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

			// Only use the substring before the ':'
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

			// Only use the substring before the ':'
			String partId = part.getElementId();
			int colonIndex = partId.indexOf(':');
			String descId = colonIndex == -1 ? partId : partId.substring(0, colonIndex);
			toolbar.setElementId(descId);

			part.setToolbar(toolbar);
		}
		apr = rendererFactory.getRenderer(toolbar, parent);
		if (apr instanceof ToolBarManagerRenderer) {
			((ToolBarManagerRenderer) apr).linkModelToManager(toolbar, tbm);
		}

		super.createPartControl(legacyPart, parent);

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

		final IContextFunction func = new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context) {
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
					func.compute(partContext));
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

			if (child instanceof MOpaqueMenuSeparator) {
				((MOpaqueMenuSeparator) child).setOpaqueItem(null);
				it.remove();
			} else if (child instanceof MOpaqueMenuItem) {
				((MOpaqueMenuItem) child).setOpaqueItem(null);
				it.remove();
			} else if (child instanceof MMenu) {
				MMenu submenu = (MMenu) child;
				MenuManager manager = renderer.getManager(submenu);
				if (manager != null) {
					renderer.clearModelToManager(submenu, manager);
				}

				if (child instanceof MOpaqueMenu) {
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
					if (element instanceof MOpaqueToolItem) {
						IContributionItem item = tbmr.getContribution(element);
						if (item != null) {
							tbmr.clearModelToContribution(element, item);
						}
						// clear the reference
						((MOpaqueToolItem) element).setOpaqueItem(null);
						// remove the opaque item
						it.remove();
					}
				}
			}
		}

		super.disposeSite(site);
	}
}
