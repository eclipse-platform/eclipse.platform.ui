/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.parser;

/**
 * Thrown by the hand-written CSS parser when the input cannot be parsed.
 * Replaces {@code org.w3c.css.sac.CSSException} for the internal parser, which
 * like its SAC predecessor is unchecked.
 */
public class CssParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int line;
	private final int column;

	public CssParseException(String message, int line, int column) {
		super(message + " (line " + line + ", column " + column + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.line = line;
		this.column = column;
	}

	/** 1-based line where parsing failed. */
	public int getLine() {
		return line;
	}

	/** 1-based column where parsing failed. */
	public int getColumn() {
		return column;
	}
}
