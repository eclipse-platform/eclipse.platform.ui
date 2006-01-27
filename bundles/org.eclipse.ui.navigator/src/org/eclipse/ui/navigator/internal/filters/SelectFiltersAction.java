/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Feb 9, 2004
 *
 */
package org.eclipse.ui.navigator.internal.filters;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class SelectFiltersAction extends Action implements IAction {

	private final CommonViewer commonViewer; 

	/**
	 * Create an action to drive the Filter selection dialog
	 * for a particular instance of the CommonViewer.
	 * @param aCommonViewer
	 */
	public SelectFiltersAction(CommonViewer aCommonViewer) {
		super(CommonNavigatorMessages.SelectFiltersActionDelegate_0); 
		setToolTipText(CommonNavigatorMessages.SelectFiltersActionDelegate_1); 
		commonViewer = aCommonViewer; 
	}

	public void run() {
		CommonFilterSelectionDialog filterSelectionDialog = new CommonFilterSelectionDialog(commonViewer);
		filterSelectionDialog.open();
	}

}
