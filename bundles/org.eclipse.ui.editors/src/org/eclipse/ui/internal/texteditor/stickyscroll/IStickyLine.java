/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.stickyscroll;

import org.eclipse.swt.custom.StyleRange;

/**
 * Representation of a sticky line.
 */
public interface IStickyLine {

	/**
	 * Returns the line number of the sticky line.
	 * 
	 * @return the line number of the sticky line
	 */
	int getLineNumber();

	/**
	 * Returns the text of the sticky line.
	 * 
	 * @return the text of the sticky line
	 */
	String getText();

	/**
	 * Returns the style ranges of the sticky line.
	 * 
	 * @return the style ranges of the sticky line
	 */
	StyleRange[] getStyleRanges();

}
