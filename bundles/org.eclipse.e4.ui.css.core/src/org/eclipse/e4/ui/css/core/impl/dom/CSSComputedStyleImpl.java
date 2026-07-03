/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
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
 *     EclipseSource - revision
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.List;
import org.eclipse.e4.ui.css.core.dom.CSSProperty;
import org.eclipse.e4.ui.css.core.dom.CSSPropertyList;

/**
 * Computed style merging the declarations of all rules that match an element,
 * ordered by specificity and source position.
 */
public class CSSComputedStyleImpl extends CSSStyleDeclarationImpl {

	private final List<StyleWrapper> styleRules;

	public CSSComputedStyleImpl(List<StyleWrapper> styleRules) {
		super(null);
		this.styleRules = styleRules;
		// TODO [rst] Optimize: A list of StyleWrapper instances could be sorted
		// only once after reading the stylesheet(s).
		this.styleRules.sort(StyleWrapper.COMPARATOR);
		for (StyleWrapper styleWrapper : this.styleRules) {
			addCSSPropertyList(styleWrapper.style().getCSSPropertyList());
		}
	}

	private void addCSSPropertyList(CSSPropertyList properties) {
		int length = properties.getLength();
		for (int i = 0; i < length; i++) {
			CSSProperty property = properties.item(i);
			super.removeProperty(property.getName());
			super.addProperty(property);
		}
	}
}
