/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

/**
 * Abstract CSS Property SWT Handler to check if the <code>element</code>
 * coming from applyCSSProperty and retrieveCSSProperty methods is SWT Control.
 */
public abstract class AbstractCSSPropertySWTHandler implements ICSSPropertyHandler {

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			// The SWT control is retrieved
			// the apply CSS property can be done.
			this.applyCSSProperty(control, property, value, pseudo, engine);
			return true;
		}
		return false;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property, String pseudo,
			CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			// The SWT control is retrieved
			// the retrieve CSS property can be done.
			return retrieveCSSProperty(control, property, pseudo, engine);
		}
		return null;
	}

	/**
	 * Apply CSS Property <code>property</code> (ex : background-color) with
	 * CSSValue <code>value</code> (ex : red) into the SWT
	 * <code>control</code> (ex : SWT Text, SWT Label).
	 *
	 * @param control
	 * @param property
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	protected abstract void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Retrieve CSS value (ex : red) of CSS Property <code>property</code> (ex :
	 * background-color) from the SWT <code>control</code> (ex : SWT Text, SWT
	 * Label).
	 *
	 * @param control
	 * @param property
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	protected abstract String retrieveCSSProperty(Control control,
			String property, String pseudo, CSSEngine engine) throws Exception;

}
