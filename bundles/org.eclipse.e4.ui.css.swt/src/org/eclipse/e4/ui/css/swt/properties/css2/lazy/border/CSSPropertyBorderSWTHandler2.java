/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2.lazy.border;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CSSPropertyBorderSWTHandler2 implements ICSSPropertyHandler2 {

	public static final ICSSPropertyHandler2 INSTANCE = new CSSPropertyBorderSWTHandler2();

	public void onAllCSSPropertiesApplyed(Object element, CSSEngine engine)
			throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			Composite parent = control.getParent();
			if (parent != null)
				parent.redraw();
		}
	}

}
