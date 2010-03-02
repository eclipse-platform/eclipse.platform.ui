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

package org.eclipse.ui.internal;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.3
 * 
 */
public class AnimatedTabFeedback extends ImageCycleFeedbackBase {

	private CTabItem tabItem;

	/**
	 * @param parentShell
	 */
	public AnimatedTabFeedback(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	public AnimatedTabFeedback(Shell parentShell, CTabItem item,
			Image[] images) {
		super(parentShell, images);
		tabItem = item;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.AnimationFeedbackBase#initialize(org.eclipse.ui.internal.AnimationEngine)
	 */
	public void initialize(AnimationEngine engine) {
		// TODO Auto-generated method stub
		background = tabItem.getParent().getBackground();
		display = tabItem.getParent().getDisplay();
	}

	public void saveStoppedImage() {
		stoppedImage = tabItem.getImage();
	}

	public void setStoppedImage(Image image) {
		tabItem.setImage(image);
	}

	public void showImage(Image image) {
		if (tabItem.isDisposed())
			return;
		tabItem.setImage(image);
	}

}
