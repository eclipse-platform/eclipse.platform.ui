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
package org.eclipse.e4.ui.css.core.exceptions;

/**
 * Exception used when CSS property handler is not retrieved.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class UnsupportedPropertyException extends Exception {
	private static final long serialVersionUID = 1L;
	private String property;

	public UnsupportedPropertyException(String property) {
		if (property == null)
			throw new IllegalArgumentException();
		this.property = property;
	}

	public String getMessage() {
		return "CSS Property " + property + " is not supported.";
	}

}
