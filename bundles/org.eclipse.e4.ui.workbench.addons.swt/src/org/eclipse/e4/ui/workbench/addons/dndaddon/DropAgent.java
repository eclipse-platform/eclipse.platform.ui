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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
abstract class DropAgent {
	protected DnDManager dndManager;

	/**
	 * @param manager
	 */
	public DropAgent(DnDManager manager) {
		dndManager = manager;
	}

	public void reactivatePart(MUIElement dragElement) {
		IEclipseContext context = dndManager.getModelService().getContainingContext(dragElement);
		if (context == null) {
			return;
		}

		EPartService ps = context.get(EPartService.class);
		if (ps == null) {
			return;
		}

		MPart partToActivate = null;
		if (dragElement instanceof MPart) {
			partToActivate = (MPart) dragElement;
		} else if (dragElement instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) dragElement;
			if (ph.getRef() instanceof MPart) {
				partToActivate = (MPart) ph.getRef();
			}
		} else if (dragElement instanceof MPartStack) {
			MPartStack stack = (MPartStack) dragElement;
			if (stack.getSelectedElement() instanceof MPart) {
				partToActivate = (MPart) stack.getSelectedElement();
			} else if (stack.getSelectedElement() instanceof MPlaceholder) {
				MPlaceholder ph = (MPlaceholder) stack.getSelectedElement();
				if (ph.getRef() instanceof MPart) {
					partToActivate = (MPart) ph.getRef();
				}
			}
		}

		if (partToActivate != null) {
			ps.activate(null);
			ps.activate(partToActivate);
		}
	}

	public abstract boolean canDrop(MUIElement dragElement, DnDInfo info);

	public abstract boolean drop(MUIElement dragElement, DnDInfo info);

	public abstract boolean track(MUIElement dragElement, DnDInfo info);

	public Cursor getCursor(Display display, MUIElement dragElement, DnDInfo info) {
		return display.getSystemCursor(SWT.CURSOR_HAND);
	}

	public Rectangle getRectangle(MUIElement dragElement, DnDInfo info) {
		return null;
	}

	public void dragEnter(MUIElement dragElement, DnDInfo info) {
	}

	public void dragLeave(MUIElement dragElement, DnDInfo info) {
	}

	/**
	 * This agent is being disposed
	 */
	public void dispose() {
	}
}
