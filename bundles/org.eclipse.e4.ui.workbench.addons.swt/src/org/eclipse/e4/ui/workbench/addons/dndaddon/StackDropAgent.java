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

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
public class StackDropAgent extends DropAgent {
	// private MWindow window;

	/**
	 * @param window
	 */
	public StackDropAgent(MWindow window) {
		// this.window = window;
	}

	@Override
	public boolean canDrop(MUIElement dragElement, CursorInfo info) {
		if (!(dragElement instanceof MPart))
			return false;

		if (info.curElement == dragElement.getParent()) {
			if (info.curElement != null && info.curElement == dragElement.getParent()) {
				CTabFolder ctf = (CTabFolder) dragElement.getParent().getWidget();
				return ctf.getItemCount() > 1;
			}
		}

		if (dragElement instanceof MPart && info.curElement instanceof MPartStack) {
			MPartStack stack = (MPartStack) info.curElement;
			boolean isView = !dragElement.getTags().contains("Editor"); //$NON-NLS-1$
			boolean isEditorStack = stack.getTags().contains("EditorStack"); //$NON-NLS-1$

			// special case...don't allow dropping views into an *enpty* Editor Stack
			if (isView && isEditorStack && stack.getChildren().size() == 0) {
				CTabFolder ctf = (CTabFolder) stack.getWidget();
				Point stackPoint = ctf.getDisplay().map(null, ctf, info.cursorPos);

				// If we're in the 'tab area' then allow the drop, else assume another
				// agent, such as split, will handle it.
				boolean canDrop = stackPoint.y <= ctf.getTabHeight();
				return canDrop;
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean drop(MUIElement dragElement, CursorInfo info) {
		MPartStack dropStack = (MPartStack) info.curElement;

		if (dragElement.getCurSharedRef() != null)
			dragElement = dragElement.getCurSharedRef();

		if (dragElement.getParent() == info.curElement) {
			CTabFolder ctf = (CTabFolder) dropStack.getWidget();
			for (CTabItem cti : ctf.getItems()) {
				if (cti.getData(AbstractPartRenderer.OWNING_ME) == dragElement) {
					if (info.itemIndex >= 0 && ctf.indexOf(cti) < info.itemIndex)
						info.itemIndex--;
				}
			}
		}

		if (dragElement.getParent() != null) {
			MElementContainer<MUIElement> dragParent = dragElement.getParent();

			// If this was the last child in the stack it will go away so
			// grab back its 'weight' if it's in the same sash container
			int curCount = dragParent.getChildren().size();
			if ((Object) dragParent instanceof MPartStack && curCount == 1
					&& dragParent.getParent() == dropStack.getParent()) {
				int dpWeight = -1;
				try {
					dpWeight = Integer.parseInt(dragParent.getContainerData());
				} catch (NumberFormatException e) {
				}
				if (dpWeight != -1) {
					int dropWeight = 0;
					try {
						dropWeight = Integer.parseInt(dropStack.getContainerData());
					} catch (NumberFormatException e) {
					}
					dropWeight += dpWeight;
					dropStack.setContainerData(Integer.toString(dropWeight));
				}
			}

			dragParent.getChildren().remove(dragElement);
		}

		if (info.itemIndex == -1) {
			dropStack.getChildren().add((MStackElement) dragElement);
		} else {
			dropStack.getChildren().add(info.itemIndex, (MStackElement) dragElement);
		}
		dropStack.setSelectedElement((MStackElement) dragElement);

		if (dragElement.getWidget() instanceof Control) {
			Control ctrl = (Control) dragElement.getWidget();
			ctrl.getShell().layout();
		}

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
		CTabFolder ctf = (CTabFolder) info.curElement.getWidget();
		if (info.itemElement != null) {
			if (info.curElement.getWidget() instanceof CTabFolder) {
				for (CTabItem cti : ctf.getItems()) {
					if (cti.getData(AbstractPartRenderer.OWNING_ME) == info.itemElement
							|| cti.getData(AbstractPartRenderer.OWNING_ME) == info.itemElementRef) {
						Rectangle itemRect = cti.getBounds();
						itemRect.width = 3;
						return cti.getDisplay().map(cti.getParent(), null, itemRect);
					}
				}
			}
		} else {
			if (ctf.getItemCount() == 0) {
				Rectangle ctfBounds = ctf.getBounds();
				ctfBounds.height = ctf.getTabHeight();
				ctfBounds.width = 3;
				return ctf.getDisplay().map(ctf, null, ctfBounds);
			}

			CTabItem cti = ctf.getItem(ctf.getItemCount() - 1);
			Rectangle itemRect = cti.getBounds();
			itemRect.x = (itemRect.x + itemRect.width) - 3;
			itemRect.width = 3;
			return cti.getDisplay().map(cti.getParent(), null, itemRect);
		}
		return null;
	}

}
