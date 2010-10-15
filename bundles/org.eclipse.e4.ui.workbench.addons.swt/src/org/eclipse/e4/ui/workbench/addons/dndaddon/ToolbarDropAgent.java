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
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class ToolbarDropAgent extends DropAgent {
	private Shell dragShell;

	public ToolbarDropAgent() {
	}

	@Override
	public boolean canDrop(MUIElement dragElement, CursorInfo info) {
		if (!(dragElement instanceof MToolBar) || info.curElement == null)
			return false;

		// We can only drop TB's onto a trim area
		if (info.curElement instanceof MTrimBar)
			return true;

		MUIElement parentElement = info.curElement.getParent();
		if (parentElement instanceof MTrimBar)
			return true;

		return false;
	}

	@Override
	public boolean drop(MUIElement dragElement, CursorInfo info) {
		MTrimBar theBar = (MTrimBar) ((info.curElement instanceof MTrimBar) ? info.curElement
				: info.curElement.getParent());

		// If we're over another trim element should we go before or after it ?
		int index = -1;
		if (info.curElement != theBar) {
			index = info.curElement.getParent().getChildren().indexOf(info.curElement);
			if (after(info))
				index++;
		}

		dragElement.getParent().getChildren().remove(dragElement);
		dragElement.setVisible(true);
		if (index == -1 || index > theBar.getChildren().size() - 1)
			theBar.getChildren().add((MTrimElement) dragElement);
		else
			theBar.getChildren().add(index, (MTrimElement) dragElement);

		if (dragShell != null)
			dragShell.dispose();

		return true;
	}

	private boolean after(CursorInfo info) {
		if (info.curElement instanceof MTrimBar)
			return false;

		MTrimBar theBar = (MTrimBar) ((MUIElement) info.curElement.getParent());
		boolean isHorizontal = theBar.getSide() == SideValue.TOP
				|| theBar.getSide() == SideValue.BOTTOM;

		MUIElement overElement = info.curElement;
		Control ctrl = (Control) overElement.getWidget();
		Rectangle bounds = ctrl.getBounds();
		bounds = ctrl.getDisplay().map(ctrl.getParent(), null, bounds);

		if (isHorizontal
				&& info.cursorPos.x - bounds.x >= (bounds.x + bounds.width) - info.cursorPos.x)
			return true;
		else if (!isHorizontal
				&& info.cursorPos.y - bounds.y >= (bounds.y + bounds.height) - info.cursorPos.y)
			return true;

		return false;
	}

	@Override
	public Rectangle getRectangle(MUIElement dragElement, CursorInfo info) {
		boolean needsOpen = false;
		if (dragShell == null) {
			dragShell = new Shell(SWT.NO_TRIM);
			dragShell.setAlpha(150);
			dragShell.setLayout(new FillLayout());

			Composite comp = new Composite(dragShell, SWT.BORDER);
			comp.setLayout(new FillLayout());

			dragShell.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					dragShell = null;
				}
			});
			Control ctrl = (Control) dragElement.getWidget();
			ctrl.setParent(comp);
			dragShell.pack();
			needsOpen = true;
		}

		Point newLoc = new Point(info.cursorPos.x + 4, info.cursorPos.y + 4);
		dragShell.setLocation(newLoc);

		if (needsOpen)
			dragShell.open();

		MUIElement overElement = info.curElement;
		Control ctrl = (Control) overElement.getWidget();
		Rectangle bounds = ctrl.getBounds();
		bounds = ctrl.getDisplay().map(ctrl.getParent(), null, bounds);
		bounds.x = after(info) ? bounds.x + bounds.width : bounds.x;
		bounds.width = 5;
		bounds.x -= bounds.width / 2;

		return bounds;
	}

	@Override
	public Cursor getCursor(Display display, MUIElement dragElement, CursorInfo info) {
		return display.getSystemCursor(SWT.CURSOR_HAND);
	}
}
