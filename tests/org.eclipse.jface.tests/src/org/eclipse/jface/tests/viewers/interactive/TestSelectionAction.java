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

import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public abstract class TestSelectionAction extends TestBrowserAction implements ISelectionChangedListener {
	public TestSelectionAction(String label, TestBrowser browser) {
		super(label, browser);
		browser.getViewer().addSelectionChangedListener(this);
		setEnabled(false);
	}

	public TestElement getTestElement(ISelection selection) {
		if (selection instanceof IStructuredSelection s) {
			return (TestElement) s.getFirstElement();
		}
		return null;
	}

	@Override
	final public void run() {
		TestElement testElement = getTestElement(getBrowser().getViewer().getSelection());
		if (testElement != null) {
			run(testElement);
		}
	}

	/**
	 * The default implementation calls run(TestElement) on every element contained
	 * in the vector.
	 *
	 * @param o
	 */
	abstract protected void run(TestElement o);

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		setEnabled(!event.getSelection().isEmpty());
	}
}
