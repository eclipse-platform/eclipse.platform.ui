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
package org.eclipse.e4.ui.css.core.dom.parsers;

import org.eclipse.e4.ui.css.core.impl.dom.parsers.CSSParserFactoryImpl;

/**
 * CSS Parser factory to manage instance of {@link ICSSParserFactory}.
 */
public abstract class CSSParserFactory implements ICSSParserFactory {

	/**
	 * Obtain a new instance of a {@link ICSSParserFactory}.
	 *
	 * @return
	 */
	public static ICSSParserFactory newInstance() {
		return new CSSParserFactoryImpl();
	}

}
