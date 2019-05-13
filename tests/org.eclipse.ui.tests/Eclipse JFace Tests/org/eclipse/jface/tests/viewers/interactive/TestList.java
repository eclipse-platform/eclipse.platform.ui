/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class TestList extends TestBrowser {
	@Override
	public Viewer createViewer(Composite parent) {
		ListViewer viewer = new ListViewer(parent);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new TestModelContentProvider());
		return viewer;
	}

	public static void main(String[] args) {
		TestList browser = new TestList();
		browser.setBlockOnOpen(true);
		browser.open(TestElement.createModel(3, 10));
	}

	/**
	 *
	 */
	@Override
	protected void viewerFillMenuBar(MenuManager mgr) {
	}
}
