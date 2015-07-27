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
package org.eclipse.e4.ui.css.core.dom.properties.converters;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSValue;

/**
 * A one-way converter.
 *
 * This interface is not intended to be implemented by clients; clients should
 * subclass {@link AbstractCSSValueConverter}.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public interface ICSSValueConverter {

	/**
	 * Returns the type to which this converter can convert. The return type is
	 * Object rather than Class to optionally support richer type systems than
	 * the one provided by Java reflection.
	 *
	 * @return the type to which this converter can convert, or null if this
	 *         converter is untyped
	 */
	public Object getToType();

	/**
	 * Returns the result of the conversion of the given CSSValue
	 * <code>value</code>.
	 *
	 * @param value
	 *            the CSSValue to convert, of type {@link #getFromType()}
	 * @param engine
	 * @param context
	 *
	 * @return the converted object, of type {@link #getToType()}
	 */
	public Object convert(CSSValue value, CSSEngine engine, Object context)
			throws Exception;

	/**
	 * Returns String {@link CSSValue} of the result of the conversion of the
	 * given Object <code>value</code>.
	 *
	 * @param value
	 *
	 * @param engine
	 * @param context
	 *
	 * @return
	 */
	public String convert(Object value, CSSEngine engine, Object context)
			throws Exception;

	/**
	 * Returns String {@link CSSValue} of the result of the conversion of the
	 * given Object <code>value</code>. <code>config</code> can be used to
	 * manage format of the CSSValue String to return.
	 *
	 * @param value
	 * @param engine
	 * @param context
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public String convert(Object value, CSSEngine engine, Object context,
			ICSSValueConverterConfig config) throws Exception;
}
