/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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

	@Override
	public void initialize(AnimationEngine engine) {
		// TODO Auto-generated method stub
		background = tabItem.getParent().getBackground();
		display = tabItem.getParent().getDisplay();
	}

	@Override
	public void saveStoppedImage() {
		stoppedImage = tabItem.getImage();
	}

	@Override
	public void setStoppedImage(Image image) {
		tabItem.setImage(image);
	}

	@Override
	public void showImage(Image image) {
		if (tabItem.isDisposed())
			return;
		tabItem.setImage(image);
	}

}
