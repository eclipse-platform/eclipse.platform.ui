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

import org.eclipse.e4.ui.widgets.CTabFolder;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class SplitDropAgent extends DropAgent {
	EModelService modelService;

	/**
	 * 
	 */
	public SplitDropAgent(EModelService modelService) {
		this.modelService = modelService;
	}

	@Override
	public boolean canDrop(MUIElement dragElement, CursorInfo info) {
		if (dragElement.getCurSharedRef() != null)
			dragElement = dragElement.getCurSharedRef();
		if (info.curElement == dragElement) {
			if (dragElement.getParent().getWidget() instanceof CTabFolder) {
				CTabFolder ctf = (CTabFolder) dragElement.getParent().getWidget();
				return ctf.getItemCount() > 1;
			}
		}

		if (dragElement instanceof MStackElement && info.curElement instanceof MStackElement)
			return true;

		return false;
	}

	private int whereToDrop(Control ctrl, Point cursorPos) {
		Rectangle bb = ctrl.getBounds();
		Rectangle displayBB = ctrl.getDisplay().map(ctrl.getParent(), null, bb);
		int dxl = cursorPos.x - displayBB.x;
		int dxr = (displayBB.x + displayBB.width) - cursorPos.x;
		int dx = Math.min(dxl, dxr);
		int dyl = cursorPos.y - displayBB.y;
		int dyr = (displayBB.y + displayBB.height) - cursorPos.y;
		int dy = Math.min(dyl, dyr);
		int where;
		if (dx < dy) {
			if (dxl < dxr)
				where = EModelService.LEFT_OF;
			else
				where = EModelService.RIGHT_OF;
		} else {
			if (dyl < dyr)
				where = EModelService.ABOVE;
			else
				where = EModelService.BELOW;
		}

		return where;
	}

	@Override
	public boolean drop(MUIElement dragElement, CursorInfo info) {
		if (dragElement.getCurSharedRef() != null)
			dragElement = dragElement.getCurSharedRef();

		MUIElement relTo = info.curElement;
		Control ctrl = (Control) relTo.getWidget();
		int where = whereToDrop(ctrl, info.cursorPos);

		if (relTo.getCurSharedRef() != null)
			relTo = relTo.getCurSharedRef();

		MUIElement relParent = relTo.getParent();
		if (relParent instanceof MPartStack)
			relTo = relParent;

		if (dragElement.getParent() != null) {
			dragElement.getParent().getChildren().remove(dragElement);
		}

		// If we're dropping a part wrap it in a stack
		MUIElement toInsert = dragElement;
		if (dragElement instanceof MStackElement) {
			MPartStack newPS = BasicFactoryImpl.eINSTANCE.createPartStack();
			newPS.getChildren().add((MStackElement) dragElement);
			newPS.setSelectedElement((MStackElement) dragElement);
			toInsert = newPS;
		}

		modelService.insert((MPartSashContainerElement) toInsert,
				(MPartSashContainerElement) relTo, where, 50);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.ui.renderers.swt.dnd.DropAgent#getCursor
	 * (org.eclipse.swt.widgets.Display, org.eclipse.e4.ui.model.application.ui.MUIElement,
	 * org.eclipse.e4.workbench.ui.renderers.swt.dnd.CursorInfo)
	 */
	@Override
	public Cursor getCursor(Display display, MUIElement dragElement, CursorInfo info) {
		MPart dropPart = (MPart) info.curElement;
		Control ctrl = (Control) dropPart.getWidget();
		int where = whereToDrop(ctrl, info.cursorPos);
		if (where == EModelService.ABOVE)
			return display.getSystemCursor(SWT.CURSOR_SIZEN);
		if (where == EModelService.BELOW)
			return display.getSystemCursor(SWT.CURSOR_SIZES);
		if (where == EModelService.LEFT_OF)
			return display.getSystemCursor(SWT.CURSOR_SIZEW);
		if (where == EModelService.RIGHT_OF)
			return display.getSystemCursor(SWT.CURSOR_SIZEE);

		return display.getSystemCursor(SWT.CURSOR_HELP);
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
		MPart dropPart = (MPart) info.curElement;
		Control ctrl = (Control) dropPart.getWidget();

		if (ctrl.getParent() instanceof CTabFolder)
			ctrl = ctrl.getParent();
		Rectangle bounds = ctrl.getBounds();
		int where = whereToDrop(ctrl, info.cursorPos);
		if (where == EModelService.ABOVE)
			bounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height / 2);
		if (where == EModelService.BELOW)
			bounds = new Rectangle(bounds.x, bounds.y + (bounds.height / 2), bounds.width,
					bounds.height / 2);
		if (where == EModelService.LEFT_OF)
			bounds = new Rectangle(bounds.x, bounds.y, bounds.width / 2, bounds.height);
		if (where == EModelService.RIGHT_OF)
			bounds = new Rectangle(bounds.x + (bounds.width / 2), bounds.y, bounds.width / 2,
					bounds.height);

		return ctrl.getDisplay().map(ctrl.getParent(), null, bounds);
	}
}
