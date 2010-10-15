/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;


/**
 * The Problems view is the view supplied by the IDE to show problems.
 * 
 * @since 3.4
 */
public class ProblemsView extends MarkerSupportView {

	/**
	 * Create a new instance of the receiver.
	 */
	public ProblemsView() {
		super(MarkerSupportRegistry.PROBLEMS_GENERATOR);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.views.markers.ExtendedMarkersView#updateTitleImage(java.lang.Integer[])
	 */
	void updateTitleImage(Integer[] counts) {
		Image image= WorkbenchPlugin.getDefault().getSharedImages().getImage(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW);
		if (counts[0].intValue() > 0)
			image= WorkbenchPlugin.getDefault().getSharedImages().getImage(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_ERROR);
		else if (counts[1].intValue() > 0)
			image= WorkbenchPlugin.getDefault().getSharedImages().getImage(IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEMS_VIEW_WARNING);
		setTitleImage(image);
	}

}
