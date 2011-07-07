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
