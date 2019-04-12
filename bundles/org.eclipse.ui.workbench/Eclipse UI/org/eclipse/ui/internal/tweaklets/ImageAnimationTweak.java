/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.RectangleAnimationFeedbackBase;
import org.eclipse.ui.internal.RectangleAnimationImageFeedback;

/**
 * Return an animation feedback that uses images.
 *
 * @since 3.3
 *
 */
public class ImageAnimationTweak extends Animations {
	/** Default c'tor */
	public ImageAnimationTweak() {
	}

	@Override
	public RectangleAnimationFeedbackBase createFeedback(Shell shell) {
		return new RectangleAnimationImageFeedback(shell, null, null);
	}

}
