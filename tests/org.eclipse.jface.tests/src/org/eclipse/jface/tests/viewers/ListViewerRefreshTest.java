/*******************************************************************************
 * Copyright (c) 2006, 2018 Brad Reynolds, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *    IBM Corporation - Bug 493357
 *    Lucas Bullen (Red Hat Inc.) - Revert disabling tests from Bug 493357
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ListViewerRefreshTest {
	/**
	 * Used for viewing the UI. Set to 0 if not wanting to see the UI.
	 */
	private static final int DELAY = 0;

	private Shell shell = null;

	private Label label = null;

	private ListViewer viewer = null;

	private ArrayList<String> input = null;

	@Before
	public void setUp() throws Exception {
		shell = new Shell();
		shell.setSize(400, 200);
		shell.setLayout(new FillLayout());
		label = new Label(shell, SWT.WRAP);
		viewer = new ListViewer(shell);
		input = new ArrayList<>();

		for (int i = 0; i < 50; i++) {
			input.add("item " + i); //$NON-NLS-1$
		}

		viewer.setContentProvider(new ContentProvider());
		viewer.setInput(input);
		shell.layout();
		shell.open();
	}

	@After
	public void tearDown() throws Exception {
		shell.dispose();
		shell = null;
	}

	/**
	 * Asserts the ability to refresh without a selection and preserve the scrolled
	 * to position.
	 *
	 * @throws Exception
	 */
	@Test
	public void testNoSelectionRefresh() throws Exception {
		shell.setText("Lost Scrolled Position Test"); //$NON-NLS-1$
		readAndDispatch();

		run("Scrolled to position 30.", () -> viewer.reveal(input.get(30)));

		run("Refreshed viewer without a selection.", () -> viewer.refresh());

		// BUG: The top index should not be the first item.

		DisplayHelper.waitAndAssertCondition(shell.getDisplay(),
				() -> assertNotEquals(viewer.getList().getTopIndex(), 0));
	}

	/**
	 * Asserts the ability to refresh with a selection and preserve the scrolled to
	 * position.
	 *
	 * @throws Exception
	 */
	@Test
	public void testSelectionRefresh() throws Exception {
		shell.setText("Preserved Scrolled Position Test"); //$NON-NLS-1$
		readAndDispatch();

		run("Setting selection to index 30.", () -> viewer.setSelection(new StructuredSelection(input.get(30))));

		// Ensure that to index is 0
		viewer.getList().setTopIndex(0);

		run("Refreshed viewer with selection.", () -> viewer.refresh());

		// Checking that the viewer is not scrolling
		assertEquals(0, viewer.getList().getTopIndex());

		viewer.getList().showSelection();

		DisplayHelper.waitAndAssertCondition(shell.getDisplay(),
				() -> assertNotEquals(viewer.getList().getTopIndex(), 0));
	}

	/**
	 * Runs the runnable and displays the description.
	 *
	 * @param description
	 * @param runnable
	 */
	private void run(String description, Runnable runnable) {
		runnable.run();
		label.setText(description);

		readAndDispatch();
	}

	/**
	 * Flush UI events and {@link #DELAY delays}.
	 */
	private static void readAndDispatch() {
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
		}

		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static class ContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<?>) inputElement).toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
}
