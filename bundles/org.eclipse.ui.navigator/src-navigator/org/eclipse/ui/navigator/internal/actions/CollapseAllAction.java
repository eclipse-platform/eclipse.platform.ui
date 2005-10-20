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
package org.eclipse.ui.navigator.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.internal.NavigatorMessages;

/**
 * This action delegate collapses all expanded elements in a Navigator view.
 * 
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class CollapseAllAction extends Action implements IAction {

	private final CommonViewer commonViewer;

	public CollapseAllAction(CommonViewer aViewer) {
		super(NavigatorMessages.getString("CollapseAllActionDelegate.0")); //$NON-NLS-1$
		setToolTipText(NavigatorMessages.getString("CollapseAllActionDelegate.1")); //$NON-NLS-1$ 
		commonViewer = aViewer;
	}

	public void run() {
		if (commonViewer != null)
			commonViewer.collapseAll();
	}
}