/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;

/**
 * A label provider implementation which, by default, uses an element's
 * <code>toString</code> value for its text and <code>null</code> for its image.
 * <p>
 * This class may be used as is, or subclassed to provide richer labels.
 * Subclasses may override any of the following methods:
 * </p>
 * <ul>
 * <li><code>isLabelProperty</code></li>
 * <li><code>getImage</code></li>
 * <li><code>getText</code></li>
 * <li><code>dispose</code></li>
 * </ul>
 */
public class LabelProvider extends BaseLabelProvider implements ILabelProvider {

	/**
	 * Creates a new label provider.
	 */
	public LabelProvider() {
	}


	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns <code>null</code>.
	 * Subclasses may override.
	 */
	@Override
	public Image getImage(Object element) {
		return null;
	}

	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns the element's
	 * <code>toString</code> string. Subclasses may override.
	 */
	@Override
	public String getText(Object element) {
		return element == null ? "" : element.toString();//$NON-NLS-1$
	}
}
