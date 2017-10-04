/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lucas Bullen - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests that resources are highlighted to match user search input. See Bug
 * 519525, 520250, and 520251 for references.
 *
 * @since 3.14
 */
public class ResourceItemLabelTest extends UITestCase {

	/**
	 * Constructs a new instance of <code>ResourceItemlLabelTest</code>.
	 *
	 * @param name
	 *            The name of the test to be run.
	 */

	public ResourceItemLabelTest(String name) {
		super(name);
	}

	private IProject project;

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(getClass().getName() + "_" + System.currentTimeMillis());
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());
	}

	/**
	 * Tests that the highlighting matches basic substrings
	 *
	 * @throws Exception
	 */
	public void testSubstringMatch() throws Exception {
		Position[] atBeginning = { new Position(0, 2) };
		compareStyleRanges(atBeginning, getStyleRanges("te", "test.txt"));

		Position[] full = { new Position(0, 8) };
		compareStyleRanges(full, getStyleRanges("test.txt", "test.txt"));
	}

	/**
	 * Tests that the highlighting matches CamelCase searches
	 *
	 * @throws Exception
	 */
	public void testCamelCaseMatch() throws Exception {
		Position[] atBeginning = { new Position(0, 1), new Position(4, 1) };
		compareStyleRanges(atBeginning, getStyleRanges("TT", "ThisTest.txt"));

		Position[] nextToEachother = { new Position(0, 1), new Position(4, 2) };
		compareStyleRanges(nextToEachother, getStyleRanges("TAT", "ThisATest.txt"));

		Position[] withSubstrings = { new Position(0, 2), new Position(4, 2) };
		compareStyleRanges(withSubstrings, getStyleRanges("ThTe", "ThisTest.txt"));
	}

	/**
	 * Tests that the highlighting matches searches using '*' and '?'
	 *
	 * @throws Exception
	 */
	public void testPatternMatch() throws Exception {
		Position[] questionMark = { new Position(0, 1), new Position(2, 2) };
		compareStyleRanges(questionMark, getStyleRanges("t?st", "test.txt"));

		Position[] star = { new Position(0, 1), new Position(6, 2) };
		compareStyleRanges(star, getStyleRanges("t*xt", "test.txt"));

		Position[] both = { new Position(0, 1), new Position(2, 1), new Position(6, 2) };
		compareStyleRanges(both, getStyleRanges("t?s*xt", "test.txt"));
	}

	/**
	 * Tests that the highlighting matches extension searches
	 *
	 * @throws Exception
	 */
	public void testExtensionMatch() throws Exception {
		Position[] basic = { new Position(8, 3) };
		compareStyleRanges(basic, getStyleRanges(".MF", "MANIFEST.MF"));

		Position[] withSubstring = { new Position(0, 1), new Position(8, 3) };
		compareStyleRanges(withSubstring, getStyleRanges("M.MF", "MANIFEST.MF"));
	}

	private void compareStyleRanges(Position[] expected, StyleRange[] actual) {
		assertEquals("Length of StyleRanges is incorrect: " + printStyleRanges(actual), expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			assertEquals("Start of StyleRange at index " + i + " is incorrect.", expected[i].offset, actual[i].start);
			assertEquals("Length of StyleRange at index " + i + " is incorrect.", expected[i].length, actual[i].length);
		}
	}

	private FilteredResourcesSelectionDialog dialog;
	private static InputStream stream = new ByteArrayInputStream(new byte[0]);

	private StyleRange[] getStyleRanges(String searchString, String fileName) throws Exception {
		IFile file = project.getFile(fileName);
		file.create(stream, true, new NullProgressMonitor());
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		dialog = new FilteredResourcesSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				true, project,
				IResource.FILE);
		dialog.setInitialPattern(searchString);
		dialog.setBlockOnOpen(false);
		dialog.create();
		dialog.open();
		Shell shell = dialog.getShell();
		Table table = (Table) ((Composite) ((Composite) ((Composite) shell.getChildren()[0]).getChildren()[0])
				.getChildren()[0]).getChildren()[3];

		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return table.getItemCount() > 0;
			}
		}.waitForCondition(shell.getDisplay(), 1000);

		assertEquals("Impropper number of results", 1, table.getItemCount());

		Object data = table.getItem(0).getData("org.eclipse.jfacestyled_label_key_0");

		dialog.close();
		file.delete(true, null);
		if (data == null || !(data instanceof StyleRange[])) {
			fail("No StyleRanges found for the TableItem");
		}

		return (StyleRange[]) data;
	}

	private String printStyleRanges(StyleRange[] styleRanges) {
		if (styleRanges == null) {
			return "null";
		}
		if (styleRanges.length == 0) {
			return "[]";
		}
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		for (StyleRange range : styleRanges) {
			builder.append("{start: ");
			builder.append(range.start);
			builder.append(", length: ");
			builder.append(range.length);
			builder.append("}, ");
		}
		builder.setLength(builder.length() - 2);
		builder.append(']');
		return builder.toString();
	}

	@Override
	protected void doTearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
		}
		if (project != null) {
			project.delete(true, null);
		}
		super.doTearDown();
	}
}
