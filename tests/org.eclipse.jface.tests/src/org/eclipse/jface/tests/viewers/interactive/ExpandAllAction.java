/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class ExpandAllAction extends TestBrowserAction {

	public ExpandAllAction(String label, TestBrowser browser) {
		super(label, browser);
	}

	@Override
	public void run() {
		Viewer viewer = getBrowser().getViewer();
		if (viewer instanceof AbstractTreeViewer treeViewer) {
			treeViewer.expandAll();
		}
	}
}
