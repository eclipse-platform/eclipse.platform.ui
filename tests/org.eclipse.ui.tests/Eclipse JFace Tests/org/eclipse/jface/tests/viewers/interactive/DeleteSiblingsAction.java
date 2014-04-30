/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		if (fAll)
			element.getContainer().deleteChildren();
		else
			element.getContainer().deleteSomeChildren();
	}
}
