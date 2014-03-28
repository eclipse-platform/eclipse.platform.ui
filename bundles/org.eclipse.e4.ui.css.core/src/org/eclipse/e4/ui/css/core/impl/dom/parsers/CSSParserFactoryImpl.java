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
package org.eclipse.e4.ui.css.core.impl.dom.parsers;

import org.eclipse.e4.ui.css.core.dom.parsers.CSSParser;
import org.eclipse.e4.ui.css.core.dom.parsers.CSSParserFactory;

/**
 * {@link CSSParserFactory} implementation.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public class CSSParserFactoryImpl extends CSSParserFactory {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.css.core.dom.parsers.ICSSParserFactory#makeCSSParser()
	 */
	@Override
	public CSSParser makeCSSParser() {
		return new CSSParserImpl();
	}
}
