/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.definition;

import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.normalizeId;
import org.eclipse.e4.ui.css.swt.dom.definition.ThemeDefinitionElement;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.definition.IThemeElementDefinitionOverridable;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyThemeElementDefinitionHandler implements ICSSPropertyHandler {
	private final static String CATEGORY_PROP = "category";
	
	private final static String LABEL_PROP = "label";
	
	private final static String DESCRIPTION_PROP = "description";
	
	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(element instanceof ThemeDefinitionElement<?>)) {
			return false;
		}
		
		IThemeElementDefinitionOverridable<?> definition = 
			(IThemeElementDefinitionOverridable<?>) ((ThemeDefinitionElement<?>) element)
			.getNativeWidget();		
		
		if (CATEGORY_PROP.equals(property)) {
			definition.setCategoryId(normalizeId(value.getCssText().substring(1)));
		} else if (LABEL_PROP.equals(property)) {
			definition.setName(value.getCssText());
		} else if (DESCRIPTION_PROP.equals(property)) {
			definition.setDescription(value.getCssText());
		}
		
		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}
}
