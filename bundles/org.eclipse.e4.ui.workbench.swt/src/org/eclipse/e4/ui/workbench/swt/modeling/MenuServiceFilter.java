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
import javax.inject.Inject;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.internal.expressions.ReferenceExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
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
import org.eclipse.swt.widgets.Widget;

public class MenuServiceFilter implements Listener {
	private static final String TMP_ORIGINAL_CONTEXT = "MenuServiceFilter.original.context";
	final public static String MC_POPUP = "menuContribution:popup";
	final public static String MC_MENU = "menuContribution:menu";

	public static boolean DEBUG = true;

	private static void trace(String msg, Widget menu, MMenu menuModel) {
		System.err.println(msg + ": " + menu + ": " + menuModel);
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
	EModelService modelService;

	private HashMap<Menu, Runnable> pendingCleanup = new HashMap<Menu, Runnable>();

	public void handleEvent(Event event) {
		if (!(event.widget instanceof Menu)) {
			return;
		}
		final Menu menu = (Menu) event.widget;
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
			if (DEBUG) {
				trace("handleMenu.Show", menu, menuModel);
			}
			cleanUp(menu);
			showMenu(event, menu, menuModel);
			break;
		case SWT.Hide:
			if (DEBUG) {
				trace("handleMenu.Hide", menu, menuModel);
			}
			// TODO we'll clean up on show
			break;
		case SWT.Dispose:
			if (DEBUG) {
				trace("handleMenu.Dispose", menu, menuModel);
			}
			cleanUp(menu);
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
		gatherMenuContributions(menuModel, menuModel.getElementId(),
				toContribute, eContext, false);
		ContributionsAnalyzer.addMenuContributions(menuModel, toContribute,
				menuContributionsToRemove);

		// create a cleanup routine for the Hide or next Show
		pendingCleanup.put(menu, new Runnable() {
			public void run() {
				if (!menu.isDisposed()) {
					unrender(menuModel);
				}
				removeMenuContributions(menuModel, menuContributionsToRemove);
			}
		});
		render(menu, menuModel);
	}

	private void handleContextMenu(final Event event, final Menu menu,
			final MPopupMenu menuModel) {
		switch (event.type) {
		case SWT.Show:
			if (DEBUG) {
				trace("handleContextMenu.Show", menu, menuModel);
			}
			cleanUp(menu);
			showPopup(event, menu, menuModel);
			break;
		case SWT.Hide:
			if (DEBUG) {
				trace("handleContextMenu.Hide", menu, menuModel);
			}
			hidePopup(event, menu, menuModel);
			break;
		case SWT.Dispose:
			if (DEBUG) {
				trace("handleContextMenu.Dispose", menu, menuModel);
			}
			cleanUp(menu);
			break;
		}
	}

	public void hidePopup(Event event, Menu menu, MPopupMenu menuModel) {
		final IEclipseContext popupContext = menuModel.getContext();
		final IEclipseContext parentContext = popupContext.getParent();
		final IEclipseContext originalChild = (IEclipseContext) popupContext
				.get(TMP_ORIGINAL_CONTEXT);
		popupContext.remove(TMP_ORIGINAL_CONTEXT);
		if (!menu.isDisposed()) {
			menu.getDisplay().asyncExec(new Runnable() {
				public void run() {
					parentContext.set(IContextConstants.ACTIVE_CHILD,
							originalChild);
				}
			});
		}
	}

	public void showPopup(final Event event, final Menu menu,
			final MPopupMenu menuModel) {
		final IEclipseContext popupContext = menuModel.getContext();
		final IEclipseContext parentContext = popupContext.getParent();
		final IEclipseContext originalChild = (IEclipseContext) parentContext
				.getLocal(IContextConstants.ACTIVE_CHILD);
		parentContext.set(IContextConstants.ACTIVE_CHILD, popupContext);
		popupContext.set(TMP_ORIGINAL_CONTEXT, originalChild);

		final ArrayList<MMenuContribution> toContribute = new ArrayList<MMenuContribution>();
		final ArrayList<MMenuElement> menuContributionsToRemove = new ArrayList<MMenuElement>();
		ExpressionContext eContext = new ExpressionContext(popupContext);
		gatherMenuContributions(menuModel, menuModel.getElementId(),
				toContribute, eContext, true);

		for (String tag : menuModel.getTags()) {
			if (tag.startsWith("popup:") && tag.length() > 6) {
				gatherMenuContributions(menuModel, tag.substring(6),
						toContribute, eContext, false);
			}
		}
		ContributionsAnalyzer.addMenuContributions(menuModel, toContribute,
				menuContributionsToRemove);

		// create a cleanup routine for the Hide or next Show
		pendingCleanup.put(menu, new Runnable() {
			public void run() {
				if (!menu.isDisposed()) {
					unrender(menuModel);
				}
				removeMenuContributions(menuModel, menuContributionsToRemove);
			}
		});
		render(menu, menuModel);
	}

	private void render(final Menu menu, final MMenu menuModel) {
		if (DEBUG) {
			trace("render", menu, menuModel);
		}
		MMenuElement lastVisibleItem = null;
		for (MMenuElement element : menuModel.getChildren()) {
			boolean skip = element instanceof MMenuSeparator
					&& lastVisibleItem instanceof MMenuSeparator;
			if (!skip) {
				renderer.createGui(element, menu);
				if (element.isVisible()) {
					lastVisibleItem = element;
				}
			}
		}
	}

	private void unrender(final MMenu menuModel) {
		if (DEBUG) {
			trace("unrender", (Widget) menuModel.getWidget(), menuModel);
		}
		for (MMenuElement element : menuModel.getChildren()) {
			renderer.removeGui(element);
		}
	}

	private void gatherMenuContributions(final MMenu menuModel,
			final String id, final ArrayList<MMenuContribution> toContribute,
			final ExpressionContext eContext, boolean includePopups) {
		for (MMenuContribution menuContribution : application
				.getMenuContributions()) {
			String parentID = menuContribution.getParentID();
			boolean popup = parentID.equals("popup")
					&& (menuModel instanceof MPopupMenu) && includePopups;
			boolean filtered = isFiltered(menuModel, menuContribution);
			if (filtered || (!popup && !parentID.equals(id))
					|| !menuContribution.isToBeRendered()) {
				continue;
			}
			if (isVisible(menuContribution, eContext)) {
				toContribute.add(menuContribution);
			}
		}
	}

	private boolean isFiltered(MMenu menuModel,
			MMenuContribution menuContribution) {
		if (menuModel.getTags().contains(MC_POPUP)) {
			return !menuContribution.getTags().contains(MC_POPUP)
					&& menuContribution.getTags().contains(MC_MENU);
		}
		if (menuModel.getTags().contains(MC_MENU)) {
			return !menuContribution.getTags().contains(MC_MENU)
					&& menuContribution.getTags().contains(MC_POPUP);
		}
		return false;
	}

	private void removeMenuContributions(final MMenu menuModel,
			final ArrayList<MMenuElement> menuContributionsToRemove) {
		for (MMenuElement item : menuContributionsToRemove) {
			if (DEBUG) {
				trace("removeMenuContributions " + item,
						(Widget) menuModel.getWidget(), menuModel);
			}
			menuModel.getChildren().remove(item);
		}
	}

	private void handlerRenderedMenu(final Event event, final Menu menu,
			final MRenderedMenu menuModel) {
		// Do nothing here for the moment, except process any cleanups
		switch (event.type) {
		case SWT.Show:
			if (DEBUG) {
				trace("handlerRenderedMenu.Show", menu, menuModel);
			}
			cleanUp(menu);
			showRenderedMenu(event, menu, menuModel);
			break;
		case SWT.Hide:
			if (DEBUG) {
				trace("handlerRenderedMenu.Hide", menu, menuModel);
			}
			// TODO don't care
			break;
		case SWT.Dispose:
			if (DEBUG) {
				trace("handlerRenderedMenu.Dispose", menu, menuModel);
			}
			cleanUp(menu);
			break;
		}
	}

	public void showRenderedMenu(final Event event, final Menu menu,
			final MRenderedMenu menuModel) {
		if (!(menuModel.getContributionManager() instanceof MenuManager)) {
			return;
		}

		MenuManager manager = (MenuManager) menuModel.getContributionManager();
		if (DEBUG) {
			trace("showRenderedMenu: " + manager, menu, menuModel);
		}
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
		if (DEBUG) {
			trace("cleanUp", menu, null);
		}
		if (pendingCleanup.isEmpty()) {
			return;
		}
		Runnable cleanUp = pendingCleanup.remove(menu);
		if (cleanUp != null) {
			if (DEBUG) {
				trace("cleanUp.run()", menu, null);
			}
			cleanUp.run();
		}
	}

	private boolean isVisible(MMenuContribution menuContribution,
			ExpressionContext eContext) {
		if (menuContribution.getVisibleWhen() == null) {
			return true;
		}
		MCoreExpression exp = (MCoreExpression) menuContribution
				.getVisibleWhen();
		Expression ref = null;
		if (exp.getCoreExpression() instanceof Expression) {
			ref = (Expression) exp.getCoreExpression();
		} else {
			ref = new ReferenceExpression(exp.getCoreExpressionId());
			exp.setCoreExpression(ref);
		}
		try {
			return ref.evaluate(eContext) != EvaluationResult.FALSE;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void dispose() {
		Menu[] keys = pendingCleanup.keySet().toArray(
				new Menu[pendingCleanup.size()]);
		for (Menu menu : keys) {
			cleanUp(menu);
		}
	}
}