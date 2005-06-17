/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		super(SearchMessages.CollapseAllAction_0); 
		fViewer= viewer;
		setToolTipText(SearchMessages.CollapseAllAction_1); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_COLLAPSE_ALL);
	}
	
	public void run() {
		fViewer.collapseAll();
	}
}
