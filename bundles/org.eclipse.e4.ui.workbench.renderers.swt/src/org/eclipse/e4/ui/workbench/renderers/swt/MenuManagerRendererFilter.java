/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 488978
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import java.util.HashSet;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.MenuService;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

public class MenuManagerRendererFilter implements Listener {
	private static final String MMRF_STATIC_CONTEXT = "HCI-staticContext"; //$NON-NLS-1$

	public static final String NUL_MENU_ITEM = "(None Applicable)"; //$NON-NLS-1$

	static final String TMP_ORIGINAL_CONTEXT = "MenuServiceFilter.original.context"; //$NON-NLS-1$

	private static void trace(String msg, Widget menu, MMenu menuModel) {
		WorkbenchSWTActivator.trace(Policy.MENUS, msg + ": " + menu + ": " //$NON-NLS-1$ //$NON-NLS-2$
				+ menuModel, null);
	}

	@Inject
	private Logger logger;

	@Inject
	private EModelService modelService;

	@Inject
	private MenuManagerRenderer renderer;

	private HashMap<Menu, Runnable> pendingCleanup = new HashMap<>();

	private class SafeWrapper implements ISafeRunnable {
		Event event;

		@Override
		public void handleException(Throwable e) {
			if (e instanceof Error) {
				// errors are deadly, we shouldn't ignore these
				throw (Error) e;
			}
			// log exceptions otherwise
			if (logger != null) {
				logger.error(e);
			}
		}

		@Override
		public void run() throws Exception {
			safeHandleEvent(event);
		}
	}

	private SafeWrapper safeWrapper = new SafeWrapper();

	@Override
	public void handleEvent(final Event event) {
		// wrap the handling in a SafeRunner so that exceptions do not prevent
		// the menu from being shown
		safeWrapper.event = event;
		SafeRunner.run(safeWrapper);
	}

	private void safeHandleEvent(Event event) {
		if (!(event.widget instanceof Menu)) {
			return;
		}
		final Menu menu = (Menu) event.widget;
		if ((menu.getStyle() & SWT.BAR) != 0) {
			// don't process the menu bar, it's not fair :-)
			return;
		}
		if (event.type == SWT.Dispose) {
			trace("handleMenu.Dispose", menu, null); //$NON-NLS-1$
			cleanUp(menu, null, null);
			return;
		}

		// fill in all of the pieces
		MenuManager menuManager = null;
		Object obj = menu.getData(AbstractPartRenderer.OWNING_ME);
		if (obj == null) {
			Object tmp = menu.getData(MenuManager.MANAGER_KEY);
			if (tmp instanceof MenuManager) {
				MenuManager tmpManager = (MenuManager) tmp;
				menuManager = tmpManager;
				obj = renderer.getMenuModel(tmpManager);
				if (obj instanceof MPopupMenu) {
					MPopupMenu popupMenu = (MPopupMenu) obj;
					if (popupMenu.getWidget() == null
							&& menuManager.getMenu() != null) {
						final MUIElement container = modelService
								.getContainer(popupMenu);
						if (container instanceof MPart) {
							MenuService.registerMenu(menuManager.getMenu()
									.getParent(), popupMenu,
									((MPart) container).getContext());
						}
					}
				}
			}
		}
	}

	/**
	 * @param info
	 * @param menuModel
	 * @param renderer
	 * @param evalContext
	 * @param recurse
	 */
	public static void collectInfo(ExpressionInfo info, final MMenu menuModel,
			final MenuManagerRenderer renderer,
			final IEclipseContext evalContext, boolean recurse) {
		HashSet<ContributionRecord> records = new HashSet<>();
		for (MMenuElement element : menuModel.getChildren()) {
			ContributionRecord record = renderer.getContributionRecord(element);
			if (record != null) {
				if (records.add(record)) {
					record.collectInfo(info);
				}
			} else {
				ContributionsAnalyzer.collectInfo(info,
						element.getVisibleWhen());
			}
			if (recurse && element instanceof MMenu) {
				MMenu childMenu = (MMenu) element;
				collectInfo(info, childMenu, renderer, evalContext, false);
			}
		}
	}

	/**
	 * @param menuModel
	 * @param renderer
	 * @param menuManager
	 * @param evalContext
	 */
	public static void updateElementVisibility(final MMenu menuModel,
			MenuManagerRenderer renderer, MenuManager menuManager,
			final IEclipseContext evalContext, final int recurseLevel,
			boolean updateEnablement) {
		final ExpressionContext exprContext = new ExpressionContext(evalContext);
		HashSet<ContributionRecord> records = new HashSet<>();
		for (MMenuElement element : menuModel.getChildren()) {
			ContributionRecord record = renderer.getContributionRecord(element);
			if (record != null) {
				if (records.add(record)) {
					record.updateVisibility(evalContext);
				}
			} else {
				MenuManagerRenderer.updateVisibility(menuManager, element,
						exprContext);
			}
			if (recurseLevel > 0 && element.isVisible()
					&& element instanceof MMenu) {
				MMenu childMenu = (MMenu) element;
				MenuManager childManager = renderer.getManager(childMenu);
				if (childManager != null) {
					updateElementVisibility(childMenu, renderer, childManager,
							evalContext, recurseLevel - 1, false);
				}
			}

			if (updateEnablement && element instanceof MHandledMenuItem) {
				ParameterizedCommand cmd = ((MHandledMenuItem) element)
						.getWbCommand();
				EHandlerService handlerService = evalContext
						.get(EHandlerService.class);
				if (cmd != null && handlerService != null) {
					MHandledMenuItem item = (MHandledMenuItem) element;
					final IEclipseContext staticContext = EclipseContextFactory
							.create(MMRF_STATIC_CONTEXT);
					ContributionsAnalyzer.populateModelInterfaces(item,
							staticContext, item.getClass().getInterfaces());
					try {
						((MHandledMenuItem) element).setEnabled(handlerService
								.canExecute(cmd, staticContext));
					} finally {
						staticContext.dispose();
					}
				}
			} else if (updateEnablement && element instanceof MDirectMenuItem) {
				MDirectMenuItem contrib = (MDirectMenuItem) element;
				if (contrib.getObject() == null) {
					IContributionFactory icf = evalContext
							.get(IContributionFactory.class);

					contrib.setObject(icf.create(contrib.getContributionURI(),
							evalContext, EclipseContextFactory.create()));
				}
				if (contrib.getObject() == null) {
					continue;
				}
				MDirectMenuItem item = (MDirectMenuItem) element;
				IEclipseContext staticContext = EclipseContextFactory
						.create(MMRF_STATIC_CONTEXT);
				ContributionsAnalyzer.populateModelInterfaces(item,
						staticContext, item.getClass().getInterfaces());
				try {
					Object rc = ContextInjectionFactory.invoke(
							contrib.getObject(), CanExecute.class, evalContext,
							staticContext, Boolean.TRUE);
					if (rc instanceof Boolean) {
						contrib.setEnabled((Boolean) rc);
					}
				} finally {
					staticContext.dispose();
				}
			}
		}
	}

	void setEnabled(MHandledMenuItem item) {
		if (!item.isToBeRendered() || !item.isVisible()
				|| item.getWidget() == null) {
			return;
		}
		ParameterizedCommand cmd = item.getWbCommand();
		if (cmd == null) {
			return;
		}
		final IEclipseContext lclContext = renderer.getContext(item);
		EHandlerService service = lclContext.get(EHandlerService.class);
		final IEclipseContext staticContext = EclipseContextFactory
				.create(MMRF_STATIC_CONTEXT);
		ContributionsAnalyzer.populateModelInterfaces(item, staticContext, item
				.getClass().getInterfaces());
		try {
			item.setEnabled(service.canExecute(cmd, staticContext));
		} finally {
			staticContext.dispose();
		}
	}

	public void cleanUp(final Menu menu, MMenu menuModel,
			MenuManager menuManager) {
		trace("cleanUp", menu, null); //$NON-NLS-1$
		if (pendingCleanup.isEmpty()) {
			return;
		}
		Runnable cleanUp = pendingCleanup.remove(menu);
		if (cleanUp != null) {
			trace("cleanUp.run()", menu, null); //$NON-NLS-1$
			cleanUp.run();
		}
	}

	public void dispose() {
		Menu[] keys = pendingCleanup.keySet().toArray(
				new Menu[pendingCleanup.size()]);
		for (Menu menu : keys) {
			cleanUp(menu, null, null);
		}
	}
}
