/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class RenderedMenuRenderer extends SWTPartRenderer {

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MRenderedMenu))
			return null;

		MRenderedMenu menuModel = (MRenderedMenu) element;
		Object contributionManager = menuModel.getContributionManager();
		if (contributionManager instanceof MenuManager) {
			Menu newMenu = null;
			MenuManager mm = (MenuManager) contributionManager;
			MUIElement container = (MUIElement) ((EObject) menuModel)
					.eContainer();
			if (parent instanceof Decorations && container instanceof MWindow) {
				newMenu = mm.createMenuBar((Decorations) parent);
			} else if (parent instanceof Menu) {
				mm.update(true);
				newMenu = mm.getMenu();
			} else {
				newMenu = mm.createContextMenu((Control) parent);
			}
			mm.update(true);
			return newMenu;
		} else if (contributionManager instanceof IMenuCreator) {
			final IMenuCreator creator = (IMenuCreator) contributionManager;
			if (parent instanceof Control) {
				Control control = (Control) parent;
				return creator.getMenu(control);
			} else if (parent instanceof Menu) {
				int addIndex = calcVisibleIndex(menuModel);
				MenuItem newItem = new MenuItem((Menu) parent, SWT.CASCADE,
						addIndex);
				setItemText(menuModel, newItem);
				newItem.setImage(getImage(menuModel));
				newItem.setEnabled(menuModel.isEnabled());
				Menu menu = (Menu) parent;
				newItem.setMenu(creator.getMenu(menu));
				return newItem;
			}
		}

		return null;
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

	@Override
	public Object unbindWidget(MUIElement me) {
		MRenderedMenu menuModel = (MRenderedMenu) me;
		Object contributionManager = menuModel.getContributionManager();
		if (contributionManager instanceof IMenuManager) {
			((IMenuManager) contributionManager).dispose();
		}
		return super.unbindWidget(me);
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
