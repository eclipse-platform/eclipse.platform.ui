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

import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class RenderedToolBarRenderer extends SWTPartRenderer {

	@Inject
	private IPresentationEngine engine;

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MRenderedToolBar)
				|| !(parent instanceof Composite))
			return null;

		MRenderedToolBar toolBar = (MRenderedToolBar) element;
		if (!(toolBar.getContributionManager() instanceof ToolBarManager)) {
			return null;
		}

		ToolBarManager tbm = (ToolBarManager) toolBar.getContributionManager();
		ToolBar tb = tbm.createControl((Composite) parent);
		tbm.update(true);
		tb.setData(ToolBarManager.class.getName(), tbm);
		
		for (MToolBarElement child : toolBar.getChildren()) {
			engine.createGui(child, tb);
		}
		tb.getParent().layout(true);

		return tb;
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
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#processContents
	 * (org.eclipse.e4.ui.model.application.ui.MElementContainer)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> container) {
		// We've delegated further rendering to the ContributionManager
		// it's their fault the menu items don't show up!
	}
}
