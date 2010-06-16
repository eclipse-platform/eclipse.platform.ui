/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

public class DetachedDropAgent extends DropAgent {
	private EModelService modelService;
	private Rectangle curRect;

	public DetachedDropAgent(EModelService modelSvc) {
		modelService = modelSvc;
	}

	@Override
	public boolean canDrop(MUIElement dragElement, CursorInfo info) {
		if (dragElement instanceof MPart && info.curElement == null)
			return true;

		return false;
	}

	@Override
	public boolean drop(MUIElement dragElement, CursorInfo info) {
		if (dragElement.getCurSharedRef() != null)
			dragElement = dragElement.getCurSharedRef();

		modelService.detach((MPartSashContainerElement) dragElement, curRect.x, curRect.y,
				curRect.width, curRect.height);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.ui.renderers.swt.dnd.DropAgent#getRectangle
	 * (org.eclipse.e4.ui.model.application.ui.MUIElement,
	 * org.eclipse.e4.workbench.ui.renderers.swt.dnd.CursorInfo)
	 */
	@Override
	public Rectangle getRectangle(MUIElement dragElement, CursorInfo info) {
		if (dragElement.getCurSharedRef() != null)
			dragElement = dragElement.getCurSharedRef();
		MUIElement parentME = dragElement.getParent();
		Control ctrl = (Control) parentME.getWidget();
		curRect = ctrl.getBounds();

		// Try to take the window's trim into account
		curRect.width += 10;
		curRect.height += 22;

		Point cp = ctrl.getDisplay().getCursorLocation();
		curRect.x = cp.x - (curRect.width / 2);
		curRect.y = cp.y - (curRect.height / 2);

		return curRect;
	}

}
