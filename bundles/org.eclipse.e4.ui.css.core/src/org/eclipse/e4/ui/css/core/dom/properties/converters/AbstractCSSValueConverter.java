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
package org.eclipse.e4.ui.css.core.dom.properties.converters;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;

/**
 * Abstract base class for converters.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public abstract class AbstractCSSValueConverter implements ICSSValueConverter {

	private Object toType;

	public AbstractCSSValueConverter(Object toType) {
		this.toType = toType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter#getToType()
	 */
	public Object getToType() {
		return toType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter#convert(java.lang.Object,
	 *      org.eclipse.e4.ui.css.core.engine.CSSEngine, java.lang.Object)
	 */
	public String convert(Object value, CSSEngine engine, Object context)
			throws Exception {
		return convert(value, engine, context, null);
	}
}
