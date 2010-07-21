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

import java.util.List;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 *
 */
public class StackDropAgent extends DropAgent {
	private MWindow window;

	/**
	 * @param window
	 */
	public StackDropAgent(MWindow window) {
		this.window = window;
	}

	@Override
	public boolean canDrop(MUIElement dragElement, CursorInfo info) {
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
			dragElement.getParent().getChildren().remove(dragElement);
		}

		if (info.itemIndex == -1) {
			dropStack.getChildren().add((MStackElement) dragElement);
		} else {
			dropStack.getChildren().add(info.itemIndex, (MStackElement) dragElement);
		}
		dropStack.setSelectedElement((MStackElement) dragElement);

		if (dragElement instanceof MPlaceholder) {
			if (isContainedInSharedPart(dragElement)) {
				MPlaceholder placeholder = (MPlaceholder) dragElement;
				EModelService modelService = window.getContext().get(EModelService.class);
				MPerspective currentPerspective = modelService.getActivePerspective(modelService
						.getTopLevelWindowFor(placeholder));
				List<MPerspective> perspectives = (List) currentPerspective.getParent()
						.getChildren();
				for (MUIElement perspective : perspectives) {
					if (perspective == currentPerspective) {
						continue;
					}
					List<MPlaceholder> phList = modelService.findElements(perspective, null,
							MPlaceholder.class, null);
					for (MPlaceholder ph : phList) {
						if (ph != placeholder && ph.getRef() == placeholder.getRef()) {
							ph.getParent().getChildren().remove(ph);
						}
					}
				}
			}
		}

		return true;
	}

	private boolean isContainedInSharedPart(MUIElement element) {
		MUIElement parent = element.getParent();
		while (parent != null) {
			if (parent.getCurSharedRef() != null) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
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
