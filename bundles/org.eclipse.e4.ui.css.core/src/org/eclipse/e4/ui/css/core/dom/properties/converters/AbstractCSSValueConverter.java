/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
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
package org.eclipse.e4.ui.css.core.dom.properties.converters;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;

/**
 * Abstract base class for converters.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public abstract class AbstractCSSValueConverter implements ICSSValueConverter {

	private Object toType;

	public AbstractCSSValueConverter(Object toType) {
		this.toType = toType;
	}

	@Override
	public Object getToType() {
		return toType;
	}

	@Override
	public String convert(Object value, CSSEngine engine, Object context)
			throws Exception {
		return convert(value, engine, context, null);
	}
}
