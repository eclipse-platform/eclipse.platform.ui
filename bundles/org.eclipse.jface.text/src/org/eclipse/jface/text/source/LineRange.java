/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

/**
 * Default implementation of {@link ILineRange}.
 *
 * @since 3.0
 */
public final class LineRange implements ILineRange {

	private final int fStartLine;
	private final int fNumberOfLines;

	/**
	 * Creates a new line range with the given specification.
	 *
	 * @param startLine the start line
	 * @param numberOfLines the number of lines
	 */
	public LineRange(int startLine, int numberOfLines) {
		fStartLine= startLine;
		fNumberOfLines= numberOfLines;
	}

	@Override
	public int getStartLine() {
		return fStartLine;
	}

	@Override
	public int getNumberOfLines() {
		return fNumberOfLines;
	}

	@Override
	public String toString() {
		StringBuilder builder= new StringBuilder();
		builder.append("LineRange [start line="); //$NON-NLS-1$
		builder.append(fStartLine);
		builder.append(", count="); //$NON-NLS-1$
		builder.append(fNumberOfLines);
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
}
