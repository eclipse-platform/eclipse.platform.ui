/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

/**
 * Abstract class which manage CSS Property composite like border:solid black
 * 1px; It dispatch
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public abstract class AbstractCSSPropertyCompositeHandler implements
		ICSSPropertyCompositeHandler {

	/**
	 * Apply CSS Property composite and dispatch CSS Property if CSSValue is
	 * CSSValueList by calling applyCSSProperty for each item of CSSValue.
	 */
	public void applyCSSPropertyComposite(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			CSSValueList valueList = (CSSValueList) value;
			int length = valueList.getLength();
			for (int i = 0; i < length; i++) {
				CSSValue value2 = valueList.item(i);
				applyCSSProperty(element, value2, pseudo, engine);
			}
		} else {
			applyCSSProperty(element, value, pseudo, engine);
		}
	}

	/**
	 * Apply CSS Property.
	 */
	public abstract void applyCSSProperty(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

}
