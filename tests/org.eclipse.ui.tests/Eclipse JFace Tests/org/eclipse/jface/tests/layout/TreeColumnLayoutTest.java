/*******************************************************************************
 * Copyright (c) 2013 Rüdiger Herrmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rüdiger Herrmann - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.layout;

import junit.framework.TestCase;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

public class TreeColumnLayoutTest extends TestCase {

	private Display display;
	private Shell parent;

	public void testBug395890LayoutAfterExpandEventWithDisposedTree() throws Exception {
		Tree tree = new Tree(parent, SWT.NONE);
		TreeColumnLayout layout = new TreeColumnLayout();
		parent.setLayout(layout);
		parent.layout(true, true);
		tree.notifyListeners(SWT.Expand, null);
		tree.dispose();
		try {
			flushPendingEvents();
		} catch (RuntimeException notExpected) {
			fail();
		}
	}

	@Override
	protected void setUp() throws Exception {
		display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}
		parent = new Shell(display, SWT.NONE);
	}

	@Override
	protected void tearDown() throws Exception {
		parent.dispose();
	}

	private void flushPendingEvents() {
		while (display.readAndDispatch()) {
		}
	}

}
