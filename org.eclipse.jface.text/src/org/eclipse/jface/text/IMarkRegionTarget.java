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

package org.eclipse.jface.text;

/**
 * A mark region target to support marked regions as found in emacs.
 *
 * @since 2.0
 */
public interface IMarkRegionTarget {

	/**
	 * Sets or clears a mark at the current cursor position.
	 *
	 * @param set sets the mark if <code>true</code>, clears otherwise.
	 */
	void setMarkAtCursor(boolean set);

	/**
	 * Swaps the mark and cursor position if the mark is in the visible region.
	 */
	void swapMarkAndCursor();
}
