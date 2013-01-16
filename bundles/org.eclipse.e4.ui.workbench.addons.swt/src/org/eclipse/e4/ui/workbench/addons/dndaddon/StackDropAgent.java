/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class StackDropAgent extends DropAgent {
	private Rectangle tabArea;
	private MPartStack dropStack;
	private CTabFolder dropCTF;

	private ArrayList<Rectangle> itemRects;
	private int curDropIndex = -2;

	/**
	 * @param manager
	 */
	public StackDropAgent(DnDManager manager) {
		super(manager);
	}

	@Override
	public boolean canDrop(MUIElement dragElement, DnDInfo info) {
		// We only except stack elements and whole stacks
		if (!(dragElement instanceof MStackElement) && !(dragElement instanceof MPartStack))
			return false;

		// We have to be over a stack ourselves
		if (!(info.curElement instanceof MPartStack))
			return false;

		MPartStack stack = (MPartStack) info.curElement;

		// We only work for CTabFolders
		if (!(stack.getWidget() instanceof CTabFolder))
			return false;

		// We can't drop stacks onto itself
		if (stack == dragElement)
			return false;

		// only allow dropping into the the area
		Rectangle areaRect = getTabAreaRect((CTabFolder) stack.getWidget());
		boolean inArea = areaRect.contains(info.cursorPos);
		if (inArea) {
			tabArea = areaRect;
			dropStack = (MPartStack) info.curElement;
			dropCTF = (CTabFolder) dropStack.getWidget();
			createInsertRects();
		}
		return inArea;
	}

	private Rectangle getTabAreaRect(CTabFolder theCTF) {
		Rectangle ctfBounds = theCTF.getBounds();
		ctfBounds.height = theCTF.getTabHeight();

		Rectangle displayBounds = Display.getCurrent().map(theCTF.getParent(), null, ctfBounds);
		return displayBounds;
	}

	private void createInsertRects() {
		itemRects = new ArrayList<Rectangle>();
		if (dropCTF.getItems().length > 0) {
			CTabItem[] items = dropCTF.getItems();

			// First rect is from left to the center of the item
			Rectangle itemRect = items[0].getBounds();
			int centerX = itemRect.x + (itemRect.width / 2);
			itemRect.width /= 2;
			int curX = itemRect.x + itemRect.width;
			Rectangle insertRect = dropCTF.getDisplay().map(dropCTF, null, itemRect);
			itemRects.add(insertRect);

			// Process the other items
			for (int i = 1; i < items.length; i++) {
				itemRect = items[i].getBounds();
				centerX = itemRect.x + (itemRect.width / 2);
				itemRect.width = centerX - curX;
				itemRect.x = curX;
				curX = centerX;
				insertRect = dropCTF.getDisplay().map(dropCTF, null, itemRect);
				itemRects.add(insertRect);
			}

			// Finally, add a rectangle from the center of the last element to the end
			itemRect.x = curX;
			itemRect.width = dropCTF.getBounds().width - curX;
			insertRect = dropCTF.getDisplay().map(dropCTF, null, itemRect);
			itemRects.add(insertRect);
		} else {
			// Empty stack, whole area is index == 0
			itemRects.add(tabArea);
		}
	}

	private int getDropIndex(DnDInfo info) {
		if (itemRects == null)
			return -1;

		for (Rectangle itemRect : itemRects) {
			if (itemRect.contains(info.cursorPos))
				return itemRects.indexOf(itemRect);
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragLeave()
	 */
	@Override
	public void dragLeave(MUIElement dragElement, DnDInfo info) {
		dndManager.clearOverlay();

		if (dndManager.getFeedbackStyle() == DnDManager.HOSTED) {
			if (dragElement.getParent() != null)
				dndManager.hostElement(dragElement, 16, 10);
		} else {
			dndManager.setHostBounds(null);
		}

		tabArea = null;
		curDropIndex = -2;

		super.dragLeave(dragElement, info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragLeave()
	 */
	@Override
	public boolean track(MUIElement dragElement, DnDInfo info) {
		if (!tabArea.contains(info.cursorPos) || dropStack == null || !dropStack.isToBeRendered())
			return false;

		int dropIndex = getDropIndex(info);
		if (curDropIndex == dropIndex || dropIndex == -1)
			return true;
		curDropIndex = dropIndex;

		dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));

		if (dropStack.getChildren().indexOf(dragElement) == dropIndex)
			return true;

		if (dndManager.getFeedbackStyle() == DnDManager.HOSTED) {
			dock(dragElement, dropIndex);
			Display.getCurrent().update();
			showFrame(dragElement);
		} else {
			if (dropIndex < dropCTF.getItemCount()) {
				Rectangle itemBounds = dropCTF.getItem(dropIndex).getBounds();
				itemBounds.width = 2;
				itemBounds = Display.getCurrent().map(dropCTF, null, itemBounds);
				dndManager.frameRect(itemBounds);
			} else if (dropCTF.getItemCount() > 0) {
				Rectangle itemBounds = dropCTF.getItem(dropIndex - 1).getBounds();
				itemBounds.x = itemBounds.x + itemBounds.width;
				itemBounds.width = 2;
				itemBounds = Display.getCurrent().map(dropCTF, null, itemBounds);
				dndManager.frameRect(itemBounds);
			} else {
				Rectangle fr = new Rectangle(tabArea.x, tabArea.y, tabArea.width, tabArea.height);
				fr.width = 2;
				dndManager.frameRect(fr);
			}

			if (dndManager.getFeedbackStyle() == DnDManager.GHOSTED) {
				Rectangle ca = dropCTF.getClientArea();
				ca = Display.getCurrent().map(dropCTF, null, ca);
				dndManager.setHostBounds(ca);
			}
		}

		return true;
	}

	/**
	 * @param dragElement
	 * @param dropIndex
	 */
	private void dock(MUIElement dragElement, int dropIndex) {
		// Adjust the index if necessary
		int elementIndex = dropStack.getChildren().indexOf(dragElement);
		if (elementIndex != -1 && !(dragElement instanceof MPartStack)) {
			// Get the index of this CTF entry
			Control dragCtrl = (Control) dragElement.getWidget();
			for (CTabItem cti : dropCTF.getItems()) {
				if (dragCtrl == cti.getControl()) {
					int itemIndex = dropCTF.indexOf(cti);
					if (dropIndex > 0 && itemIndex < dropIndex)
						dropIndex--;
				}
			}
		}

		// 'dropIndex' is now the index of the CTabItem to put ourselves before
		// we need to adjust this to be a model index
		int ctfItemCount = dropCTF.getItemCount();
		if (dropIndex < ctfItemCount) {
			CTabItem item = dropCTF.getItem(dropIndex);
			MUIElement itemModel = (MUIElement) item.getData(AbstractPartRenderer.OWNING_ME);

			// if we're going before ourselves its a NO-OP
			if (itemModel == dragElement)
				return;

			dropIndex = itemModel.getParent().getChildren().indexOf(itemModel);
			// if the item is dropped at the last position, there is
			// no existing item to put ourselves before
			// so we'll just go to the end.
		} else if (dropIndex == ctfItemCount) {
			dropIndex = dropStack.getChildren().size();
		}

		if (dragElement instanceof MStackElement) {
			if (dragElement.getParent() != null)
				dragElement.getParent().getChildren().remove(dragElement);

			if (dropIndex >= 0 && dropIndex < dropStack.getChildren().size())
				dropStack.getChildren().add(dropIndex, (MStackElement) dragElement);
			else
				dropStack.getChildren().add((MStackElement) dragElement);

			// (Re)active the element being dropped
			dropStack.setSelectedElement((MStackElement) dragElement);
		} else {
			MPartStack stack = (MPartStack) dragElement;
			MStackElement curSel = stack.getSelectedElement();
			List<MStackElement> kids = stack.getChildren();
			while (kids.size() > 0) {
				MStackElement lastChild = kids.remove(kids.size() - 1);
				if (dropIndex >= 0 && dropIndex < dropStack.getChildren().size())
					dropStack.getChildren().add(dropIndex, lastChild);
				else
					dropStack.getChildren().add(lastChild);
			}

			// (Re)active the element being dropped
			dropStack.setSelectedElement(curSel);
		}
	}

	/**
	 * @param dragElement
	 */
	private void showFrame(MUIElement dragElement) {
		CTabFolder ctf = (CTabFolder) dropStack.getWidget();
		CTabItem[] items = ctf.getItems();
		CTabItem item = null;
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData(AbstractPartRenderer.OWNING_ME) == dragElement) {
				item = items[i];
				break;
			}
		}

		Rectangle bounds = item.getBounds();
		bounds = Display.getCurrent().map(dropCTF, null, bounds);
		Rectangle outerBounds = new Rectangle(bounds.x - 3, bounds.y - 3, bounds.width + 6,
				bounds.height + 6);

		dndManager.frameRect(outerBounds);
	}

	@Override
	public boolean drop(MUIElement dragElement, DnDInfo info) {
		if (dndManager.getFeedbackStyle() != DnDManager.HOSTED) {
			int dropIndex = getDropIndex(info);
			if (dropIndex != -1)
				dock(dragElement, dropIndex);
		}
		return true;
	}
}
