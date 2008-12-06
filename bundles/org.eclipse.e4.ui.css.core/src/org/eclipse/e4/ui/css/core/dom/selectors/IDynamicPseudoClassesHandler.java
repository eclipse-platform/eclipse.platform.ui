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
package org.eclipse.e4.ui.css.core.dom.selectors;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;

/**
 * Interface to manage dynamic pseudo classes handler like (...:focus,
 * ...:hover).
 */
public interface IDynamicPseudoClassesHandler {

	/**
	 * Initialize the <code>element</code>. In this method you can add
	 * Listener to the element if it is Swing container, SWT Widget.
	 * 
	 * @param element
	 * @param engine
	 */
	public void intialize(Object element, CSSEngine engine);

	/**
	 * Dispose the <code>element</code>. In this method you can remove
	 * Listener to the element if it is Swing container, SWT Widget.
	 * 
	 * @param element
	 * @param engine
	 */
	public void dispose(Object element, CSSEngine engine);
}
