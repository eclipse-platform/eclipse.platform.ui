/*******************************************************************************
 * Copyright (c) 2019 Emmanuel Chebbi
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Emmanuel Chebbi - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import static java.util.Arrays.asList;
import static org.junit.Assume.assumeFalse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests that FilteredResourcesSelectionDialog selects its initial selection
 * when opened. See also bug 214491.
 *
 * @since 3.14
 */
@RunWith(JUnit4.class)
public class ResourceInitialSelectionTest extends UITestCase {

	/** The names of the files created within the test project. */
	private final static List<String> FILE_NAMES = asList("foo.txt", "bar.txt", "foofoo");

	/** The test files stored by name. */
	private final static Map<String, IFile> FILES = new HashMap<>();

	/** Used to fill created files with an empty content. */
	private static InputStream stream = new ByteArrayInputStream(new byte[0]);

	private FilteredResourcesSelectionDialog dialog;

	private IProject project;

	/**
	 * Constructs a new instance of <code>ResourceItemInitialSelectionTest</code>.
	 */
	public ResourceInitialSelectionTest() {
		super(ResourceInitialSelectionTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		FILES.clear();
		createProject();
	}

	/**
	 * Test that a resource is selected by default even without initial selection.
	 */
	@Test
	public void testSingleSelectionAndNoInitialSelectionWithInitialPattern() {
		boolean hasMultiSelection = false;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialPattern("**");
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertFalse("One file should be selected by default", selected.isEmpty());
	}

	/**
	 * Test that a specific resource can be selected by default.
	 */
	@Test
	public void testSingleSelectionAndOneInitialSelectionWithInitialPattern() {
		boolean hasMultiSelection = false;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialPattern("**");
		dialog.setInitialElementSelections(asList(FILES.get("foo.txt")));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertEquals("One file should be selected by default", asList(FILES.get("foo.txt")), selected);
	}

	/**
	 * Test that no resource is selected by default when the specified one does not
	 * exist.
	 */
	@Test
	public void testSingleSelectionAndOneInitialNonExistingSelectionWithInitialPattern() {
		boolean hasMultiSelection = false;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialPattern("**");
		dialog.setInitialElementSelections(asList("not an available item"));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertTrue("No file should be selected by default", selected.isEmpty());
	}

	/**
	 * Test that no resource is selected by default when no initial pattern is set.
	 */
	@Test
	public void testSingleSelectionAndOneInitialSelectionWithoutInitialPattern() {
		boolean hasMultiSelection = false;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialElementSelections(asList(FILES.get("foo.txt")));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertTrue("No file should be selected by default", selected.isEmpty());
	}

	/**
	 * Test that no resource is selected by default when the initial pattern does
	 * not match.
	 */
	@Test
	public void testSingleSelectionAndOneFilteredSelection() {
		boolean hasMultiSelection = false;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialPattern("*.txt");
		dialog.setInitialElementSelections(asList(FILES.get("foofoo")));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertTrue("No file should be selected by default", selected.isEmpty());
	}

	/**
	 * Test that only the first specified resource is selected when multi selection
	 * is disabled.
	 */
	@Test
	public void testSingleSelectionAndTwoInitialSelectionsWithInitialPattern() {
		boolean hasMultiSelection = false;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialPattern("**");
		dialog.setInitialElementSelections(asList(FILES.get("foo.txt"), FILES.get("bar.txt")));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertEquals("The first file should be selected by default", asList(FILES.get("foo.txt")), selected);
	}

	/**
	 * Test that one resource is selected by default multi selection is enabled but
	 * no initial selection is specified.
	 */
	@Test
	public void testMultiSelectionAndNoInitialSelectionWithInitialPattern() {
		boolean hasMultiSelection = true;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialPattern("**");
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertFalse("One file should be selected by default", selected.isEmpty());
	}

	/**
	 * Test that a specified resource can be selected by default when multi
	 * selection is enabled.
	 */
	@Test
	public void testMultiSelectionAndOneInitialSelectionWithInitialPattern() {
		boolean hasMultiSelection = true;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialPattern("**");
		dialog.setInitialElementSelections(asList(FILES.get("foo.txt")));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertEquals("One file should be selected by default", asList(FILES.get("foo.txt")), selected);
	}

	/**
	 * Test that no resource is selected by default when no initial pattern is set.
	 */
	@Test
	public void testMultiSelectionAndOneInitialSelectionWithoutInitialPattern() {
		boolean hasMultiSelection = true;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialElementSelections(asList(FILES.get("foo.txt")));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertTrue("No file should be selected by default", selected.isEmpty());
	}

	/**
	 * Test that no item is selected by default when non existing items are
	 * specified.
	 */
	@Test
	public void testMultiSelectionAndTwoInitialNonExistingSelectionWithInitialPattern() {
		boolean hasMultiSelection = true;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialPattern("**");
		dialog.setInitialElementSelections(asList("not an available item", "still not an available item"));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);

		assertTrue("No file should be selected by default", selected.isEmpty());
	}

	/**
	 * Test that only existing items are selected by default when some of the
	 * specified initial selections do not exist.
	 */
	@Test
	public void testMultiSelectionAndSomeInitialNonExistingSelectionWithInitialPattern() {
		boolean hasMultiSelection = true;
		dialog = createDialog(hasMultiSelection);

		dialog.setInitialPattern("**");
		dialog.setInitialElementSelections(asList(FILES.get("bar.txt"), "not an available item", FILES.get("foofoo")));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);
		Set<IFile> expectedSelection = new HashSet<>(asList(FILES.get("bar.txt"), FILES.get("foofoo")));
		boolean allInitialElementsAreSelected = expectedSelection.equals(new HashSet<>(selected));

		assertTrue("Two files should be selected by default", allInitialElementsAreSelected);
	}

	/**
	 * Test that several specified resources can be selected by default.
	 */
	@Test
	public void testMultiSelectionAndTwoInitialSelectionsWithInitialPattern() {
		assumeFalse("Test fails on Windows: Bug 559353", Platform.OS_WIN32.equals(Platform.getOS()));

		boolean hasMultiSelection = true;
		List<IFile> initialSelection = asList(FILES.get("foo.txt"), FILES.get("bar.txt"));

		dialog = createDialog(hasMultiSelection);
		dialog.setInitialPattern("**");
		dialog.setInitialElementSelections(initialSelection);
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);
		boolean initialElementsAreSelected = selected.containsAll(initialSelection)
				&& initialSelection.containsAll(selected);

		assertTrue("Two files should be selected by default", initialElementsAreSelected);
	}

	/**
	 * Test that several specified resources can be selected by default but are
	 * ignored if the initial pattern does not match.
	 */
	@Test
	public void testMultiSelectionAndTwoInitialFilteredSelections() {
		assumeFalse("Test fails on Windows: Bug 559353", Platform.OS_WIN32.equals(Platform.getOS()));

		boolean hasMultiSelection = true;

		dialog = createDialog(hasMultiSelection);
		dialog.setInitialPattern("*.txt");
		dialog.setInitialElementSelections(asList(FILES.get("foo.txt"), FILES.get("bar.txt"), FILES.get("foofoo")));
		dialog.open();
		dialog.refresh();

		List<Object> selected = getSelectedItems(dialog);
		List<IFile> expectedSelection = asList(FILES.get("foo.txt"), FILES.get("bar.txt"));
		boolean initialElementsAreSelected = selected.containsAll(expectedSelection)
				&& expectedSelection.containsAll(selected);

		assertTrue("Two files should be selected by default", initialElementsAreSelected);
	}

	private FilteredResourcesSelectionDialog createDialog(boolean multiSelection) {
		FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), multiSelection, project,
				IResource.FILE);
		dialog.setBlockOnOpen(false);
		return dialog;
	}

	private List<Object> getSelectedItems(FilteredResourcesSelectionDialog dialog) {
		Table table = (Table) ((Composite) ((Composite) ((Composite) dialog.getShell().getChildren()[0])
				.getChildren()[0]).getChildren()[0]).getChildren()[3];
		List<Object> selected = Arrays.stream(table.getSelection()).map(TableItem::getData)
				.toList();
		return selected;
	}

	private void createProject() throws CoreException {
		project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(getClass().getName() + "_" + System.currentTimeMillis());
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());

		// Create files

		for (String fileName : FILE_NAMES) {
			IFile file = project.getFile(fileName);
			file.create(stream, true, new NullProgressMonitor());
			FILES.put(fileName, file);
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		// Assert files have been properly created

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		for (String fileName : FILE_NAMES) {
			new DisplayHelper() {
				@Override
				protected boolean condition() {
					return project.getFile(fileName).exists();
				}
			}.waitForCondition(shell.getDisplay(), 1000);

			assertTrue("File was not created", project.getFile(fileName).exists());
		}
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
