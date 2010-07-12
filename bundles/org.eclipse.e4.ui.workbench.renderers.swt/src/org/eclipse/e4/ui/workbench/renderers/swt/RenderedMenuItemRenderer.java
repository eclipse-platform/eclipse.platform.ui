/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.e4.ui.internal.workbench.ExtensionPointProxy;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class RenderedMenuItemRenderer extends SWTPartRenderer {

	private static Method aboutToShow;

	private static Method getAboutToShow() {
		if (aboutToShow == null) {
			try {
				aboutToShow = MenuManager.class
						.getDeclaredMethod("handleAboutToShow"); //$NON-NLS-1$
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

	private Map<IContributionItem, List<MenuRecord>> map = new WeakHashMap<IContributionItem, List<MenuRecord>>();

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MRenderedMenuItem)
				|| !(parent instanceof Menu)) {
			return null;
		}

		MRenderedMenuItem menuModel = (MRenderedMenuItem) element;
		Object contribution = menuModel.getContributionItem();
		if (contribution instanceof ExtensionPointProxy) {
			ExtensionPointProxy proxy = (ExtensionPointProxy) contribution;
			Object delegate = proxy.createDelegate(menuModel);
			if (delegate != null) {
				proxy.setField("dirty", Boolean.TRUE); //$NON-NLS-1$
				return fill((IContributionItem) delegate, (Menu) parent);
			}
		} else if (contribution instanceof IContributionItem) {
			return fill((IContributionItem) contribution, (Menu) parent);
		}
		return null;
	}

	private Object fill(IContributionItem item, Menu menu) {
		ContributionItem contribution = (ContributionItem) item;
		MenuManager manager = (MenuManager) contribution.getParent();
		try {
			if (manager == null) {
				manager = new MenuManager();
				manager.add(contribution);
			}
			getAboutToShow().invoke(manager, new Object[0]);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		int itemCount = menu.getItemCount();
		List<MenuRecord> list = map.get(item);
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				MenuRecord record = list.get(i);
				Menu storedMenu = record.getMenu();
				if (storedMenu.isDisposed()) {
					list.remove(i);
					i--;
				} else if (storedMenu == menu) {
					record.dispose();
					itemCount = menu.getItemCount();
					list.remove(i);
					break;
				}
			}
		}

		item.fill(menu, itemCount);
		int endIndex = menu.getItemCount();

		if (list == null) {
			if (itemCount != endIndex) {
				list = new ArrayList<MenuRecord>();
				MenuRecord record = new MenuRecord(menu);
				for (int i = itemCount; i < endIndex; i++) {
					record.addItem(menu.getItem(i));
				}
				list.add(record);
				map.put(item, list);
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				MenuRecord record = list.get(i);
				if (record.getMenu() == menu) {
					list.remove(i);
					record = new MenuRecord(menu);
					for (int j = itemCount; j < endIndex; j++) {
						record.addItem(menu.getItem(j));
					}
					list.add(record);
					return null;
				}
			}

			MenuRecord record = new MenuRecord(menu);
			for (int i = itemCount; i < endIndex; i++) {
				record.addItem(menu.getItem(i));
			}
			list.add(record);
			map.put(item, list);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#unbindWidget
	 * (org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public Object unbindWidget(MUIElement me) {
		MRenderedMenuItem item = (MRenderedMenuItem) me;
		Object contributionItem = item.getContributionItem();
		if (contributionItem instanceof ExtensionPointProxy) {
			ExtensionPointProxy proxy = (ExtensionPointProxy) contributionItem;
			Object delegate = proxy.getDelegate();
			if (delegate instanceof IContributionItem) {
				((IContributionItem) delegate).dispose();
			}
		} else if (contributionItem instanceof IContributionItem) {
			((IContributionItem) contributionItem).dispose();
		}
		return super.unbindWidget(me);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#processContents
	 * (org.eclipse.e4.ui.model.application.ui.MElementContainer)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> container) {
		// We've delegated further rendering to the ContributionManager
		// it's their fault the menu items don't show up!
	}

	class MenuRecord {

		private Menu menu;
		private List<MenuItem> items = new ArrayList<MenuItem>();

		public MenuRecord(Menu menu) {
			this.menu = menu;
		}

		public Menu getMenu() {
			return menu;
		}

		public void addItem(MenuItem item) {
			items.add(item);
		}

		public void dispose() {
			for (MenuItem item : items) {
				item.dispose();
			}
		}
	}
}
