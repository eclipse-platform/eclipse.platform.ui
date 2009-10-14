/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.e4.ui.model.application.MUIElement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Create a contribute part.
 */
public class ToolItemRenderer extends SWTPartRenderer {

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MToolItem) || !(parent instanceof ToolBar))
			return null;

		MToolItem itemModel = (MToolItem) element;

		int flags = SWT.PUSH;
		// if (itemModel.get() != null)
		// flags = SWT.DROP_DOWN;
		ToolItem newItem = new ToolItem((ToolBar) parent, flags);
		newItem.setText(itemModel.getName());

		return newItem;
	}

}
