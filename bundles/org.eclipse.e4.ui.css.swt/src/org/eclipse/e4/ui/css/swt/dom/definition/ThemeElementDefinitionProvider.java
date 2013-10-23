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
package org.eclipse.e4.ui.css.swt.dom.definition;

import org.eclipse.e4.ui.internal.css.swt.definition.IColorDefinitionOverridable;

import org.eclipse.e4.ui.internal.css.swt.definition.IFontDefinitionOverridable;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;

public class ThemeElementDefinitionProvider implements IElementProvider {
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof IFontDefinitionOverridable) {
			return new FontDefinitionElement((IFontDefinitionOverridable) element, engine);
		}
		if (element instanceof IColorDefinitionOverridable) {
			return new ColorDefinitionElement((IColorDefinitionOverridable) element, engine);
		}
		return null;
	}

}
