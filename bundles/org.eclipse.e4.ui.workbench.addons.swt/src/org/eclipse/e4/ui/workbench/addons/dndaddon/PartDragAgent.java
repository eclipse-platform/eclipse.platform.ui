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
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;

/**
 *
 */
public class PartDragAgent extends DragAgent {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.addons.dndaddon.DragAgent#getElementToDrag(org.eclipse.e4.ui.
	 * workbench.addons.dndaddon.CursorInfo)
	 */
	@Override
	public MUIElement getElementToDrag(CursorInfo info) {
		if (info.curElement instanceof MPartStack && info.itemElement instanceof MPart) {
			dragElement = info.itemElement;
			return info.itemElement;
		}
		return null;
	}

}
