/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

/**
 * @since 3.0
 */
public final class LineRange implements ILineRange {
	
	private int fStartLine;
	private int fNumberOfLines;
	
	public LineRange(int startLine, int numberOfLines) {
		fStartLine= startLine;
		fNumberOfLines= numberOfLines;
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineRange#getStartLine()
	 */
	public int getStartLine() {
		return fStartLine;
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineRange#getNumberOfLines()
	 */
	public int getNumberOfLines() {
		return fNumberOfLines;
	}
}
