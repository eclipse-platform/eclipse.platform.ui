/*******************************************************************************
 * Copyright (c) 2006, 2025 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.jface.dialogs;

import org.eclipse.swt.widgets.Control;


/**
 * Factory for control animators used by JFace to animate the display of an SWT
 * Control. Through the use of the method
 * {@link org.eclipse.jface.util.Policy#setAnimatorFactory(AnimatorFactory)}
 * a new type of animator factory can be plugged into JFace.
 *
 * @since 3.2
 * @deprecated as of 3.3, this class is no longer used.
 */
@Deprecated(forRemoval = true, since = "2025-03")
public class AnimatorFactory {
	/**
	 * Creates a new ControlAnimator for use by JFace in animating
	 * the display of an SWT Control. <p>
	 * Subclasses should override this method.
	 *
	 * @param control the SWT Control to de displayed
	 * @return the ControlAnimator.
	 * @since 3.2
	 */
	public ControlAnimator createAnimator(Control control) {
		return new ControlAnimator(control);
	}
}
