/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;

/**
 * Element provider to retrieve w3c {@link Element} which wrap the native widget
 * (SWT Control, Swing JComponent...).
 */
public interface IElementProvider {

	/**
	 * Return the w3c Element which wrap the native widget <code>element</code>
	 * (SWT Control, Swing JComponent). The <code>element</code> can be the
	 * w3c Element.
	 *
	 * @param element
	 * @param engine
	 *
	 * @return
	 */
	public Element getElement(Object element, CSSEngine engine);

}
