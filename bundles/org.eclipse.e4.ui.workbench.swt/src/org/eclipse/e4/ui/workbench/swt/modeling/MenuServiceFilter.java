/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.modeling;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

public class MenuServiceFilter implements Listener {
	public static final String NUL_MENU_ITEM = "(None Applicable)"; //$NON-NLS-1$

	private static final String TMP_ORIGINAL_CONTEXT = "MenuServiceFilter.original.context";

	private static void trace(String msg, Widget menu, MMenu menuModel) {
		WorkbenchSWTActivator.trace(Policy.MENUS, msg + ": " + menu + ": "
				+ menuModel, null);
	}

	private static Method aboutToShow;

	public static Method getAboutToShow() {
		if (aboutToShow == null) {
			try {
				aboutToShow = MenuManager.class
						.getDeclaredMethod("handleAboutToShow");
				aboutToShow.setAccessible(true);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return aboutToShow;
	}

	@Inject
	private MApplication application;

	@Inject
	private IPresentationEngine renderer;

	@Inject
	private Logger logger;

	@Inject
	EModelService modelService;

	private HashMap<Menu, Runnable> pendingCleanup = new HashMap<Menu, Runnable>();

	public void handleEvent(final Event event) {
		// wrap the handling in a SafeRunner so that exceptions do not prevent
		// the menu from being shown
		SafeRunner.run(new ISafeRunnable() {
			public void handleException(Throwable e) {
				if (e instanceof Error) {
					// errors are deadly, we shouldn't ignore these
					throw (Error) e;
				} else {
					// log exceptions otherwise
					if (logger != null) {
						logger.error(e);
					}
				}
			}

			public void run() throws Exception {
				safeHandleEvent(event);
			}
		});
	}

	private void safeHandleEvent(Event event) {
		if (!(event.widget instanceof Menu)) {
			return;
		}
		final Menu menu = (Menu) event.widget;
		if (event.type == SWT.Dispose) {
			trace("handleMenu.Dispose", menu, null);
			cleanUp(menu);
		}
		Object obj = menu.getData(AbstractPartRenderer.OWNING_ME);
		if (obj == null && menu.getParentItem() != null) {
			obj = menu.getParentItem().getData(AbstractPartRenderer.OWNING_ME);
		}
		if (obj instanceof MRenderedMenu) {
			handlerRenderedMenu(event, menu, (MRenderedMenu) obj);
		} else if (obj instanceof MPopupMenu) {
			handleContextMenu(event, menu, (MPopupMenu) obj);
		} else if (obj instanceof MMenu) {
			handleMenu(event, menu, (MMenu) obj);
		}
	}

	private void handleMenu(final Event event, final Menu menu,
			final MMenu menuModel) {
		if ((menu.getStyle() & SWT.BAR) != 0) {
			// don't process the menu bar, it's not fair :-)
			return;
		}
		switch (event.type) {
		case SWT.Show:
			trace("handleMenu.Show", menu, menuModel);
			cleanUp(menu);
			showMenu(event, menu, menuModel);
			break;
		case SWT.Hide:
			trace("handleMenu.Hide", menu, menuModel);
			// TODO we'll clean up on show
			break;
		}
	}

	public void showMenu(final Event event, final Menu menu,
			final MMenu menuModel) {
		final IEclipseContext parentContext = modelService
				.getContainingContext(menuModel);

		final ArrayList<MMenuContribution> toContribute = new ArrayList<MMenuContribution>();
		final ArrayList<MMenuElement> menuContributionsToRemove = new ArrayList<MMenuElement>();
		ExpressionContext eContext = new ExpressionContext(parentContext);
		ContributionsAnalyzer.gatherMenuContributions(menuModel,
				application.getMenuContributions(), menuModel.getElementId(),
				toContribute, eContext, false);
		if (menu.getItemCount() == 1) {
			MenuItem item = menu.getItem(0);
			if (NUL_MENU_ITEM.equals(item.getText())) {
				item.dispose();
			}
		}
		ContributionsAnalyzer.addMenuContributions(menuModel, toContribute,
				menuContributionsToRemove);

		// create a cleanup routine for the Hide or next Show
		pendingCleanup.put(menu, new Runnable() {
			public void run() {
				if (!menu.isDisposed()) {
					unrender(menuContributionsToRemove);
				}
				removeMenuContributions(menuModel, menuContributionsToRemove);
			}
		});
		render(menu, menuModel);
		if (menu.getItemCount() == 0) {
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(NUL_MENU_ITEM);
			menuItem.setEnabled(false);
		}
	}

	private void handleContextMenu(final Event event, final Menu menu,
			final MPopupMenu menuModel) {
		switch (event.type) {
		case SWT.Show:
			trace("handleContextMenu.Show", menu, menuModel);
			cleanUp(menu);
			showPopup(event, menu, menuModel);
			break;
		case SWT.Hide:
			trace("handleContextMenu.Hide", menu, menuModel);
			hidePopup(event, menu, menuModel);
			break;
		}
	}

	public void hidePopup(Event event, Menu menu, MPopupMenu menuModel) {
		final IEclipseContext popupContext = menuModel.getContext();
		final IEclipseContext originalChild = (IEclipseContext) popupContext
				.get(TMP_ORIGINAL_CONTEXT);
		popupContext.remove(TMP_ORIGINAL_CONTEXT);
		if (!menu.isDisposed()) {
			menu.getDisplay().asyncExec(new Runnable() {
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

	public void showPopup(final Event event, final Menu menu,
			final MPopupMenu menuModel) {
		final IEclipseContext popupContext = menuModel.getContext();
		final IEclipseContext parentContext = popupContext.getParent();
		final IEclipseContext originalChild = parentContext.getActiveChild();
		popupContext.activate();
		popupContext.set(TMP_ORIGINAL_CONTEXT, originalChild);

		final ArrayList<MMenuContribution> toContribute = new ArrayList<MMenuContribution>();
		final ArrayList<MMenuElement> menuContributionsToRemove = new ArrayList<MMenuElement>();
		ExpressionContext eContext = new ExpressionContext(popupContext);
		ContributionsAnalyzer.gatherMenuContributions(menuModel,
				application.getMenuContributions(), menuModel.getElementId(),
				toContribute, eContext, true);

		for (String tag : menuModel.getTags()) {
			if (tag.startsWith("popup:") && tag.length() > 6) {
				ContributionsAnalyzer.gatherMenuContributions(menuModel,
						application.getMenuContributions(), tag.substring(6),
						toContribute, eContext, false);
			}
		}
		ContributionsAnalyzer.addMenuContributions(menuModel, toContribute,
				menuContributionsToRemove);

		// create a cleanup routine for the Hide or next Show
		pendingCleanup.put(menu, new Runnable() {
			public void run() {
				if (!menu.isDisposed()) {
					unrender(menuContributionsToRemove);
				}
				removeMenuContributions(menuModel, menuContributionsToRemove);
			}
		});
		render(menu, menuModel);
	}

	private void render(final Menu menu, final MMenu menuModel) {
		trace("render", menu, menuModel);
		for (MMenuElement element : menuModel.getChildren()) {
			renderer.createGui(element, menu, null);
			if (element instanceof MHandledMenuItem) {
				setEnabled((MHandledMenuItem) element);
			}
		}
	}

	private void setEnabled(MHandledMenuItem item) {
		if (!item.isToBeRendered() || !item.isVisible()
				|| item.getWidget() == null) {
			return;
		}
		ParameterizedCommand cmd = item.getWbCommand();
		if (cmd == null) {
			return;
		}
		final IEclipseContext lclContext = modelService
				.getContainingContext(item);
		EHandlerService service = lclContext.get(EHandlerService.class);
		item.setEnabled(service.canExecute(cmd));
	}

	private void unrender(final List<MMenuElement> menuModel) {
		trace("unrender", null, null);
		for (MMenuElement element : menuModel) {
			renderer.removeGui(element);
		}
	}

	private void removeMenuContributions(final MMenu menuModel,
			final ArrayList<MMenuElement> menuContributionsToRemove) {
		for (MMenuElement item : menuContributionsToRemove) {
			trace("removeMenuContributions " + item,
					(Widget) menuModel.getWidget(), menuModel);
			menuModel.getChildren().remove(item);
		}
	}

	private void handlerRenderedMenu(final Event event, final Menu menu,
			final MRenderedMenu menuModel) {
		// Do nothing here for the moment, except process any cleanups
		switch (event.type) {
		case SWT.Show:
			trace("handlerRenderedMenu.Show", menu, menuModel);
			cleanUp(menu);
			showRenderedMenu(event, menu, menuModel);
			break;
		case SWT.Hide:
			trace("handlerRenderedMenu.Hide", menu, menuModel);
			// TODO don't care
			break;
		}
	}

	public void showRenderedMenu(final Event event, final Menu menu,
			final MRenderedMenu menuModel) {
		if (!(menuModel.getContributionManager() instanceof MenuManager)) {
			return;
		}

		MenuManager manager = (MenuManager) menuModel.getContributionManager();
		Method handleAboutToShow = getAboutToShow();
		try {
			handleAboutToShow.invoke(manager);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (menuModel.getChildren().size() == 1
				&& menuModel.getChildren().get(0) instanceof MPopupMenu) {
			showPopup(event, menu, (MPopupMenu) menuModel.getChildren().get(0));
		} else {
			showMenu(event, menu, menuModel);
		}
		event.type = SWT.None;
		event.doit = false;
	}

	public void cleanUp(final Menu menu) {
		trace("cleanUp", menu, null);
		if (pendingCleanup.isEmpty()) {
			return;
		}
		Runnable cleanUp = pendingCleanup.remove(menu);
		if (cleanUp != null) {
			trace("cleanUp.run()", menu, null);
			cleanUp.run();
		}
	}

	public void dispose() {
		Menu[] keys = pendingCleanup.keySet().toArray(
				new Menu[pendingCleanup.size()]);
		for (Menu menu : keys) {
			cleanUp(menu);
		}
	}
}
