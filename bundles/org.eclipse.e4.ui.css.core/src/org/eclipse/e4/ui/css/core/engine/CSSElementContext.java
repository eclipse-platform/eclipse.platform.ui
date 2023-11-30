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
package org.eclipse.e4.ui.css.core.engine;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.w3c.dom.Element;

/**
 * Context of the {@link Element} which wrap the native widget (SWT widget,
 * Swing Component...).
 *
 * This context can be used to store any data.
 */
public interface CSSElementContext {

	/**
	 * Return the {@link Element} which wrap the native widget.
	 */
	public Element getElement();

	/**
	 * Set the {@link Element} which wrap the native widget.
	 */
	public void setElement(Element newElement);

	/**
	 * Set data <code>value</code> into the context with <code>key</code>.
	 */
	public void setData(Object key, Object value);

	/**
	 * Get data with <code>key</code>.
	 */
	public Object getData(Object key);

	/**
	 * Return true if element provider has changed and false otherwise.
	 */
	public boolean elementMustBeRefreshed(IElementProvider elementProvider);

	/**
	 * Set {@link IElementProvider} used to get the Element wich wrap the native
	 * widget.
	 */
	public void setElementProvider(IElementProvider elementProvider);
}
