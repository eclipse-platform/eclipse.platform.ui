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

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class MenuService implements EMenuService {
	@Inject
	private MPart myPart;

	public MPopupMenu registerContextMenu(Object parent, String menuId) {
		if (!(parent instanceof Control)) {
			return null;
		}
		Control parentControl = (Control) parent;
		for (MMenu mmenu : myPart.getMenus()) {
			if (menuId.equals(mmenu.getElementId())
					&& mmenu instanceof MPopupMenu) {
				if (registerMenu(parentControl, (MPopupMenu) mmenu)) {
					return (MPopupMenu) mmenu;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	private boolean registerMenu(final Control parentControl,
			final MPopupMenu mmenu) {
		if (mmenu.getWidget() != null) {
			return false;
		}
		// we need to delegate to the renderer so that it "processes" the
		// MenuManager correctly
		IRendererFactory rendererFactory = myPart.getContext().get(
				IRendererFactory.class);
		AbstractPartRenderer renderer = rendererFactory.getRenderer(mmenu,
				parentControl);
		IEclipseContext popupContext = myPart.getContext().createChild(
				"popup:" + mmenu.getElementId());
		mmenu.setContext(popupContext);
		Object widget = renderer.createWidget(mmenu, parentControl);
		if (!(widget instanceof Menu)) {
			return false;
		}
		renderer.bindWidget(mmenu, widget);

		return true;
	}
}
