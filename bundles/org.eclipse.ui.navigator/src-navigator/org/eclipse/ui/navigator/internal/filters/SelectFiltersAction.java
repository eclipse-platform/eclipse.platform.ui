/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
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
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.internal.NavigatorMessages;

/**
 * 
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 *  
 */
public class SelectFiltersAction extends Action implements IAction {

	private final CommonNavigator commonNavigator;

	public SelectFiltersAction(CommonNavigator aCommonNavigator) {
		super(NavigatorMessages.getString("SelectFiltersActionDelegate.0")); //$NON-NLS-1$
		setToolTipText(NavigatorMessages.getString("SelectFiltersActionDelegate.1")); //$NON-NLS-1$
		commonNavigator = aCommonNavigator;
	}

	public void run() {
		CommonFilterSelectionDialog filterSelectionDialog = new CommonFilterSelectionDialog(commonNavigator);
		filterSelectionDialog.open();
	}

}