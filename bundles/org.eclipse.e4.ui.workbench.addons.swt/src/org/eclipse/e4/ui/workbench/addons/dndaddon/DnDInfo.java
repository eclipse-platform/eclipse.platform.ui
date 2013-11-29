/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

class DnDInfo {
	Point cursorPos;
	Control curCtrl;
	Item curItem;

	MUIElement curElement;
	MUIElement itemElement;
	int itemIndex;
	Rectangle itemRect;

	Shell dragHost = null;
	int offsetX = 0;
	int offsetY = 0;
	Rectangle dragHostBounds = null;
	Point initialHostSize;

	MWindow topLevelWindow;
	Display display;

	EModelService modelService;

	public DnDInfo(MWindow topWin) {
		topLevelWindow = topWin;
		display = ((Control) (topWin.getWidget())).getDisplay();

		modelService = topWin.getContext().get(EModelService.class);
		update();
	}

	public void setDragHost(Shell hostingShell, int xOffset, int yOffset) {
		dragHost = hostingShell;
		offsetX = xOffset;
		offsetY = yOffset;
		dragHostBounds = null;

		if (dragHost == null)
			return;

		// Punch a 'hole' where the cursor is using a region
		Region rgn = dragHost.getRegion();
		// if (rgn != null && !rgn.isDisposed())
		// rgn.dispose();
		// rgn = new Region(display);
		Rectangle bounds = dragHost.getBounds();
		rgn.add(0, 0, bounds.width, bounds.height);
		rgn.subtract(offsetX, offsetY, 1, 1);
		dragHost.setRegion(rgn);
		initialHostSize = dragHost.getSize();

		// Do an initial 'track'
		Point curLoc = dragHost.getDisplay().getCursorLocation();
		dragHost.setLocation(curLoc.x - offsetX, curLoc.y - offsetY);

		dragHost.layout(true);
	}

	public void setDragHostBounds(Rectangle displayRect) {
		if (dragHost == null)
			return;

		dragHostBounds = displayRect;

		// Re-attach the drag host to the cursor
		if (dragHostBounds == null) {
			dragHost.setSize(initialHostSize);
			setDragHost(dragHost, offsetX, offsetY);
			return;
		}

		// dragHost.setVisible(false);
		dragHost.setAlpha(200);
		dragHost.setBounds(dragHostBounds);

		// punch a 'hole' where the cursor *is*
		Point cursorLoc = display.getCursorLocation();
		cursorLoc = display.map(null, dragHost, cursorLoc);
		Region rgn = dragHost.getRegion();
		Rectangle bounds = dragHost.getBounds();
		rgn.add(0, 0, bounds.width, bounds.height);
		rgn.subtract(cursorLoc.x, cursorLoc.y, 1, 1);
		display.update();
	}

	private void reset() {
		cursorPos = null;
		curCtrl = null;
		curItem = null;
		curElement = null;
		itemElement = null;
		itemIndex = -1;
		itemRect = null;
	}

	private void setItemInfo() {
		if (curElement == null)
			return;

		Control ctrl = (Control) curElement.getWidget();

		// KLUDGE!! Should delegate to curElement's renderer
		if (ctrl instanceof CTabFolder) {
			CTabFolder ctf = (CTabFolder) ctrl;
			Point localPos = display.map(null, ctf, cursorPos);
			curItem = ctf.getItem(localPos);
			if (curItem != null) {
				itemElement = (MUIElement) curItem.getData(AbstractPartRenderer.OWNING_ME);
				if (itemElement != null) {
					itemIndex = ctf.indexOf((CTabItem) curItem);
					itemRect = display.map(ctf, ctf.getShell(), ((CTabItem) curItem).getBounds());
				}
			}
		} else if (ctrl instanceof ToolBar) {
			ToolBar tb = (ToolBar) ctrl;
			Point localPos = display.map(null, tb, cursorPos);
			ToolItem curItem = tb.getItem(localPos);
			if (curItem != null) {
				itemElement = (MUIElement) curItem.getData(AbstractPartRenderer.OWNING_ME);
				if (itemElement != null) {
					itemIndex = tb.indexOf(curItem);
					itemRect = display.map(tb, tb.getShell(), curItem.getBounds());
				}
			}
		}
	}

	private MUIElement getModelElement(Control ctrl) {
		if (ctrl == null)
			return null;

		MUIElement element = (MUIElement) ctrl.getData(AbstractPartRenderer.OWNING_ME);
		if (element != null) {
			if (modelService.getTopLevelWindowFor(element) == topLevelWindow)
				return element;
			return null;
		}

		return getModelElement(ctrl.getParent());
	}

	public void update() {
		final Display display = Display.getCurrent();
		if (display == null)
			return;

		reset();

		cursorPos = display.getCursorLocation();

		if (dragHost != null && !dragHost.isDisposed() && dragHost.getVisible()) {
			if (dragHostBounds == null) {
				// First move the dragHost so that its 'hole' is where the mouse is
				dragHost.setLocation(cursorPos.x - offsetX, cursorPos.y - offsetY);
			} else {
				// Move the 'hole' to where the cursor is
				Point cursorLoc = display.getCursorLocation();
				cursorLoc = display.map(null, dragHost, cursorLoc);
				Region rgn = dragHost.getRegion();
				Rectangle bounds = dragHost.getBounds();
				rgn.add(0, 0, bounds.width, bounds.height);
				rgn.subtract(cursorLoc.x, cursorLoc.y, 1, 1);
			}
		}

		curCtrl = display.getCursorControl();
		if (curCtrl == null)
			return;

		curElement = getModelElement(curCtrl);
		setItemInfo();
	}

	public void update(DragDetectEvent e) {
		reset();
		if (!(e.widget instanceof Control))
			return;
		curCtrl = (Control) e.widget;
		cursorPos = new Point(e.x, e.y);
		cursorPos = curCtrl.getDisplay().map(curCtrl, null, cursorPos);

		curElement = getModelElement(curCtrl);
		setItemInfo();
	}
}
