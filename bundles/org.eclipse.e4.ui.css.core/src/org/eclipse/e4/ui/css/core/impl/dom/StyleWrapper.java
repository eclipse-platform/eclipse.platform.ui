/*******************************************************************************
 * Copyright (c) 2009, 2026 EclipseSource and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   IBM Corporation - ongoing development
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 422702
 ******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.Comparator;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * A wrapper that holds a reference to the styles defined in a CSS rule block,
 * together with all the information needed to calculate a matching selector's
 * precedence.
 */
final record StyleWrapper(CSSStyleDeclaration style, int specificity, int position) {

	/**
	 * A comparator for {@link StyleWrapper}s.
	 */
	static final Comparator<StyleWrapper> COMPARATOR = Comparator.comparingInt(StyleWrapper::specificity)
			.thenComparingInt(StyleWrapper::position);

}
