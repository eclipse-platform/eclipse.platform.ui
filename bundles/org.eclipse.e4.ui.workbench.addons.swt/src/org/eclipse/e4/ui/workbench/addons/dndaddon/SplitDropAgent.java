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
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class SplitDropAgent extends DropAgent {
	private static final int NOWHERE = -1;

	private MPartStack dropStack;
	private CTabFolder dropCTF;
	private Rectangle clientBounds;
	private String weight;
	private MPartStack toInsert;
	private int curDockLocation = NOWHERE;

	private Rectangle ctfBounds;

	/**
	 * @param modelService
	 *            The model service related to this agent
	 * 
	 */
	public SplitDropAgent(DnDManager manager) {
		super(manager);
	}

	@Override
	public boolean canDrop(MUIElement dragElement, DnDInfo info) {
		if (!(dragElement instanceof MStackElement))
			return false;

		if (!(info.curElement instanceof MStackElement))
			return false;

		// Detect placeholders
		MUIElement parent = info.curElement.getParent();
		if (info.curElement instanceof MPart && info.curElement.getCurSharedRef() != null)
			parent = info.curElement.getCurSharedRef().getParent();

		if (!(parent instanceof MPartStack) || !(parent.getWidget() instanceof CTabFolder))
			return false;

		dropStack = (MPartStack) parent;
		weight = dropStack.getContainerData();
		dropCTF = (CTabFolder) parent.getWidget();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragEnter(org.eclipse.e4.ui.model.
	 * application.ui.MUIElement, org.eclipse.e4.ui.workbench.addons.dndaddon.DnDInfo)
	 */
	@Override
	public void dragEnter(MUIElement dragElement, DnDInfo info) {
		super.dragEnter(dragElement, info);

		clientBounds = dropCTF.getClientArea();
		clientBounds = Display.getCurrent().map(dropCTF, null, clientBounds);
		ctfBounds = dropCTF.getBounds();
		ctfBounds = Display.getCurrent().map(dropCTF.getParent(), null, ctfBounds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragLeave(org.eclipse.e4.ui.model.
	 * application.ui.MUIElement, org.eclipse.e4.ui.workbench.addons.dndaddon.DnDInfo)
	 */
	@Override
	public void dragLeave(MUIElement dragElement, DnDInfo info) {
		if (dndManager.getFeedbackStyle() != DnDManager.SIMPLE)
			unDock(dragElement);
		dndManager.clearOverlay();

		curDockLocation = NOWHERE;

		super.dragLeave(dragElement, info);
	}

	@Override
	public boolean drop(MUIElement dragElement, DnDInfo info) {
		if (dndManager.getFeedbackStyle() != DnDManager.HOSTED && curDockLocation != NOWHERE) {
			dock(dragElement, curDockLocation);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#track(org.eclipse.e4.ui.model.application
	 * .ui.MUIElement, org.eclipse.e4.ui.workbench.addons.dndaddon.DnDInfo)
	 */
	@Override
	public boolean track(MUIElement dragElement, DnDInfo info) {
		if (!clientBounds.contains(info.cursorPos))
			return false;

		int dockLocation = getDockLocation(info);
		if (dockLocation == curDockLocation)
			return true;

		curDockLocation = dockLocation;

		if (curDockLocation != NOWHERE) {
			Rectangle dockBounds = getDockBounds(curDockLocation);
			if (dndManager.getFeedbackStyle() == DnDManager.HOSTED) {
				dock(dragElement, curDockLocation);
			} else if (dndManager.getFeedbackStyle() == DnDManager.GHOSTED) {
				dndManager.setHostBounds(dockBounds);
			}
			dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
			dndManager.frameRect(dockBounds);
		} else {
			unDock(dragElement);
			dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_NO));
		}

		return true;
	}

	/**
	 * @param curDockLocation2
	 * @return
	 */
	private Rectangle getDockBounds(int location) {
		Rectangle bounds = new Rectangle(ctfBounds.x, ctfBounds.y, ctfBounds.width,
				ctfBounds.height);

		if (location == EModelService.ABOVE) {
			bounds.height /= 2;
		} else if (location == EModelService.BELOW) {
			bounds.height /= 2;
			bounds.y += bounds.height;
		} else if (location == EModelService.LEFT_OF) {
			bounds.width /= 2;
		} else if (location == EModelService.RIGHT_OF) {
			bounds.width /= 2;
			bounds.x += bounds.width;
		}
		return bounds;
	}

	/**
	 * @param info
	 * @return
	 */
	private int getDockLocation(DnDInfo info) {
		int dx = info.cursorPos.x - clientBounds.x;
		int dy = info.cursorPos.y - clientBounds.y;
		int dxr = (clientBounds.x + clientBounds.width) - info.cursorPos.x;
		int dyr = (clientBounds.y + clientBounds.height) - info.cursorPos.y;
		int minDx = Math.min(dx, dxr);
		int minDy = Math.min(dy, dyr);

		if (minDx < minDy)
			return dx < dxr ? EModelService.LEFT_OF : EModelService.RIGHT_OF;

		return dy < dyr ? EModelService.ABOVE : EModelService.BELOW;
	}

	protected void unDock(MUIElement dragElement) {
		dndManager.clearOverlay();
		dndManager.setHostBounds(null);
		dndManager.setDragHostVisibility(true);
	}

	protected boolean dock(MUIElement dragElement, int where) {
		dndManager.setDragHostVisibility(false);
		MUIElement relTo = dropStack;

		// wrap it in a stack
		MStackElement stackElement = (MStackElement) dragElement;
		toInsert = BasicFactoryImpl.eINSTANCE.createPartStack();
		toInsert.getChildren().add(stackElement);
		toInsert.setSelectedElement(stackElement);

		MUIElement relToParent = relTo.getParent();

		dndManager.getModelService().insert((MPartSashContainerElement) toInsert,
				(MPartSashContainerElement) relTo, where, 50);

		// Force the new sash to have the same weight as the original
		if (relTo.getParent() != relToParent)
			relTo.getParent().setContainerData(weight);
		dndManager.update();

		return true;
	}
}
