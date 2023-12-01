/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
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
 *     Marco Descher <marco@descher.at> - Bug 389063,398865,398866,403081,403083
 *     Bruce Skingle <Bruce.Skingle@immutify.com> - Bug 442570
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.menu.MDynamicMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;

/**
 * <code>MenuManagerShowProcessor</code> provides hooks for renderer processing
 * before and after the <code>MenuManager</code> calls out to its
 * <code>IMenuManagerListener2</code> for the <code>menuAboutToShow</code>
 * events.
 */
public class MenuManagerShowProcessor implements IMenuListener2 {

	private static void trace(String msg, MenuManager menuManager, MMenu menuModel) {
		WorkbenchSWTActivator.trace(Policy.DEBUG_MENUS_FLAG,
				msg + ": " + menuManager + ": " + menuManager.getMenu() + ": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ menuModel, null);
	}

	@Inject
	private EModelService modelService;

	@Inject
	private IRendererFactory rendererFactory;

	@Inject
	private MenuManagerRenderer renderer;

	@Inject
	private IContributionFactory contributionFactory;

	@Inject
	@Optional
	private Logger logger;

	@Override
	public void menuAboutToShow(IMenuManager manager) {
		if (!(manager instanceof MenuManager)) {
			return;
		}
		MenuManager menuManager = (MenuManager) manager;
		final MMenu menuModel = renderer.getMenuModel(menuManager);
		final Menu menu = menuManager.getMenu();

		if (menuModel != null) {
			cleanUp(menuModel, menuManager);
		}
		if (menuModel instanceof MPopupMenu) {
			showPopup((MPopupMenu) menuModel);
		}
		AbstractPartRenderer obj = rendererFactory.getRenderer(menuModel,
				menu.getParent());
		if (!(obj instanceof MenuManagerRenderer)) {
			if (Policy.DEBUG_MENUS) {
				trace("Not the correct renderer: " + obj, menuManager, menuModel); //$NON-NLS-1$
			}
			return;
		}
		MenuManagerRenderer renderer = (MenuManagerRenderer) obj;
		if (menuModel.getWidget() == null) {
			renderer.bindWidget(menuModel, menuManager.getMenu());
		}
	}

	@Override
	public void menuAboutToHide(IMenuManager manager) {
		if (!(manager instanceof MenuManager)) {
			return;
		}
		MenuManager menuManager = (MenuManager) manager;
		final MMenu menuModel = renderer.getMenuModel(menuManager);
		if (menuModel != null) {
			processDynamicElements(menuModel, menuManager);
			showMenu(menuModel, menuManager);
		}
	}

	/**
	 * HashMap key for storage of {@link MDynamicMenuContribution} elements
	 */
	protected static final String DYNAMIC_ELEMENT_STORAGE_KEY = MenuManagerShowProcessor.class
			.getSimpleName() + ".dynamicElements"; //$NON-NLS-1$

	/**
	 * Process dynamic menu contributions provided by
	 * {@link MDynamicMenuContribution} application model elements
	 */
	private void processDynamicElements(MMenu menuModel, MenuManager menuManager) {
		MMenuElement[] menuElements = menuModel.getChildren().toArray(
				new MMenuElement[menuModel.getChildren().size()]);
		for (MMenuElement currentMenuElement : menuElements) {

			if (currentMenuElement instanceof MDynamicMenuContribution) {
				MDynamicMenuContribution dmc = (MDynamicMenuContribution) currentMenuElement;
				Object contribution = dmc.getObject();
				if (contribution == null) {
					IEclipseContext context = modelService.getContainingContext(menuModel);
					contribution = contributionFactory.create(dmc.getContributionURI(), context);
					dmc.setObject(contribution);
				}

				IEclipseContext dynamicMenuContext = EclipseContextFactory.create();
				ArrayList<MMenuElement> mel = new ArrayList<>();
				dynamicMenuContext.set(List.class, mel);
				dynamicMenuContext.set(MDynamicMenuContribution.class, dmc);
				IEclipseContext parentContext = modelService.getContainingContext(currentMenuElement);
				Object rc = ContextInjectionFactory.invoke(contribution,
						AboutToShow.class, parentContext, dynamicMenuContext,
						this);
				dynamicMenuContext.dispose();
				if (rc == this) {
					if (logger != null) {
						logger.error("Missing @AboutToShow method in " + contribution); //$NON-NLS-1$
					}
					continue;
				}

				if (mel.size() > 0) {

					int position = 0;
					while (position < menuModel.getChildren().size()) {
						if (currentMenuElement == menuModel.getChildren().get(
								position)) {
							position++;
							break;
						}
						position++;
					}

					// ensure that each element of the list has a valid element
					// id
					// and set the parent of the entries
					for (int j = 0; j < mel.size(); j++) {
						MMenuElement menuElement = mel.get(j);
						if (menuElement.getElementId() == null
								|| menuElement.getElementId().length() < 1) {
							menuElement.setElementId(currentMenuElement
									.getElementId() + "." + j); //$NON-NLS-1$
						}
						menuModel.getChildren().add(position++, menuElement);
						renderer.modelProcessSwitch(menuManager, menuElement);
					}
					currentMenuElement.getTransientData().put(DYNAMIC_ELEMENT_STORAGE_KEY, mel);
				}
			}
		}
	}

	/**
	 * Remove all of the items created by any dynamic contributions on the
	 * menuModel. In addition removes all of the items of menuModel in the case
	 * all items of menuManager need removal when the menu is about to show.
	 * This needs to be done or else menu items get added multiple times to
	 * MenuModel which results in incorrect behavior and memory leak - bug
	 * 486474
	 */
	private void cleanUp(MMenu menuModel, MenuManager menuManager) {
		if (Policy.DEBUG_MENUS) {
			trace("\nCleaning up the dynamic menu contributions", menuManager, menuModel); //$NON-NLS-1$
		}
		renderer.removeDynamicMenuContributions(menuManager, menuModel);

		if (menuManager.getRemoveAllWhenShown()) {
			// remove the items from the model related to contributions defined
			// with location URIs
			if (Policy.DEBUG_MENUS) {
				trace("\nCleaning up all of the menu model items", menuManager, menuModel); //$NON-NLS-1$
			}
			renderer.cleanUp(menuModel);

			// cleanup any leftovers - opaque items etc
			for (Iterator<MMenuElement> it = menuModel.getChildren().iterator(); it.hasNext();) {
				MMenuElement mMenuElement = it.next();
				// remove item from the menu model
				it.remove();
				// cleanup the renderer
				IContributionItem ici = renderer.getContribution(mMenuElement);
				if (ici == null && mMenuElement instanceof MMenu) {
					MMenu menuElement = (MMenu) mMenuElement;
					ici = renderer.getManager(menuElement);
					renderer.clearModelToManager(menuElement, (MenuManager) ici);
				}
				renderer.clearModelToContribution(mMenuElement, ici);
			}
		}
	}

	private void showPopup(final MPopupMenu menuModel) {
		// System.err.println("showPopup: " + menuModel + "\n\t" + menu);
		// we need some context foolery here
		final IEclipseContext popupContext = menuModel.getContext();
		final IEclipseContext parentContext = popupContext.getParent();
		final IEclipseContext originalChild = parentContext.getActiveChild();
		popupContext.activate();
		popupContext.set(MenuManagerRendererFilter.TMP_ORIGINAL_CONTEXT,
				originalChild);
	}

	private void showMenu(final MMenu menuModel,
			MenuManager menuManager) {

		final IEclipseContext evalContext;
		if (menuModel instanceof MContext) {
			evalContext = ((MContext) menuModel).getContext();
		} else {
			evalContext = modelService.getContainingContext(menuModel);
		}
		MenuManagerRendererFilter.updateElementVisibility(menuModel, renderer,
				menuManager, evalContext, 2, true);
	}

}
