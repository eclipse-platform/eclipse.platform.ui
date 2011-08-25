/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * 
	 * @param property
	 * @return
	 * @throws Exception
	 */
	public Collection getCSSPropertyHandlers(String property) throws Exception;

	/**
	 * Return the default CSS style declaration of the <code>element</code>
	 * before apply the <code>newStyle</code> {@link CSSStyleDeclaration}.
	 * 
	 * @param engine
	 * @param element
	 * @param newStyle
	 * @param pseudoE
	 * @return
	 * @throws Exception
	 */
	public CSSStyleDeclaration getDefaultCSSStyleDeclaration(CSSEngine engine,
			Object element, CSSStyleDeclaration newStyle, String pseudoE)
			throws Exception;
}
