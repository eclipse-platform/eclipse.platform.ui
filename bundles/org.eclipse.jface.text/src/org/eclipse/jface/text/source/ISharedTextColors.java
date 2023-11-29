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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


/**
 * Manages SWT color objects. Until the <code>dispose</code> method is called,
 * the same color object is returned for equal <code>RGB</code> values.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 2.1
 */
public interface ISharedTextColors {

	/**
	 * Returns the color object for the value represented by the given
	 * <code>RGB</code> object.
	 *
	 * @param rgb the RBG color specification
	 * @return the color object for the given RGB value
	 */
	Color getColor(RGB rgb);

	/**
	 * Tells this object to dispose all its managed objects- Note that colors do not need dispose
	 * anymore
	 */
	default void dispose() {
		// nothing to do anymore as colors do not require disposal
	}
}
