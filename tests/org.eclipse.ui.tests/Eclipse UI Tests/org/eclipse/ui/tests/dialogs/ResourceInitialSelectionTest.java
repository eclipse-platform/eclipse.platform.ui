/*******************************************************************************
 * Copyright (c) 2019, 2024 Emmanuel Chebbi and others.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that FilteredResourcesSelectionDialog selects its initial selection
 * when opened. See also bug 214491.
 *
 * @since 3.14
 */
public class ResourceInitialSelectionTest {

	/** The names of the files created within the test project. */
	private final static List<String> FILE_NAMES = asList("foo.txt", "bar.txt", "foofoo");

	/** The test files stored by name. */
	private final static Map<String, IFile> FILES = new HashMap<>();

	private FilteredResourcesSelectionDialog dialog;

	private IProject project;


	@Before
	public void doSetUp() throws Exception {
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
			file.create(new byte[0], true, false, new NullProgressMonitor());
			FILES.put(fileName, file);
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		// Assert files have been properly created

		Display display = PlatformUI.getWorkbench().getDisplay();

		for (String fileName : FILE_NAMES) {
			DisplayHelper.waitForCondition(display, 1000, () -> project.getFile(fileName).exists());
			assertTrue("File was not created", project.getFile(fileName).exists());
		}
	}

	@After
	public void doTearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
		}
		if (project != null) {
			try {
				Job.getJobManager().wakeUp(DecoratorManager.FAMILY_DECORATE);
				Job.getJobManager().join(DecoratorManager.FAMILY_DECORATE, null);
				project.delete(true, null);
			} catch (Exception e) {
				// try to get a stacktrace which jobs still has project open so that it can not
				// be deleted:
				for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
					Exception exception = new Exception("ThreadDump for thread \"" + entry.getKey().getName() + "\"");
					exception.setStackTrace(entry.getValue());
					e.addSuppressed(exception);
				}
				throw e;
			}
		}
	}
}
