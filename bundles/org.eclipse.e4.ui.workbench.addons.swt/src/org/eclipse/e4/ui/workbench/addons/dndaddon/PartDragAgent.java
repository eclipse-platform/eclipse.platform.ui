/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import java.util.List;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;

public class PartDragAgent extends DragAgent {
	public PartDragAgent(DnDManager manager) {
		super(manager);
	}

	@Override
	public MUIElement getElementToDrag(DnDInfo info) {
		if (!(info.curElement instanceof MPartStack)) {
			return null;
		}

		MPartStack stack = (MPartStack) info.curElement;

		// Drag a part that is in a stack
		if (info.itemElement instanceof MStackElement) {
			// Prevent dragging 'No Move' parts
			List<String> tags;
			if (info.itemElement instanceof MPlaceholder) {
				tags = ((MPlaceholder) info.itemElement).getRef().getTags();
			} else {
				tags = info.itemElement.getTags();
			}
			if (tags.contains(IPresentationEngine.NO_MOVE)) {
				return null;
			}

			// If it's an MPart only drag the part itself
			if (info.itemElement instanceof MPart) {
				return info.itemElement;
			}

			// check if we want to drag the placeholder or default to dragging the whole stack
			int tbrCount = dndManager.getModelService().countRenderableChildren(stack);
			if (tbrCount > 1 || dndManager.getModelService().isLastEditorStack(stack)) {
				dragElement = info.itemElement;
				return info.itemElement;
			}
		}

		// Drag a complete stack
		// Only allow a drag to start if we're a CTabFolder
		if (!(stack.getWidget() instanceof CTabFolder)) {
			return null;
		}

		// Only allow a drag to start if we're inside the 'tab area' of the CTF
		CTabFolder ctf = (CTabFolder) stack.getWidget();
		Point ctfPos = ctf.getDisplay().map(null, ctf, info.cursorPos);
		if (ctfPos.y > ctf.getTabHeight()) {
			return null;
		}

		// Prevent dragging 'No Move' stacks
		if (stack.getTags().contains(IPresentationEngine.NO_MOVE)) {
			return null;
		}

		// Prevent dragging the last stack out of the shared area
		if (dndManager.getModelService().isLastEditorStack(stack)) {
			return null;
		}

		dragElement = info.curElement;
		return info.curElement;
	}

	@Override
	public void dragStart(DnDInfo info) {
		super.dragStart(info);
		if (dndManager.getFeedbackStyle() != DnDManager.SIMPLE) {
			dndManager.hostElement(dragElement, 16, 10);
		}
	}

	@Override
	public void dragFinished(boolean performDrop, DnDInfo info) {
		if (dragElement instanceof MPart) {
			EPartService ps = dndManager.getDragWindow().getContext().get(EPartService.class);
			ps.activate((MPart) dragElement);
		}
		super.dragFinished(performDrop, info);
	}
}
