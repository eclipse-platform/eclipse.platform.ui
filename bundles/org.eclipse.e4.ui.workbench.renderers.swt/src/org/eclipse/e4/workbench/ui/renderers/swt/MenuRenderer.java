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
package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class MenuRenderer extends SWTPartRenderer {

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MMenu))
			return null;

		MMenu menuModel = (MMenu) element;

		Menu newMenu = null;
		if (parent instanceof Decorations) {
			MUIElement container = (MUIElement) ((EObject) element)
					.eContainer();
			if (container instanceof MWindow)
				newMenu = new Menu((Decorations) parent, SWT.BAR);
			else {
				newMenu = new Menu((Decorations) parent, SWT.POP_UP);
			}
		} else if (parent instanceof Menu) {
			MenuItem newItem = new MenuItem((Menu) parent, SWT.CASCADE);
			setItemText(menuModel, newItem);
			newItem.setImage(getImage(menuModel));
			newItem.setEnabled(menuModel.isEnabled());
			return newItem;
		} else if (parent instanceof Control) {
			newMenu = new Menu((Control) parent);
		}

		return newMenu;
	}

	private void setItemText(MMenu model, MenuItem item) {
		String text = model.getLabel();
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		item.setText(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer#hideChild
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
	 * org.eclipse.e4.workbench.ui.renderers.AbstractPartRenderer#getUIContainer
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

}
