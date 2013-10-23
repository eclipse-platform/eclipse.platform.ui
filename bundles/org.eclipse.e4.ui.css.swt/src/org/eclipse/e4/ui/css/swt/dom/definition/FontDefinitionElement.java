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

import org.eclipse.e4.ui.css.core.engine.CSSEngine;

import org.eclipse.e4.ui.internal.css.swt.definition.IFontDefinitionOverridable;

public class FontDefinitionElement extends ThemeDefinitionElement<IFontDefinitionOverridable> {
	public FontDefinitionElement(IFontDefinitionOverridable definition,
			CSSEngine engine) {
		super(definition, engine);
	}
}
