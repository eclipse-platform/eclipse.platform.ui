/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.navigator;

import org.eclipse.ui.internal.navigator.framelist.TreeFrame;
import org.eclipse.ui.internal.navigator.framelist.TreeViewerFrameSource;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * Frame source for the common navigator.
 */
public class CommonNavigatorFrameSource extends TreeViewerFrameSource {

	private CommonNavigator navigator;

	/**
	 * Constructs a new frame source for the specified common navigator.
	 *
	 * @param navigator the common navigator
	 */
	public CommonNavigatorFrameSource(CommonNavigator navigator) {
		super(navigator.getCommonViewer());
		this.navigator = navigator;
	}

	/**
	 * Returns a new frame.  This implementation extends the super implementation
	 * by setting the frame's tool tip text to show the full path for the input
	 * element.
	 */
	@Override
	protected TreeFrame createFrame(Object input) {
		TreeFrame frame = super.createFrame(input);
		frame.setName(navigator.getTitle());
		frame.setToolTipText(navigator.getFrameToolTipText(input));
		return frame;
	}

	/**
	 * Also updates the navigator's title.
	 */
	@Override
	protected void frameChanged(TreeFrame frame) {
		super.frameChanged(frame);
		navigator.updateTitle();
	}


}
