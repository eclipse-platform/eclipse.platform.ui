/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marco Descher <marco@descher.at> - Bug 403081, 403083
 *     Bruce Skingle <Bruce.Skingle@immutify.com> - Bug 442570
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.model.application.ui.menu.MDynamicMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;

/**
 * <code>MenuManagerHideProcessor</code> provides hooks for renderer processing
 * before and after the <code>MenuManager</code> calls out to its
 * <code>IMenuManagerListener2</code> for the <code>menuAboutToHide</code>
 * events.
 */
public class MenuManagerHideProcessor implements IMenuListener2 {

	@Inject
	private MenuManagerRenderer renderer;

	@Inject
	private EModelService modelService;

	@Override
	public void menuAboutToShow(IMenuManager manager) {
		if (!(manager instanceof MenuManager)) {
			return;
		}
		MenuManager menuManager = (MenuManager) manager;
		final MMenu menuModel = renderer.getMenuModel(menuManager);
		final Menu menu = menuManager.getMenu();
		if (menuModel instanceof MPopupMenu) {
			hidePopup(menu, (MPopupMenu) menuModel, menuManager);
		}
		if (menuModel != null && menu != null)
			processDynamicElements((MenuManager) manager, menu, menuModel);
	}

	/**
	 * Process dynamic menu contributions provided by
	 * {@link MDynamicMenuContribution} application model elements
	 *
	 * @param menu
	 * @param menuModel
	 *
	 */
	private void processDynamicElements(final MenuManager menuManager, Menu menu, final MMenu menuModel) {
		// We need to make a copy of the dynamic items which need to be removed
		// because the actual remove happens asynchronously.
		final Map<MDynamicMenuContribution, ArrayList<MMenuElement>> toBeHidden = new HashMap<>();

		for (MMenuElement currentMenuElement : menuModel.getChildren()) {
			if (currentMenuElement instanceof MDynamicMenuContribution) {

				final Map<String, Object> storageMap = currentMenuElement.getTransientData();
				@SuppressWarnings("unchecked")
				ArrayList<MMenuElement> mel = (ArrayList<MMenuElement>) storageMap
						.get(MenuManagerShowProcessor.DYNAMIC_ELEMENT_STORAGE_KEY);

				toBeHidden.put((MDynamicMenuContribution) currentMenuElement, mel);
			}
		}

		if (!menu.isDisposed()) {
			menu.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (Entry<MDynamicMenuContribution, ArrayList<MMenuElement>> entry : toBeHidden.entrySet()) {
						MDynamicMenuContribution currentMenuElement = entry.getKey();
						Object contribution = currentMenuElement.getObject();
						IEclipseContext dynamicMenuContext = EclipseContextFactory.create();

						ArrayList<MMenuElement> mel = entry.getValue();

						dynamicMenuContext.set(List.class, mel);
						IEclipseContext parentContext = modelService.getContainingContext(currentMenuElement);
						ContextInjectionFactory.invoke(contribution, AboutToHide.class, parentContext,
								dynamicMenuContext, null);
						dynamicMenuContext.dispose();
						// remove existing entries for this dynamic
						// contribution item if there are any
						if (mel != null && mel.size() > 0) {
							renderer.removeDynamicMenuContributions(menuManager, menuModel, mel);
						}

						// make existing entries for this dynamic contribution
						// item invisible if there are any
						if (mel != null && mel.size() > 0) {
							for (MMenuElement item : mel) {
								item.setVisible(false);
							}
						}
					}
				}
			});
		}
	}

	@Override
	public void menuAboutToHide(IMenuManager manager) {
	}

	private void hidePopup(Menu menu, MPopupMenu menuModel,
			MenuManager menuManager) {
		final IEclipseContext popupContext = menuModel.getContext();
		final IEclipseContext originalChild = (IEclipseContext) popupContext
				.get(MenuManagerRendererFilter.TMP_ORIGINAL_CONTEXT);
		popupContext.remove(MenuManagerRendererFilter.TMP_ORIGINAL_CONTEXT);
		if (!menu.isDisposed()) {
			menu.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (originalChild == null) {
						popupContext.deactivate();
					} else {
						originalChild.activate();
					}
				}
			});
		}
	}
}
