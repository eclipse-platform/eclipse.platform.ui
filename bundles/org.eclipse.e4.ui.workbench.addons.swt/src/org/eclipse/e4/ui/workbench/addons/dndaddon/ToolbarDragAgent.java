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
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.swt.widgets.ToolBar;

/**
 *
 */
public class ToolbarDragAgent extends DragAgent {

	@Override
	public MUIElement getElementToDrag(CursorInfo info) {
		if (info.curElement instanceof MToolBar) {
			if (info.curCtrl instanceof ToolBar) {
				if (info.curCtrl == info.curCtrl.getParent().getChildren()[0]) {
					dragElement = info.curElement;
					return info.curElement;
				}
			}
		}
		return null;
	}

	@Override
	public void dragStart(MUIElement element) {
		super.dragStart(element);

		element.setVisible(false);
	}
}
