/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import org.eclipse.jface.tests.viewers.TestElement;

public class DeleteSiblingsAction extends TestSelectionAction {

	boolean fAll = false;

	public DeleteSiblingsAction(String label, TestBrowser browser, boolean all) {
		super(label, browser);
		fAll = all;
	}

	@Override
	public void run(TestElement element) {
		if (fAll) {
			element.getContainer().deleteChildren();
		} else {
			element.getContainer().deleteSomeChildren();
		}
	}
}
