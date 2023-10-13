/*******************************************************************************
 * Copyright (c) 2013 Rüdiger Herrmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rüdiger Herrmann - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.layout;

import static org.junit.Assert.fail;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TreeColumnLayoutTest {

	private Display display;
	private Shell parent;

	@Test
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

	@Before
	public void setUp() throws Exception {
		display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}
		parent = new Shell(display, SWT.NONE);
	}

	@After
	public void tearDown() throws Exception {
		parent.dispose();
	}

	private void flushPendingEvents() {
		while (display.readAndDispatch()) {
		}
	}

}
