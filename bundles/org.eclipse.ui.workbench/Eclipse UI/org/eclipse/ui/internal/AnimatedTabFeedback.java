/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

public class AnimatedTabFeedback extends ImageCycleFeedbackBase {

	private CTabItem tabItem;

	public AnimatedTabFeedback(Shell parentShell) {
		super(parentShell);
	}

	public AnimatedTabFeedback(Shell parentShell, CTabItem item, Image[] images) {
		super(parentShell, images);
		tabItem = item;
	}

	@Override
	public void initialize(AnimationEngine engine) {
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
		if (tabItem.isDisposed()) {
			return;
		}
		tabItem.setImage(image);
	}

}
