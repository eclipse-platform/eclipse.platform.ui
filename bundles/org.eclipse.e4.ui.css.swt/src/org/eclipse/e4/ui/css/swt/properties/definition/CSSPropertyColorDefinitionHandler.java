/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 459961
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.definition;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.definition.ColorDefinitionElement;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorDefinitionOverridable;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyColorDefinitionHandler implements ICSSPropertyHandler {
	private static final String COLOR_PROP = "color";

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (element instanceof ColorDefinitionElement && COLOR_PROP.equals(property)) {
			IColorDefinitionOverridable definition =
					(IColorDefinitionOverridable) ((ColorDefinitionElement) element).getNativeWidget();
			definition.setValue(CSSSWTColorHelper.getRGBA(value).rgb);
		}
		return false;
	}

}
