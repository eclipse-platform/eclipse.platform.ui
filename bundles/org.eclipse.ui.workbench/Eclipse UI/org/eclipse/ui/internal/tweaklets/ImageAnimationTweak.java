/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public ImageAnimationTweak() {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.tweaklets.Animations#getFeedback()
	 */
	public RectangleAnimationFeedbackBase createFeedback(Shell shell) {
		return new RectangleAnimationImageFeedback(shell, null, null);
	}

}
