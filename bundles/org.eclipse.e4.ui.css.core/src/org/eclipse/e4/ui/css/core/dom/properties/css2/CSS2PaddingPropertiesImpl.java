/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;

public class CSS2PaddingPropertiesImpl implements CSSValue {

	public CSSValue top;

	public CSSValue bottom;

	public CSSValue left;

	public CSSValue right;

	@Override
	public String getCssText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short getCssValueType() {
		return CSSValue.CSS_CUSTOM;
	}

	@Override
	public void setCssText(String arg0) throws DOMException {
		// TODO Auto-generated method stub
	}

}
