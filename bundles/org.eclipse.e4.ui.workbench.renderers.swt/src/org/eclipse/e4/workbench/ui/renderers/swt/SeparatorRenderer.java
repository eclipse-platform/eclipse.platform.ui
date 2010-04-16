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

		if (element instanceof MMenuSeparator) {
			Object widget = element.getParent().getWidget();
			Menu menu = widget instanceof Menu ? (Menu) widget
					: ((MenuItem) widget).getMenu();
			newSep = new MenuItem(menu, SWT.SEPARATOR);
		} else if (element instanceof MToolBarSeparator) {
			ToolBar tb = (ToolBar) element.getParent().getWidget();
			newSep = new ToolItem(tb, SWT.SEPARATOR);
		}

		return newSep;
	}
}
