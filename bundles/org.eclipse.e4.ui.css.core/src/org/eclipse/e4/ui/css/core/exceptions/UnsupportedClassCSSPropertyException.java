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
package org.eclipse.e4.ui.css.core.exceptions;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.dom.properties.providers.CSSPropertyHandlerLazyProviderImpl;

/**
 * Exception used when java Class CSS property is not retrieved.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * @see CSSPropertyHandlerLazyProviderImpl
 */
public class UnsupportedClassCSSPropertyException extends Exception {
	private static final long serialVersionUID = 1L;
	private final Class<?> clazz;

	public UnsupportedClassCSSPropertyException(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public String getMessage() {
		return clazz + " must implement " + ICSSPropertyHandler.class.getName();
	}

}
