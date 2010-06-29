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

import org.eclipse.e4.ui.internal.workbench.ExtensionPointProxy;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class RenderedMenuItemRenderer extends SWTPartRenderer {

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
		int index = menu.getItemCount();
		item.fill(menu, index);

		if (index == menu.getItemCount()) {
			return null;
		}
		return menu.getItem(index);
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
}
