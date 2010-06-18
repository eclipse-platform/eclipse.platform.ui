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

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class SeparatorRenderer extends SWTPartRenderer {

	public Object createWidget(final MUIElement element, Object parent) {
		Widget newSep = null;
		if (!element.isVisible()) {
			return null;
		}
		if (element instanceof MMenuSeparator) {
			Menu menu = null;
			Object widget = element.getParent().getWidget();
			if (widget instanceof Menu) {
				menu = (Menu) widget;
			} else if (widget instanceof MenuItem) {
				menu = ((MenuItem) widget).getMenu();
			}
			if (menu != null) {
				int objIndex = calcIndex(element);
				if (objIndex == element.getParent().getChildren().size() - 1) {
					return null;
				}
				if (element.getParent().getChildren().get(objIndex + 1) instanceof MMenuSeparator) {
					return null;
				}
				// determine the index at which we should create the new item
				int addIndex = calcVisibleIndex(element);
				// this shouldn't happen, but it might
				newSep = new MenuItem(menu, SWT.SEPARATOR, addIndex);
			}
		} else if (element instanceof MToolBarSeparator) {
			ToolBar tb = parent instanceof ToolBar ? (ToolBar) parent
					: (ToolBar) element.getParent().getWidget();
			// determine the index at which we should create the new item
			int addIndex = calcVisibleIndex(element);
			newSep = new ToolItem(tb, SWT.SEPARATOR, addIndex);
		}

		return newSep;
	}
}
