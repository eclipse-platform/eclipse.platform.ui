/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class DetachedDropAgent extends DropAgent {
	DnDManager manager;
	private EModelService modelService;
	private Rectangle curRect;

	public DetachedDropAgent(DnDManager manager) {
		super(manager);
		this.manager = manager;
		modelService = manager.getModelService();
	}

	@Override
	public boolean canDrop(MUIElement dragElement, DnDInfo info) {
		if (info.curElement != null)
			return false;

		if (dragElement instanceof MPart || dragElement instanceof MPlaceholder
				|| dragElement instanceof MPartStack)
			return true;

		return false;
	}

	@Override
	public boolean drop(MUIElement dragElement, DnDInfo info) {
		// Ensure that the stack is restored first if minimizdd
		if (dragElement.getTags().contains(IPresentationEngine.MINIMIZED)) {
			dragElement.getTags().remove(IPresentationEngine.MINIMIZED);
		}

		if (dragElement.getCurSharedRef() != null)
			dragElement = dragElement.getCurSharedRef();

		modelService.detach((MPartSashContainerElement) dragElement, curRect.x, curRect.y,
				curRect.width, curRect.height);

		// Fully re-activate the part since its location has changed
		reactivatePart(dragElement);

		return true;
	}

	@Override
	public Rectangle getRectangle(MUIElement dragElement, DnDInfo info) {
		if (dragElement.getCurSharedRef() != null)
			dragElement = dragElement.getCurSharedRef();

		if (dragElement instanceof MPartStack) {
			Control ctrl = (Control) dragElement.getWidget();
			curRect = ctrl.getBounds();
		} else {
			// Adjust the rectangle to account for the stack inside the new window
			MUIElement parentME = dragElement.getParent();
			Control ctrl = (Control) parentME.getWidget();
			curRect = ctrl.getBounds();

			// Try to take the window's trim into account
			curRect.width += 10;
			curRect.height += 22;
		}

		Point cp = Display.getCurrent().getCursorLocation();
		curRect.x = cp.x - 15;
		curRect.y = cp.y - 15;

		return curRect;
	}

	@Override
	public boolean track(MUIElement dragElement, DnDInfo info) {
		if (info.curElement != null)
			return false;

		manager.frameRect(getRectangle(dragElement, info));
		return true;
	}

	@Override
	public void dragEnter(MUIElement dragElement, DnDInfo info) {
		super.dragEnter(dragElement, info);
		dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
	}

	@Override
	public void dragLeave(MUIElement dragElement, DnDInfo info) {
		manager.clearOverlay();
		dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_NO));

		super.dragLeave(dragElement, info);
	}
}
