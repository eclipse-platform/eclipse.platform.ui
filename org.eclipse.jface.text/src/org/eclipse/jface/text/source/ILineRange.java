/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

/**
 * Describes a range of lines.
 *
 * @since 3.0
 */
public interface ILineRange {

	/**
	 * Returns the start line of this line range or <code>-1</code>.
	 *
	 * @return the start line of this line range or <code>-1</code> if this line range is invalid.
	 */
	int getStartLine();

	/**
	 * Returns the number of lines of this line range or <code>-1</code>.
	 *
	 * @return the number of lines in this line range or <code>-1</code> if this line range is invalid.
	 */
	int getNumberOfLines();
}
