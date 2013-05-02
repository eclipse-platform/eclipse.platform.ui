/*******************************************************************************
 * Copyright (c) 2009, 2013 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   IBM Corporation - ongoing development
 ******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.Comparator;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * A wrapper that holds a reference to the styles defined in a CSS rule block,
 * together with all the information needed to calculate a matching selector's
 * precedence.
 */
final class StyleWrapper {

	private static class StyleWrapperComparator implements Comparator {
	
		public int compare(final Object object1, final Object object2) {
			int result = 0;
			StyleWrapper wrapper1 = (StyleWrapper) object1;
			StyleWrapper wrapper2 = (StyleWrapper) object2;
			if (wrapper1.specificity > wrapper2.specificity) {
				result = 1;
			} else if (wrapper1.specificity < wrapper2.specificity) {
				result = -1;
			} else if (wrapper1.position > wrapper2.position) {
				result = 1;
			} else if (wrapper1.position < wrapper2.position) {
				result = -1;
			}
			return result;
		}
	}

	/**
	 * A comparator for {@link StyleWrapper}s.
	 */
	public static final StyleWrapperComparator COMPARATOR = new StyleWrapperComparator();

	public final CSSStyleDeclaration style;
	public final int specificity;
	public final int position;

	public StyleWrapper(CSSStyleDeclaration style, int specificity,
			int position) {
		this.style = style;
		this.specificity = specificity;
		this.position = position;
	}
}
