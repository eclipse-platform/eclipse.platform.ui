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
import org.eclipse.jface.tests.viewers.TestModelChange;

public class AddChildAction extends TestSelectionAction {

	int fEventKind;

	public AddChildAction(String label, TestBrowser browser) {
		this(label, browser, TestModelChange.INSERT);
	}

	public AddChildAction(String label, TestBrowser browser, int eventKind) {
		super(label, browser);
		fEventKind = eventKind;
	}

	@Override
	public void run(TestElement element) {
		element.addChild(fEventKind);
	}
}
