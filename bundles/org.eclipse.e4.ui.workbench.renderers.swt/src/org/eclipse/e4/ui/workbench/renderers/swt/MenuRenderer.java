/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class MenuRenderer extends SWTPartRenderer {
	private MApplication application;
	private HashMap<MMenu, ArrayList<ArrayList<MMenuElement>>> pendingCleanup = new HashMap<MMenu, ArrayList<ArrayList<MMenuElement>>>();

	@Override
	public void init(IEclipseContext context) {
		super.init(context);
		application = context.get(MApplication.class);
	}

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MMenu))
			return null;

		final MMenu menuModel = (MMenu) element;

		Menu newMenu = null;
		if (parent instanceof Decorations) {
			MUIElement container = (MUIElement) ((EObject) element)
					.eContainer();
			if (container instanceof MWindow) {
				newMenu = new Menu((Decorations) parent, SWT.BAR);
				newMenu.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						cleanUp(menuModel);
					}
				});
			} else {
				newMenu = new Menu((Decorations) parent, SWT.POP_UP);
			}
		} else if (parent instanceof Menu) {
			int addIndex = calcVisibleIndex(menuModel);
			MenuItem newItem = new MenuItem((Menu) parent, SWT.CASCADE,
					addIndex);
			setItemText(menuModel, newItem);
			newItem.setImage(getImage(menuModel));
			newItem.setEnabled(menuModel.isEnabled());
			return newItem;
		} else if (parent instanceof Control) {
			newMenu = new Menu((Control) parent);
		}

		return newMenu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#processContents
	 * (org.eclipse.e4.ui.model.application.ui.MElementContainer)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> container) {
		if (container.getChildren().size() == 0) {
			Object obj = container.getWidget();
			if (obj instanceof MenuItem) {
				MenuItem mi = (MenuItem) obj;
				if (mi.getMenu() == null) {
					mi.setMenu(new Menu(mi));
				}
				Menu menu = mi.getMenu();
				MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText(MenuManagerRendererFilter.NUL_MENU_ITEM);
				menuItem.setEnabled(false);
			}
		}

		super.processContents(container);

		Object obj = container.getWidget();
		Object menuObj = container;
		if ((obj instanceof Menu) && (((Menu) obj).getStyle() & SWT.BAR) != 0
				&& (menuObj instanceof MMenu)) {
			MMenu menuModel = (MMenu) menuObj;
			IEclipseContext ctx = getContext(container);
			ExpressionContext eContext = new ExpressionContext(ctx);
			ArrayList<MMenuContribution> toContribute = new ArrayList<MMenuContribution>();
			ContributionsAnalyzer.gatherMenuContributions(menuModel,
					application.getMenuContributions(),
					menuModel.getElementId(), toContribute, eContext, false);
			addMenuBarContributions(menuModel, toContribute, ctx, eContext);
		}
	}

	// this is similar in nature to:
	// org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer.addMenuContributions(MMenu,
	// ArrayList<MMenuContribution>, ArrayList<MMenuElement>)
	// the difference is it needs to add all the contributions and manage their
	// visiblility through a RAT
	private void addMenuBarContributions(final MMenu menuModel,
			ArrayList<MMenuContribution> toContribute,
			final IEclipseContext ctx, final ExpressionContext eContext) {
		HashSet<String> existingMenuIds = new HashSet<String>();
		HashSet<String> existingSeparatorNames = new HashSet<String>();
		for (MMenuElement child : menuModel.getChildren()) {
			String elementId = child.getElementId();
			if (child instanceof MMenu && elementId != null) {
				existingMenuIds.add(elementId);
			} else if (child instanceof MMenuSeparator && elementId != null) {
				existingSeparatorNames.add(elementId);
			}
		}

		boolean done = toContribute.size() == 0;
		while (!done) {
			ArrayList<MMenuContribution> curList = new ArrayList<MMenuContribution>(
					toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (final MMenuContribution menuContribution : curList) {
				final ArrayList<MMenuElement> menuContributionsToRemove = new ArrayList<MMenuElement>();
				if (!ContributionsAnalyzer.processAddition(menuModel,
						menuContributionsToRemove, menuContribution,
						existingMenuIds, existingSeparatorNames)) {
					toContribute.add(menuContribution);
				} else {
					if (menuContribution.getVisibleWhen() != null) {
						ctx.runAndTrack(new RunAndTrack() {
							@Override
							public boolean changed(IEclipseContext context) {
								if (!menuModel.isToBeRendered()
										|| !menuModel.isVisible()
										|| menuModel.getWidget() == null) {
									return false;
								}
								boolean rc = ContributionsAnalyzer.isVisible(
										menuContribution, eContext);
								for (MMenuElement element : menuContributionsToRemove) {
									element.setToBeRendered(rc);
								}
								return true;
							}
						});
					}
					ArrayList<ArrayList<MMenuElement>> lists = pendingCleanup
							.get(menuModel);
					if (lists == null) {
						lists = new ArrayList<ArrayList<MMenuElement>>();
						pendingCleanup.put(menuModel, lists);
					}
					lists.add(menuContributionsToRemove);
				}
			}
			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = (toContribute.size() == 0)
					|| (toContribute.size() == retryCount);
		}
	}

	private void setItemText(MMenu model, MenuItem item) {
		String text = model.getLocalizedLabel();
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		item.setText(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#hideChild
	 * (org.eclipse.e4.ui.model.application.MElementContainer,
	 * org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void hideChild(MElementContainer<MUIElement> parentElement,
			MUIElement child) {
		super.hideChild(parentElement, child);

		// Since there's no place to 'store' a child that's not in a menu
		// we'll blow it away and re-create on an add
		Widget widget = (Widget) child.getWidget();
		if (widget != null && !widget.isDisposed())
			widget.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.AbstractPartRenderer#getUIContainer
	 * (org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public Object getUIContainer(MUIElement element) {
		if (!(element instanceof MMenuElement))
			return null;

		if (element.getParent().getWidget() instanceof MenuItem) {
			MenuItem mi = (MenuItem) element.getParent().getWidget();
			if (mi.getMenu() == null) {
				mi.setMenu(new Menu(mi));
			}
			return mi.getMenu();
		}

		return super.getUIContainer(element);
	}

	void cleanUp(MMenu menuModel) {
		ArrayList<ArrayList<MMenuElement>> lists = pendingCleanup
				.remove(menuModel);
		if (lists == null) {
			return;
		}
		for (ArrayList<MMenuElement> list : lists) {
			for (MMenuElement item : list) {
				menuModel.getChildren().remove(item);
			}
		}
	}
}
