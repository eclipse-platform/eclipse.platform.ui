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

import java.util.Collection;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * Interface CSS Property Handler provider to manage
 * <ul>
 * <li>the strategy to provide list of {@link ICSSPropertyHandler} linked to a
 * CSS Property.</li>
 * <li>the strategy to get the CSS default style declaration of an element</li>
 * </ul>
 */
public interface ICSSPropertyHandlerProvider {

	/**
	 * Return list of {@link ICSSPropertyHandler} linked to the CSS Property
	 * <code>property</code>.
	 */
	public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
			String property) throws Exception;

	/**
	 * Return the default CSS style declaration of the <code>element</code>
	 * before apply the <code>newStyle</code> {@link CSSStyleDeclaration}.
	 */
	public CSSStyleDeclaration getDefaultCSSStyleDeclaration(CSSEngine engine,
			Object element, CSSStyleDeclaration newStyle, String pseudoE)
			throws Exception;

	/**
	 * Return list of {@link ICSSPropertyHandler} linked to the CSS Property
	 * <code>property</code> for the provided element.
	 *
	 * @param element
	 *            the DOM element
	 * @return the handlers for the element
	 */
	public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
			Object element, String property) throws Exception;

	/**
	 * Return the list of applicable properties for <code>element</code>
	 *
	 * @param element
	 *            the DOM element
	 */
	public Collection<String> getCSSProperties(Object element);

}
