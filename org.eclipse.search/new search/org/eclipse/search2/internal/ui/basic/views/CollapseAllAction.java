/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search2.internal.ui.SearchMessages;

public class CollapseAllAction extends Action {

	private TreeViewer fViewer;

	public CollapseAllAction(TreeViewer viewer) {
		super(SearchMessages.getString("CollapseAllAction.0")); //$NON-NLS-1$
		fViewer= viewer;
		setToolTipText(SearchMessages.getString("CollapseAllAction.1")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_COLLAPSE_ALL);
	}
	
	public void run() {
		fViewer.collapseAll();
	}
}
