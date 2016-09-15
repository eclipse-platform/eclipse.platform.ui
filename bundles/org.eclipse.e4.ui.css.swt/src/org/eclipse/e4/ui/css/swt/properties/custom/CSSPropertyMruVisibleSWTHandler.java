/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		IBM Corporation - initial API and implementation
 * 		Andrey Loskutov <loskutov@gmx.de> - Bug 388476
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyMruVisibleSWTHandler extends AbstractCSSPropertySWTHandler{

	private static boolean mruControlledByCSS = true;

	@Override
	public void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!isMRUControlledByCSS()) {
			return;
		}
		boolean isMruVisible = (Boolean)engine.convert(value, Boolean.class, null);
		if (control instanceof CTabFolder) {
			CTabFolder folder = (CTabFolder) control;
			folder.setMRUVisible(isMruVisible);
		}
	}

	@Override
	public String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if (control instanceof CTabFolder){
			CTabFolder folder = (CTabFolder)control;
			return Boolean.toString( folder.getMRUVisible() );
		}
		return null;
	}

	public static boolean isMRUControlledByCSS() {
		return mruControlledByCSS;
	}

	public static void setMRUControlledByCSS(boolean controlledByCSS) {
		mruControlledByCSS = controlledByCSS;
	}
}
