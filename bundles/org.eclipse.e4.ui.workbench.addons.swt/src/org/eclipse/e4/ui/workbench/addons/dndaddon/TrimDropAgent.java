/*******************************************************************************
 * Copyright (c) 2012 - 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel (Lars.Vogel@gmail.com) - Bug 420835
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import java.util.List;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimBarLayout;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class TrimDropAgent extends DropAgent {
	private SideValue side;
	private MTrimBar trimBar;

	/**
	 * @param manager
	 */
	public TrimDropAgent(DnDManager manager) {
		super(manager);
	}

	@Override
	public boolean canDrop(MUIElement dragElement, DnDInfo info) {
		// We only except elements that can go in the trim
		if (!(dragElement instanceof MTrimElement))
			return false;

		// are we over a 'side' ?
		side = getDropSide(info);

		return side != null;
	}

	private SideValue getDropSide(DnDInfo info) {
		Shell ds = dndManager.getDragShell();
		if (ds.getLayout() instanceof TrimmedPartLayout) {
			TrimmedPartLayout tpl = (TrimmedPartLayout) ds.getLayout();

			if (tpl.getTrimRect(SWT.TOP).contains(info.cursorPos))
				return SideValue.TOP;
			if (tpl.getTrimRect(SWT.BOTTOM).contains(info.cursorPos))
				return SideValue.BOTTOM;
			if (tpl.getTrimRect(SWT.LEFT).contains(info.cursorPos))
				return SideValue.LEFT;
			if (tpl.getTrimRect(SWT.RIGHT).contains(info.cursorPos))
				return SideValue.RIGHT;
		}

		return null;
	}

	private MUIElement getInsertionElement(MUIElement dragElement, DnDInfo info) {
		Composite trimComp = (Composite) trimBar.getWidget();

		// If we're over the trim bar itself drop at the end
		// May need to take margins into account
		if (info.curCtrl == trimComp)
			return null;

		TrimBarLayout tbl = (TrimBarLayout) trimComp.getLayout();
		Point trimPos = trimComp.getDisplay().map(null, trimComp, info.cursorPos);
		Control trimCtrl = tbl.ctrlFromPoint(trimComp, trimPos);

		if (trimCtrl == null)
			return null;

		if (trimCtrl == dragElement.getWidget())
			return dragElement;

		// Are we closer to the 'end' of the trim control ?
		// If so insert before the next control (if any)
		MUIElement trimElement = (MUIElement) trimCtrl.getData(AbstractPartRenderer.OWNING_ME);
		if (isAfter(trimCtrl, info)) {
			MElementContainer<MUIElement> trimParent = trimElement.getParent();
			int trimIndex = trimParent.getChildren().indexOf(trimElement);
			if (trimIndex == trimParent.getChildren().size() - 1)
				return null;

			return trimParent.getChildren().get(trimIndex + 1);
		}

		return trimElement;
	}

	private boolean isAfter(Control trimCtrl, DnDInfo info) {
		Rectangle bounds = trimCtrl.getBounds();
		bounds = trimCtrl.getDisplay().map(trimCtrl.getParent(), null, bounds);
		Point center = new Point(bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2));
		boolean horizontal = trimBar.getSide() == SideValue.TOP
				|| trimBar.getSide() == SideValue.BOTTOM;
		boolean after = horizontal ? info.cursorPos.x > center.x : info.cursorPos.y > center.y;

		return after;
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

		MTrimmedWindow window = (MTrimmedWindow) dndManager.getDragWindow();
		trimBar = dndManager.getModelService().getTrim(window, side);
		trimBar.setToBeRendered(true);

		dragElement.setVisible(true);
		track(dragElement, info);

		dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragLeave()
	 */
	@Override
	public void dragLeave(MUIElement dragElement, DnDInfo info) {
		trimBar = null;
		side = null;

		dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_NO));
		super.dragLeave(dragElement, info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragLeave()
	 */
	@Override
	public boolean track(MUIElement dragElement, DnDInfo info) {
		SideValue curSide = getDropSide(info);
		if (side != curSide)
			return false;

		MUIElement insertBefore = getInsertionElement(dragElement, info);

		// Check for no-ops
		MUIElement curParent = dragElement.getParent();
		List<MTrimElement> trimKids = trimBar.getChildren();

		boolean sameParent = curParent == trimBar;
		boolean alreadyThere = false;
		if (sameParent) {
			if (insertBefore == null) {
				alreadyThere = trimKids.get(trimKids.size() - 1) == dragElement;
			} else {
				alreadyThere = insertBefore == dragElement; // Reflexive
				if (!alreadyThere) {
					alreadyThere = trimKids.indexOf(dragElement) == (trimKids.indexOf(insertBefore) - 1);
				}
			}
		}

		if (!sameParent || !alreadyThere) {
			dock(dragElement, insertBefore);
		}

		return true;
	}

	private void dock(MUIElement dragElement, MUIElement insertBefore) {
		dragElement.setToBeRendered(false);
		dragElement.getParent().getChildren().remove(dragElement);

		dragElement.setVisible(true);

		if (insertBefore == null) {
			trimBar.getChildren().add((MTrimElement) dragElement);
		} else {
			int dropIndex = trimBar.getChildren().indexOf(insertBefore);
			trimBar.getChildren().add(dropIndex, (MTrimElement) dragElement);
		}

		dragElement.setToBeRendered(true);
		Control trimCtrl = (Control) dragElement.getWidget();
		trimCtrl.setBackground(trimCtrl.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
	}

	@Override
	public boolean drop(MUIElement dragElement, DnDInfo info) {
		Control trimCtrl = (Control) dragElement.getWidget();
		trimCtrl.setBackground(null);
		return true;
	}
}
