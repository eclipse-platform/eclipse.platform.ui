/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getUndoContext()
	 * @since 3.7
	 */
	protected IUndoContext getUndoContext() {
		return WorkspaceUndoUtil.getProblemsUndoContext();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.views.markers.ExtendedMarkersView#getDeleteOperationName(org.eclipse.core.resources.IMarker[])
	 * @since 3.7
	 */
	protected String getDeleteOperationName(IMarker[] markers) {
		Assert.isLegal(markers.length > 0);
		return markers.length == 1 ? MarkerMessages.deleteProblemMarker_operationName : MarkerMessages.deleteProblemMarkers_operationName;
	}

}
