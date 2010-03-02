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
import org.eclipse.ui.internal.LegacyAnimationFeedback;
import org.eclipse.ui.internal.RectangleAnimationFeedbackBase;

/**
 * Return the default (legacy) animation.
 * 
 * @since 3.3
 *
 */
public class LegacyAnimations extends Animations {
	/** Default c'tor */
	public LegacyAnimations() {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.tweaklets.Animations#getFeedback()
	 */
	public RectangleAnimationFeedbackBase createFeedback(Shell shell) {
		return new LegacyAnimationFeedback(shell, null, null);
	}

}
