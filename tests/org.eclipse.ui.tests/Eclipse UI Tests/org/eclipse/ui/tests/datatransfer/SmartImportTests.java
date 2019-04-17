/*******************************************************************************
 * Copyright (c) 2016, 2019 Red Hat Inc. and others
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.SmartImportRootWizardPage;
import org.eclipse.ui.internal.wizards.datatransfer.SmartImportWizard;
import org.eclipse.ui.tests.datatransfer.contributions.ImportMeProjectConfigurator;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @since 3.12
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
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
	}

	@Override
	public void doTearDown() throws Exception {
		ImportMeProjectConfigurator.configuredProjects.clear();
		try {
			clearAll();
		} finally {
			super.doTearDown();
		}
	}

	private void clearAll() throws CoreException, IOException {
		processEvents();
		boolean closed = true;
		if (dialog != null && !dialog.getShell().isDisposed()) {
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
		WizardDialog dialog = new WizardDialog(getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		try {
			dialog.setBlockOnOpen(false);
			dialog.open();
			processEvents();
			final Button okButton = getFinishButton(dialog.buttonBar);
			assertNotNull(okButton);
			processEventsUntil(() -> okButton.isEnabled(), -1);
			wizard.performFinish();
			waitForJobs(100, 1000); // give the job framework time to schedule the job
			wizard.getImportJob().join();
			waitForJobs(100, 5000); // give some time for asynchronous workspace jobs to complete
		} finally {
			if (!dialog.getShell().isDisposed()) {
				dialog.close();
			}
		}
	}

	/**
	 * @param dialog
	 */
	private Button getFinishButton(Control control) {
		if (control instanceof Button && ((Button) control).getText().equals(IDialogConstants.FINISH_LABEL)) {
			return (Button)control;
		} else if (control instanceof Composite) {
			Button res = null;
			for (Control child : ((Composite)control).getChildren()) {
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
			if (control instanceof FilteredTree) {
				return (CheckboxTreeViewer) ((FilteredTree) control).getViewer();
			} else if (control instanceof Composite) {
				CheckboxTreeViewer res = getTreeViewer((Composite) control);
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
	public void testCancelWizardCancelsJob() {
		SmartImportWizard wizard = new SmartImportWizard();
		wizard.setInitialImportSource(File.listRoots()[0]);
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
			if (control instanceof ToolBar) {
				for (ToolItem item : ((ToolBar) control).getItems()) {
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
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
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
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
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
			wizard.performFinish();
			waitForJobs(100, 1000); // give the job framework time to schedule the job
			wizard.getImportJob().join();
			waitForJobs(100, 5000); // give some time for asynchronous workspace jobs to complete
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

	private static Combo getComboWithSelection(String selection, Composite parent) {
		for (Control control : parent.getChildren()) {
			if (control instanceof Combo && ((Combo) control).getText().equals(selection)) {
				return (Combo) control;
			} else if (control instanceof Composite) {
				Combo res = getComboWithSelection(selection, (Composite) control);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}
}
