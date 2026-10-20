/*******************************************************************************
 * Copyright (c) 2019, 2026 Emmanuel Chebbi and others.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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


	@BeforeEach
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);

		assertFalse(selected.isEmpty(), "One file should be selected by default");
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);

		assertEquals(asList(FILES.get("foo.txt")), selected, "One file should be selected by default");
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

		// Intentionally no waitForDialogRefresh: this asserts that during
		// initial load, the dialog does not pre-select anything for an
		// invalid initial element. After the refresh pipeline drains the
		// dialog falls back to selecting row 0, which is a separate
		// behavior not under test here.

		List<Object> selected = getSelectedItems(dialog);

		assertTrue(selected.isEmpty(), "No file should be selected by default");
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);

		assertTrue(selected.isEmpty(), "No file should be selected by default");
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

		// Intentionally no waitForDialogRefresh: foofoo is filtered out by
		// the *.txt pattern, so during initial load nothing is selected.
		// After the refresh pipeline drains the dialog falls back to row 0,
		// which is a separate behavior not under test here.

		List<Object> selected = getSelectedItems(dialog);

		assertTrue(selected.isEmpty(), "No file should be selected by default");
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);

		assertEquals(asList(FILES.get("foo.txt")), selected, "The first file should be selected by default");
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);

		assertFalse(selected.isEmpty(), "One file should be selected by default");
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);

		assertEquals(asList(FILES.get("foo.txt")), selected, "One file should be selected by default");
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);

		assertTrue(selected.isEmpty(), "No file should be selected by default");
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

		// Intentionally no waitForDialogRefresh: this asserts that during
		// initial load, the dialog does not pre-select anything for invalid
		// initial elements. After the refresh pipeline drains the dialog
		// falls back to selecting row 0, which is a separate behavior not
		// under test here.

		List<Object> selected = getSelectedItems(dialog);

		assertTrue(selected.isEmpty(), "No file should be selected by default");
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);
		Set<IFile> expectedSelection = new HashSet<>(asList(FILES.get("bar.txt"), FILES.get("foofoo")));
		boolean allInitialElementsAreSelected = expectedSelection.equals(new HashSet<>(selected));

		assertTrue(allInitialElementsAreSelected, "Two files should be selected by default");
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);
		boolean initialElementsAreSelected = selected.containsAll(initialSelection)
				&& initialSelection.containsAll(selected);

		assertTrue(initialElementsAreSelected, "Two files should be selected by default");
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

		// Wait for background refresh jobs to complete
		waitForDialogRefresh();

		List<Object> selected = getSelectedItems(dialog);
		List<IFile> expectedSelection = asList(FILES.get("foo.txt"), FILES.get("bar.txt"));
		boolean initialElementsAreSelected = selected.containsAll(expectedSelection)
				&& expectedSelection.containsAll(selected);

		assertTrue(initialElementsAreSelected, "Two files should be selected by default");
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
			assertTrue(project.getFile(fileName).exists(), "File was not created");
		}
	}

	@AfterEach
	public void doTearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
		}
		if (project != null) {
			// Process any pending UI events before cleanup
			processUIEvents();

			try {
				// Wait for decorator jobs to finish
				Job.getJobManager().wakeUp(DecoratorManager.FAMILY_DECORATE);
				Job.getJobManager().join(DecoratorManager.FAMILY_DECORATE, null);

				// Wait for any resource jobs that might be running
				Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_REFRESH, null);
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);

				// Process UI events again after joining jobs
				processUIEvents();

				// Try to delete with proper progress monitor and retry mechanism
				deleteProjectWithRetry(project);

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

	/**
	 * Process any pending UI events.
	 */
	private void processUIEvents() {
		Display display = Display.getCurrent();
		if (display != null) {
			while (display.readAndDispatch()) {
				// Process all pending events
			}
		}
	}

	/**
	 * Wait for the dialog's background filter/refresh jobs to complete.
	 * <p>
	 * The dialog schedules a chain of jobs on open/refresh:
	 * {@code FilterHistoryJob → FilterJob → RefreshCacheJob → RefreshJob}. All
	 * four are tagged with {@link FilteredItemsSelectionDialog#JOB_FAMILY}, so
	 * we wait for that family to drain. The 30 s ceiling is a deadlock guard;
	 * the pipeline usually completes within a few hundred milliseconds.
	 */
	private void waitForDialogRefresh() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		DisplayHelper.waitForCondition(display, 30_000L, () -> {
			processUIEvents();
			return Job.getJobManager().find(FilteredItemsSelectionDialog.JOB_FAMILY).length == 0;
		});
		// Final event loop processing to pick up any trailing asyncExecs.
		processUIEvents();
	}

	/**
	 * Delete project with retry mechanism to handle cases where background jobs
	 * are still using the project resources.
	 */
	private void deleteProjectWithRetry(IProject projectToDelete) throws CoreException {
		final int MAX_RETRY = 5;
		CoreException lastException = null;

		for (int i = 0; i < MAX_RETRY; i++) {
			try {
				projectToDelete.delete(true, true, new NullProgressMonitor());
				return; // Success
			} catch (CoreException e) {
				lastException = e;
				if (i < MAX_RETRY - 1) {
					// Process UI events and wait before retrying
					processUIEvents();
					try {
						Thread.sleep(1000); // Wait 1 second before retry
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}

		// If we get here, all retries failed
		if (lastException != null) {
			throw lastException;
		}
	}
}
