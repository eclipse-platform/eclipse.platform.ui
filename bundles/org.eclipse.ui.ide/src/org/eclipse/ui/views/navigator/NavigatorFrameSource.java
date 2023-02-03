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
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.views.framelist.TreeFrame;
import org.eclipse.ui.views.framelist.TreeViewerFrameSource;

/**
 * Frame source for the resource navigator.
 *
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
@Deprecated(forRemoval = true)
public class NavigatorFrameSource extends TreeViewerFrameSource {

	private ResourceNavigator navigator;

	/**
	 * Constructs a new frame source for the specified resource navigator.
	 *
	 * @param navigator the resource navigator
	 */
	public NavigatorFrameSource(ResourceNavigator navigator) {
		super(navigator.getTreeViewer());
		this.navigator = navigator;
	}

	/**
	 * Returns a new frame. This implementation extends the super implementation by
	 * setting the frame's tool tip text to show the full path for the input
	 * element.
	 */
	@Override
	protected TreeFrame createFrame(Object input) {
		TreeFrame frame = super.createFrame(input);
		frame.setName(navigator.getFrameName(input));
		frame.setToolTipText(navigator.getFrameToolTipText(input));
		return frame;
	}

	/**
	 * Also updates the navigator's title.
	 */
	@Override
	protected void frameChanged(TreeFrame frame) {
		IResource resource = (IResource) frame.getInput();
		IProject project = resource.getProject();

		if (project != null && project.isOpen() == false) {
			MessageDialog.openInformation(navigator.getViewSite().getShell(),
					ResourceNavigatorMessages.NavigatorFrameSource_closedProject_title,
					NLS.bind(ResourceNavigatorMessages.NavigatorFrameSource_closedProject_message, project.getName()));
			navigator.getFrameList().back();
		} else {
			super.frameChanged(frame);
			navigator.updateTitle();
		}
	}
}
