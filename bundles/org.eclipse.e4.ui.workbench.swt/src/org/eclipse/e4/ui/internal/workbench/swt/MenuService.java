/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class MenuService implements EMenuService {
	@Inject
	private MPart myPart;

	@Override
	public boolean registerContextMenu(Object parent, String menuId) {
		if (!(parent instanceof Control)) {
			return false;
		}
		Control parentControl = (Control) parent;
		for (MMenu mmenu : myPart.getMenus()) {
			if (menuId.equals(mmenu.getElementId())
					&& mmenu instanceof MPopupMenu) {
				Menu menu = registerMenu(parentControl, (MPopupMenu) mmenu, myPart.getContext());
				if (menu != null) {
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public static Menu registerMenu(final Control parentControl, final MPopupMenu mmenu, IEclipseContext context) {
		if (mmenu.getWidget() != null) {
			return (Menu) mmenu.getWidget();
		}
		// we need to delegate to the renderer so that it "processes" the
		// MenuManager correctly
		IRendererFactory rendererFactory = context.get(IRendererFactory.class);
		AbstractPartRenderer renderer = rendererFactory.getRenderer(mmenu, parentControl);
		mmenu.setRenderer(renderer);
		IEclipseContext popupContext = context.createChild("popup:" + mmenu.getElementId());
		mmenu.setContext(popupContext);
		if (mmenu.getParent() == null) {
			mmenu.getTransientData().put(IPresentationEngine.RENDERING_PARENT_KEY, parentControl);
		}
		Object widget = renderer.createWidget(mmenu, parentControl);
		if (!(widget instanceof Menu)) {
			mmenu.getTransientData().remove(IPresentationEngine.RENDERING_PARENT_KEY);
			return null;
		}
		renderer.bindWidget(mmenu, widget);
		renderer.hookControllerLogic(mmenu);

		// Process its internal structure through the renderer that created
		// it
		Object castObject = mmenu;
		@SuppressWarnings("unchecked")
		MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) castObject;
		renderer.processContents(container);

		// Allow a final chance to set up
		renderer.postProcess(mmenu);

		// Now that we have a widget let the parent (if any) know
		MElementContainer<MUIElement> parentElement = mmenu.getParent();
		if (parentElement != null) {
			AbstractPartRenderer parentRenderer = rendererFactory.getRenderer(parentElement, null);
			if (parentRenderer != null) {
				parentRenderer.childRendered(parentElement, mmenu);
			}
		}

		return (Menu) widget;
	}
}
