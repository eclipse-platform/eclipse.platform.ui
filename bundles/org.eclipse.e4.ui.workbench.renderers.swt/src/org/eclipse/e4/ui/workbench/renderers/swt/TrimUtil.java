/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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

package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 *
 */
public class TrimUtil {
	public static Composite wrapTrim(Control trim) {
		int orientation = SWT.HORIZONTAL;
		if (trim instanceof ToolBar)
			orientation = (((ToolBar) trim).getStyle() & SWT.VERTICAL) == 0 ? SWT.HORIZONTAL
					: SWT.VERTICAL;

		Composite parentComp = trim.getParent();
		Composite wrapper = new Composite(parentComp, SWT.NONE);
		RowLayout layout = RowLayoutFactory.fillDefaults().wrap(false)
				.spacing(0).type(orientation).create();
		layout.marginLeft = 3;
		layout.center = true;
		wrapper.setLayout(layout);

		// Separator (aka 'drag handle')
		ToolBar separatorToolBar = new ToolBar(wrapper, orientation | SWT.WRAP
				| SWT.FLAT | SWT.RIGHT);
		new ToolItem(separatorToolBar, SWT.SEPARATOR);

		// Put the trim under the wrapper and ensure it's last
		trim.setParent(wrapper);
		trim.moveBelow(null);

		return wrapper;
	}

	public static Control getRealControl(Composite wrapper) {
		Control[] kids = wrapper.getChildren();
		if (kids.length == 2)
			return kids[1];

		return null;
	}
}
