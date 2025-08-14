/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 * - Lucas Bullen (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.tests.datatransfer;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.datatransfer.ImportTestUtils.restoreWorkspaceConfiguration;
import static org.eclipse.ui.tests.datatransfer.ImportTestUtils.setWorkspaceAutoBuild;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEventsUntil;
import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.SmartImportRootWizardPage;
import org.eclipse.ui.internal.wizards.datatransfer.SmartImportWizard;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.datatransfer.contributions.ImportMeProjectConfigurator;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.12
 */
@RunWith(JUnit4.class)
public class SmartImportTests extends UITestCase {

	private WizardDialog dialog;

	public SmartImportTests() {
		super(SmartImportTests.class.getName());
	}

	@Override
	public void doSetUp() throws Exception {
		super.doSetUp();
		ImportMeProjectConfigurator.configuredProjects.clear();
		clearAll();
		setWorkspaceAutoBuild(true);
	}

	@Override
	public void doTearDown() throws Exception {
		ImportMeProjectConfigurator.configuredProjects.clear();
		try {
			clearAll();
			restoreWorkspaceConfiguration();
		} finally {
			super.doTearDown();
		}
	}

	private void clearAll() throws CoreException, IOException {
		processEvents();
		boolean closed = true;
		if (dialog != null && dialog.getShell() != null && !dialog.getShell().isDisposed()) {
			closed = dialog.close();
		}
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			IFile projectDescription = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
			IPath projectDescriptionLocation = projectDescription != null ? projectDescription.getRawLocation() : null;

			project.delete(false, false, new NullProgressMonitor());

			// Bug 535940: The project description which may be created from the test run
			// must be removed or further test runs may fail.
			if (projectDescriptionLocation != null) {
				Path projectDescriptionFile = projectDescriptionLocation.toFile().toPath();
				if (Files.exists(projectDescriptionFile)) {
					Files.delete(projectDescriptionFile);
				}
			}
		}
		waitForJobs(100, 300);
		if (!closed) {
			assertTrue("Wizard dialog was not properly closed!", closed);
		}
	}

	public void runSmartImport(File source) throws OperationCanceledException, InterruptedException {
		SmartImportWizard wizard = new SmartImportWizard();
		wizard.setInitialImportSource(source);
		proceedSmartImportWizard(wizard);
	}

	private void proceedSmartImportWizard(SmartImportWizard wizard) throws InterruptedException {
		Consumer<SmartImportRootWizardPage> doNothing = page -> {};
		proceedSmartImportWizard(wizard, doNothing);
	}

	private void proceedSmartImportWizard(SmartImportWizard wizard, Consumer<SmartImportRootWizardPage> setSettings)
			throws InterruptedException {
		WizardDialog dialog = new WizardDialog(getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		try {
			dialog.setBlockOnOpen(false);
			dialog.open();
			processEvents();
			SmartImportRootWizardPage page = (SmartImportRootWizardPage) wizard
					.getPage(SmartImportRootWizardPage.class.getName());
			setSettings.accept(page);
			final Button okButton = getFinishButton(dialog.buttonBar);
			assertNotNull(okButton);
			processEventsUntil(() -> okButton.isEnabled(), -1);
			finishWizard(wizard);
		} finally {
			if (dialog.getShell() != null && !dialog.getShell().isDisposed()) {
				dialog.close();
			}
		}
	}

	private Button getFinishButton(Control control) {
		if (control instanceof Button b && b.getText().equals(IDialogConstants.FINISH_LABEL)) {
			return b;
		} else if (control instanceof Composite comp) {
			Button res = null;
			for (Control child : comp.getChildren()) {
				res = getFinishButton(child);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	@Test
	public void testImport6Projects() throws IOException, OperationCanceledException, InterruptedException {
		URL url = FileLocator
				.toFileURL(getClass().getResource("/data/org.eclipse.datatransferArchives/ProjectsArchive.zip"));
		File file = new File(url.getFile());
		runSmartImport(file);
		assertEquals(6, ResourcesPlugin.getWorkspace().getRoot().getProjects().length);
	}

	@Test
	public void testImportModularProjectsWithSameName()
			throws IOException, OperationCanceledException, InterruptedException {
		URL url = FileLocator
				.toFileURL(getClass().getResource("/data/org.eclipse.datatransferArchives/project"));
		File file = new File(url.getFile());
		runSmartImport(file);

		// Check expected projects are there
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(10, projects.length);

		Set<String> implProjectNames = new HashSet<>();
		for (IProject project : projects) {
			if (project.getLocation().lastSegment().equals("impl")) {
				implProjectNames.add(project.getName());
			}
		}
		assertEquals(3, implProjectNames.size());
		assertTrue(implProjectNames.contains("impl"));
		assertTrue(implProjectNames.contains("module2_impl"));
		assertTrue(implProjectNames.contains("module3_impl"));
	}

	@Test
	public void testImportProjectWithExistingName()
			throws IOException, OperationCanceledException, InterruptedException {
		URL url = FileLocator
				.toFileURL(getClass()
						.getResource("/data/org.eclipse.datatransferArchives/sameNameProject1/sameNameProject"));
		File file = new File(url.getFile());
		runSmartImport(file);

		// Check expected project is there
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(1, projects.length);

		url = FileLocator
				.toFileURL(getClass()
						.getResource("/data/org.eclipse.datatransferArchives/sameNameProject2/sameNameProject"));
		file = new File(url.getFile());

		SmartImportWizard wizard = new SmartImportWizard();
		wizard.setInitialImportSource(file);
		this.dialog = new WizardDialog(getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.setBlockOnOpen(false);
		dialog.open();
		processEvents();
		processEventsUntil(() -> !dialog.getErrorMessage().isEmpty(), -1);
		SmartImportRootWizardPage page = (SmartImportRootWizardPage) dialog.getCurrentPage();
		CheckboxTreeViewer treeViewer = getTreeViewer((Composite) page.getControl());
		assertNotNull(treeViewer);
		assertEquals(1, treeViewer.getTree().getItemCount());
		assertEquals(0, treeViewer.getCheckedElements().length);
		assertEquals("Project with same name already imported", treeViewer.getTree().getItems()[0].getText(1));
	}

	private CheckboxTreeViewer getTreeViewer(Composite parent) {
		for (Control control : parent.getChildren()) {
			if (control instanceof FilteredTree fTree) {
				return (CheckboxTreeViewer) fTree.getViewer();
			} else if (control instanceof Composite comp) {
				CheckboxTreeViewer res = getTreeViewer(comp);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	@Test
	public void testConfigurationIgnoreNestedProjects()
			throws IOException, OperationCanceledException, InterruptedException {
		URL url = FileLocator
				.toFileURL(getClass().getResource("/data/org.eclipse.datatransferArchives/projectSingleModule"));
		File file = new File(url.getFile());
		runSmartImport(file);

		// Check expected projects are there
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		assertEquals(2, projects.length);

		IProject rootProject = ResourcesPlugin.getWorkspace().getRoot().getProject("projectSingleModule");
		assertTrue("Missing root project", rootProject.exists());
		assertFalse("Root project shouldn't have been configured",
				ImportMeProjectConfigurator.configuredProjects.contains(rootProject));

		assertEquals("Should have one project configured", 1, ImportMeProjectConfigurator.configuredProjects.size());
		Set<IProject> modules = new HashSet<>(Arrays.asList(projects));
		modules.remove(rootProject);
		assertEquals(modules.size(), ImportMeProjectConfigurator.configuredProjects.size());
		assertTrue("All modules should be configured",
				ImportMeProjectConfigurator.configuredProjects.containsAll(modules));
	}

	@Test
	public void testConfigurationCloseImportedProjects() throws Exception {
		String location = ImportTestUtils.copyDataLocation("ImportExistingProjectsWizardTestRebuildProject");
		File file = new File(location);
		SmartImportWizard wizard = new SmartImportWizard();
		wizard.setInitialImportSource(file);
		proceedSmartImportWizard(wizard, page -> page.setCloseProjectsAfterImport(true));

		// Check expected projects are there
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		try {
			assertEquals(2, projects.length);

			IProject rootProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject("ImportExistingProjectsWizardTestRebuildProject");
			assertTrue("Missing test project", rootProject.exists());
			assertFalse("Test project should be closed after import", rootProject.isOpen());
		} finally {
			ImportTestUtils.deleteWorkspaceProjects(projects);
		}
	}

	@Test
	public void testConfigurationFullBuildAfterImportedProjects() throws Exception {
		String location = ImportTestUtils.copyDataLocation("ImportExistingProjectsWizardTestRebuildProject");
		File file = new File(location);
		SmartImportWizard wizard = new SmartImportWizard();
		wizard.setInitialImportSource(file);

		ImportTestUtils.TestBuilder.resetCallCount();
		proceedSmartImportWizard(wizard, page -> {
			page.setCloseProjectsAfterImport(false);
		});
		ImportTestUtils.waitForBuild();

		// Check expected projects are there
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		try {
			assertEquals(2, projects.length);

			IProject rootProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject("ImportExistingProjectsWizardTestRebuildProject");

			assertTrue("Missing test project", rootProject.exists());
			ImportTestUtils.TestBuilder.assertFullBuildWasDone();
		} finally {
			ImportTestUtils.deleteWorkspaceProjects(projects);
		}
	}

	@Test
	public void testCancelWizardCancelsJob() {
		// Use the (probably) largest root as import source so that the importer is
		// running long enough that we can cancel it.
		// First version of this test used File.listRoots()[0] but while usually sorted
		// it is not guaranteed. Additional the first root might not what you expect.
		// The Windows test machine returns A:\ as first root which is not suitable for
		// this test.
		File importRoot = new File(Util.isWindows() ? "C:\\" : "/");
		if (!importRoot.isDirectory()) {
			importRoot = File.listRoots()[0];
		}
		TestPlugin.getDefault().getLog().log(new Status(IStatus.INFO, TestPlugin.PLUGIN_ID,
				"Testing job cancel with root: " + importRoot.getAbsolutePath()));

		SmartImportWizard wizard = new SmartImportWizard();
		wizard.setInitialImportSource(importRoot);
		this.dialog = new WizardDialog(getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.setBlockOnOpen(false);
		dialog.open();
		SmartImportRootWizardPage page = (SmartImportRootWizardPage) dialog.getCurrentPage();
		ProgressMonitorPart wizardProgressMonitor = page.getWizardProgressMonitor();
		assertNotNull("Wizard should have a progress monitor", wizardProgressMonitor);
		ToolItem stopButton = getStopButton(wizardProgressMonitor);
		processEventsUntil(() -> stopButton.isEnabled(), 10000);
		assertTrue("Wizard should show progress monitor", wizardProgressMonitor.isVisible());
		assertTrue("Stop button should be enabled", stopButton.isEnabled());
		Event clickButtonEvent = new Event();
		clickButtonEvent.widget = stopButton;
		clickButtonEvent.item = stopButton;
		clickButtonEvent.type = SWT.Selection;
		clickButtonEvent.doit = true;
		clickButtonEvent.stateMask = SWT.BUTTON1;
		stopButton.notifyListeners(SWT.Selection, clickButtonEvent);
		processEventsUntil(() -> !wizardProgressMonitor.isVisible(), 10000);
		assertFalse("Progress monitor should be hidden within 10 seconds", wizardProgressMonitor.isVisible());
	}

	private static ToolItem getStopButton(ProgressMonitorPart part) {
		for (Control control : part.getChildren()) {
			if (control instanceof ToolBar toolbar) {
				for (ToolItem item : toolbar.getItems()) {
					if (item.getToolTipText().equals(JFaceResources.getString("ProgressMonitorPart.cancelToolTip"))) { //$NON-NLS-1$ ))
						return item;
					}
				}
			}
		}
		return null;
	}

	@Test
	public void testInitialWorkingSets() throws Exception {
		IWorkingSetManager workingSetManager = getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet = workingSetManager.createWorkingSet("testWorkingSet", new IAdaptable[0]);
		workingSet.setId("org.eclipse.ui.resourceWorkingSetPage");
		workingSetManager.addWorkingSet(workingSet);
		File directoryToImport = Files
				.createTempDirectory(getClass().getSimpleName() + "_" + System.currentTimeMillis()).toFile();
		try {
			SmartImportWizard wizard = new SmartImportWizard();
			wizard.setInitialImportSource(directoryToImport);
			wizard.setInitialWorkingSets(Collections.singleton(workingSet));
			proceedSmartImportWizard(wizard);
			assertEquals("Projects were not added to working set", 1, workingSet.getElements().length);
		} finally {
			for (File child : directoryToImport.listFiles()) {
				child.delete();
			}
			directoryToImport.delete();
			workingSetManager.removeWorkingSet(workingSet);
		}
	}

	@Test
	public void testChangedWorkingSets() throws Exception {
		IWorkingSetManager workingSetManager = getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet = workingSetManager.createWorkingSet("testWorkingSet", new IAdaptable[0]);
		workingSet.setId("org.eclipse.ui.resourceWorkingSetPage");
		workingSetManager.addWorkingSet(workingSet);
		IWorkingSet workingSet2 = workingSetManager.createWorkingSet("testWorkingSet2", new IAdaptable[0]);
		workingSet2.setId("org.eclipse.ui.resourceWorkingSetPage");
		workingSetManager.addWorkingSet(workingSet2);
		WorkbenchPlugin.getDefault().getDialogSettings().put("workingset_selection_history",
				new String[] { workingSet.getName(), workingSet2.getName() });
		File directoryToImport = Files
				.createTempDirectory(getClass().getSimpleName() + "_" + System.currentTimeMillis()).toFile();
		try {
			SmartImportWizard wizard = new SmartImportWizard();
			wizard.setInitialImportSource(directoryToImport);
			wizard.setInitialWorkingSets(Collections.singleton(workingSet));
			this.dialog = new WizardDialog(getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
			dialog.setBlockOnOpen(false);
			dialog.open();
			processEvents();
			final Button okButton = getFinishButton(dialog.buttonBar);
			assertNotNull(okButton);
			processEventsUntil(() -> okButton.isEnabled(), -1);
			SmartImportRootWizardPage page = (SmartImportRootWizardPage) dialog.getCurrentPage();
			Combo combo = getComboWithSelection(workingSet.getName(), (Composite) page.getControl());
			combo.select(1);
			Event e = new Event();
			e.widget = combo;
			e.display = combo.getDisplay();
			e.type = SWT.Selection;
			e.text = workingSet2.getName();
			e.index = 1;
			combo.notifyListeners(SWT.Selection, e);
			processEvents();
			processEventsUntil(() -> okButton.isEnabled(), -1);
			finishWizard(wizard);
			assertEquals("WorkingSet2 should be selected", Collections.singleton(workingSet2),
					page.getSelectedWorkingSets());
			assertEquals("Projects were not added to working set", 1, workingSet2.getElements().length);
		} finally {
			for (File child : directoryToImport.listFiles()) {
				child.delete();
			}
			directoryToImport.delete();
			workingSetManager.removeWorkingSet(workingSet);
			workingSetManager.removeWorkingSet(workingSet2);
		}
	}

	private void finishWizard(SmartImportWizard wizard) throws InterruptedException {
		wizard.performFinish();
		wizard.getCurrentImportJob().join();
	}

	/**
	 * Bug 559600 - [SmartImport] Label provider throws exception if results contain
	 * filesystem root
	 */
	@Test
	public void testBug559600() throws Exception {
		AtomicInteger errors = new AtomicInteger();
		ILogListener errorListener = (status, plugin) -> {
			if (status.getSeverity() == IStatus.ERROR) {
				errors.incrementAndGet();
			}
		};

		CharArrayWriter existingDialogSettings = new CharArrayWriter();
		SmartImportWizard wizard = new SmartImportWizard();
		try {
			Platform.addLogListener(errorListener);
			wizard.getDialogSettings().save(existingDialogSettings);
			wizard.setInitialImportSource(File.listRoots()[0]);
			wizard.getDialogSettings().put("SmartImportRootWizardPage.STORE_HIDE_ALREADY_OPEN", true);
			wizard.getDialogSettings().put("SmartImportRootWizardPage.STORE_NESTED_PROJECTS", false);
			this.dialog = new WizardDialog(getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
			dialog.setBlockOnOpen(false);
			dialog.open();
			SmartImportRootWizardPage page = (SmartImportRootWizardPage) dialog.getCurrentPage();
			ProgressMonitorPart wizardProgressMonitor = page.getWizardProgressMonitor();
			processEventsUntil(() -> !wizardProgressMonitor.isVisible(), 10000);
			assertEquals("Label provider (or something else) produced error", 0, errors.get());
			assertFalse("Searching projects should be done within 10 seconds", wizardProgressMonitor.isVisible());
		} finally {
			dialog.close();
			Platform.removeLogListener(errorListener);
			wizard.getDialogSettings().load(new CharArrayReader(existingDialogSettings.toCharArray()));
		}
	}

	private static Combo getComboWithSelection(String selection, Composite parent) {
		for (Control control : parent.getChildren()) {
			if (control instanceof Combo combo && combo.getText().equals(selection)) {
				return combo;
			} else if (control instanceof Composite comp) {
				Combo res = getComboWithSelection(selection, comp);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}
}
