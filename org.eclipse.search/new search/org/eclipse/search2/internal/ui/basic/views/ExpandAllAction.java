/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     Karsten Thoms <karsten.thoms@itemis.de> Bug 522335
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.search.internal.ui.SearchPluginImages;

import org.eclipse.search2.internal.ui.SearchMessages;

public class ExpandAllAction extends Action {

	private TreeViewer fViewer;

	public ExpandAllAction() {
		super(SearchMessages.ExpandAllAction_label);
		setToolTipText(SearchMessages.ExpandAllAction_tooltip);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_EXPAND_ALL);
	}

	public void setViewer(TreeViewer viewer) {
		fViewer= viewer;
	}

	@Override
	public void run() {
		if (fViewer != null) {
			fViewer.getTree().setRedraw(false);
			try {
				fViewer.expandAll();
			} finally {
				fViewer.getTree().setRedraw(true);
			}
		}
	}
}
