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
package org.eclipse.e4.ui.css.core.dom.parsers;

/**
 * CSS Parser factory to manage instance of {@link CSSParser}.
 */
public interface ICSSParserFactory {

	/**
	 * Return instance of {@link CSSParser}.
	 *
	 * @return
	 */
	public CSSParser makeCSSParser();
}
