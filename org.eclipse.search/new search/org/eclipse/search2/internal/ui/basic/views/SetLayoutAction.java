/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

public class SetLayoutAction extends Action {

	private AbstractTextSearchViewPage fPage;
	private boolean fIsFlatMode;

	public SetLayoutAction(AbstractTextSearchViewPage page, String label, String tooltip, boolean flatMode) {
		super(label,  IAction.AS_CHECK_BOX);
		fPage= page;
		setToolTipText(tooltip); //$NON-NLS-1$
		fIsFlatMode= flatMode;
	}
	
	public void run() {
		fPage.setFlatLayout(fIsFlatMode);
	}
}
