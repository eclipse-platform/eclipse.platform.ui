/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lucas Bullen - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
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
		compareStyleRanges(atBeginning, getStyleRanges("te", "test.txt"), "test.txt", "");

		Position[] full = { new Position(0, 8) };
		compareStyleRanges(full, getStyleRanges("test.txt", "test.txt"), "test.txt", "");

		Position[] withDigits = { new Position(0, 3) };
		compareStyleRanges(withDigits, getStyleRanges("t3s", "t3st.txt"), "t3st.txt", "");
	}

	/**
	 * Tests that the highlighting matches CamelCase searches
	 *
	 * @throws Exception
	 */
	public void testCamelCaseMatch() throws Exception {
		Position[] atBeginning = { new Position(0, 1), new Position(4, 1) };
		compareStyleRanges(atBeginning, getStyleRanges("TT", "ThisTest.txt"), "ThisTest.txt", "");

		Position[] nextToEachother = { new Position(0, 1), new Position(4, 2) };
		compareStyleRanges(nextToEachother, getStyleRanges("TAT", "ThisATest.txt"), "ThisATest.txt", "");

		Position[] withSubstrings = { new Position(0, 2), new Position(4, 2) };
		compareStyleRanges(withSubstrings, getStyleRanges("ThTe", "ThisTest.txt"), "ThisTest.txt", "");

		Position[] withDigits = { new Position(0, 2), new Position(4, 2) };
		compareStyleRanges(withDigits, getStyleRanges("Th3T", "This3Test.txt"), "This3Test.txt", "");

		Position[] skippingDigit = { new Position(0, 2), new Position(5, 1) };
		compareStyleRanges(skippingDigit, getStyleRanges("ThT", "This3Test.txt"), "This3Test.txt", "");
	}

	/**
	 * Tests that the highlighting matches searches using '*' and '?'
	 *
	 * @throws Exception
	 */
	public void testPatternMatch() throws Exception {
		Position[] questionMark = { new Position(0, 1), new Position(2, 2) };
		compareStyleRanges(questionMark, getStyleRanges("t?st", "test.txt"), "test.txt", "");

		Position[] star = { new Position(0, 1), new Position(6, 2) };
		compareStyleRanges(star, getStyleRanges("t*xt", "test.txt"), "test.txt", "");

		Position[] both = { new Position(0, 1), new Position(2, 1), new Position(6, 2) };
		compareStyleRanges(both, getStyleRanges("t?s*xt", "test.txt"), "test.txt", "");

		Position[] withDigits = { new Position(0, 1), new Position(2, 2), new Position(7, 3) };
		compareStyleRanges(withDigits, getStyleRanges("t?s3*x3t", "tes3t.tx3t"), "tes3t.tx3t", "");
	}

	/**
	 * Tests that regex symbols do not break search
	 *
	 * @throws Exception
	 */
	public void testBug529451() throws Exception {
		Position[] basic = { new Position(4, 1) };
		compareStyleRanges(basic, getStyleRanges("*$", "test$.txt"), "test$.txt", "");

		Position[] multiple = { new Position(0, 3), new Position(7, 6), new Position(14, 1) };
		compareStyleRanges(multiple, getStyleRanges("^${*}[])(+?-", "^${skip}[])(+s-"), "^${skip}[])(+s-", "");
	}

	/**
	 * Tests that the highlighting matches searches using '<' and ' '
	 *
	 * @throws Exception
	 */
	public void testDisableAutoPrefixMatching() throws Exception {
		Position[] questionMark = { new Position(0, 1), new Position(4, 4) };
		compareStyleRanges(questionMark, getStyleRanges("M*file<", "Makefile"), "Makefile", "");

		Position[] star = { new Position(0, 1), new Position(4, 4) };
		compareStyleRanges(star, getStyleRanges("M*file ", "MockFile"), "MockFile", "");

		Position[] both = { new Position(0, 3), new Position(6, 1) };
		compareStyleRanges(both, getStyleRanges("CreS<", "CreateStuff.java"), "CreateStuff.java", "");
	}

	/**
	 * Tests that the highlighting matches extension searches
	 *
	 * @throws Exception
	 */
	public void testExtensionMatch() throws Exception {
		Position[] basic = { new Position(8, 3) };
		compareStyleRanges(basic, getStyleRanges(".MF", "MANIFEST.MF"), "MANIFEST.MF", "");

		Position[] withSubstring = { new Position(0, 1), new Position(8, 3) };
		compareStyleRanges(withSubstring, getStyleRanges("M.MF", "MANIFEST.MF"), "MANIFEST.MF", "");

		Position[] withCamelCase = { new Position(4, 3), new Position(8, 1) };
		compareStyleRanges(withCamelCase, getStyleRanges(".TxT", "test.TxxT"), "test.TxxT", "");

		Position[] withPattern = { new Position(4, 2), new Position(8, 1) };
		compareStyleRanges(withPattern, getStyleRanges(".t*t", "test.txxt"), "test.txxt", "");

		Position[] withDigits = { new Position(4, 2), new Position(8, 1) };
		compareStyleRanges(withDigits, getStyleRanges(".3*3", "test.3xx3"), "test.3xx3", "");
	}

	/**
	 * Tests for Bug 528301: Camel Case match with precursing letter matches
	 *
	 * @throws Exception
	 */
	public void testBug528301() throws Exception {
		Position[] withSameLettersBeforeCamel = { new Position(0, 1), new Position(3, 1), new Position(5, 1) };
		compareStyleRanges(withSameLettersBeforeCamel, getStyleRanges("ABC", "AbcBzCz.txt"), "AbcBzCz.txt", "");

		Position[] withDigits = { new Position(0, 1), new Position(4, 1), new Position(6, 1), new Position(8, 1) };
		compareStyleRanges(withDigits, getStyleRanges("AB5C", "Ab5cBz5zCz.txt"), "Ab5cBz5zCz.txt", "");
	}

	/**
	 * Tests for Bug 531610: Open Resource dialog doesn't show paths for duplicated
	 * files
	 *
	 * @throws Exception
	 */
	public void testBug531610() throws Exception {
		IFolder folder = project.getFolder("folder");
		IFile fileB = folder.getFile("file");
		folder.create(true, true, new NullProgressMonitor());
		fileB.create(stream, true, new NullProgressMonitor());
		StyleRange[] ranges = getStyleRanges("file", "file");
		// if true adds "/folder" length to the range
		boolean withFolder = false;
		for (StyleRange range : ranges) {
			if (range.length > 3 + project.getName().length()) {
				withFolder = true;
			}
		}
		Position[] withDigits = { new Position(0, 4) }; // " - "
		// withFolder - "/folder"
		compareStyleRanges(withDigits, ranges, "file", withFolder ? "/folder" : "");
	}

	private void compareStyleRanges(Position[] expected, StyleRange[] actual, String fileName, String fileParentPath) {
		assertEquals("Length of StyleRanges is incorrect: " + printStyleRanges(actual), expected.length + 1,
				actual.length);
		int i;
		for (i = 0; i < actual.length - 1; i++) {
			assertEquals("Start of StyleRange at index " + i + " is incorrect.", expected[i].offset, actual[i].start);
			assertEquals("Length of StyleRange at index " + i + " is incorrect.", expected[i].length, actual[i].length);
		}
		assertEquals("Start of file path StyleRange is incorrect.", fileName.length(), actual[i].start);
		assertEquals("Length of file path StyleRange at index is incorrect.",
				3 + project.getName().length() + fileParentPath.length(), actual[i].length);
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
		dialog.setBlockOnOpen(false);
		dialog.create();
		dialog.open();
		Shell shell = dialog.getShell();

		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return project.getFile(fileName).exists();
			}
		}.waitForCondition(shell.getDisplay(), 1000);

		assertTrue("File was not created", project.getFile(fileName).exists());
		dialog.reloadCache(true, new NullProgressMonitor());

		((Text) dialog.getPatternControl()).setText(searchString);
		Table table = (Table) ((Composite) ((Composite) ((Composite) shell.getChildren()[0]).getChildren()[0])
				.getChildren()[0]).getChildren()[3];

		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return table.getItemCount() > 0;
			}
		}.waitForCondition(shell.getDisplay(), 3000);

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
