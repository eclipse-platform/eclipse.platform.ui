/*******************************************************************************
 * Copyright (c) 2023 ETAS GmbH and others, all rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     ETAS GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.dialogs;

import static org.eclipse.ui.PlatformUI.getWorkbench;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ImportExportWizard;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.ProjectRecord;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;

public class ResourcePathCopyTest extends UITestCase {

	private static final String DATA_PATH_PREFIX = "data/org.eclipse.datatransferArchives/";
	private static final String ARCHIVE_JAVA_PROJECT = "helloworld";

	/**
	 * @param testName
	 */
	public ResourcePathCopyTest(String testName) {
		super(testName);
	}

	@Test
	public void testPathCopy() throws CoreException, IOException, HeadlessException, UnsupportedFlavorException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] workspaceProjects = root.getProjects();
		for (IProject workspaceProject : workspaceProjects) {
			FileUtil.deleteProject(workspaceProject);
		}
		URL archiveFile = FileLocator.toFileURL(FileLocator.find(TestPlugin.getDefault().getBundle(),
				new Path(DATA_PATH_PREFIX + ARCHIVE_JAVA_PROJECT + ".zip"), null));
		WizardProjectsImportPage wizardProjectsImportPage = getNewWizard();
		Set<String> projects = Set.of("HelloWorld");
		wizardProjectsImportPage.getProjectFromDirectoryRadio().setSelection((false));
		wizardProjectsImportPage.updateProjectsList(archiveFile.getPath());
		ProjectRecord[] selectedProjects = wizardProjectsImportPage.getProjectRecords();
		ArrayList<String> projectNames = new ArrayList<>();
		for (ProjectRecord selectedProject : selectedProjects) {
			assertFalse(selectedProject.hasConflicts());
			projectNames.add(selectedProject.getProjectName());
		}
		assertTrue("Not all projects were found correctly in zip", projectNames.containsAll(projects));

		CheckboxTreeViewer projectsList = wizardProjectsImportPage.getProjectsList();
		projectsList.setCheckedElements(selectedProjects);
		wizardProjectsImportPage.createProjects();

		workspaceProjects = root.getProjects();
		assertEquals("Incorrect Number of projects imported", 1, workspaceProjects.length);

		IWorkbenchPage page = getWorkbench().showPerspective(EmptyPerspective.PERSP_ID,
				getWorkbench().getActiveWorkbenchWindow());
		page.hideView(page.findView(IIntroConstants.INTRO_VIEW_ID));
		IViewPart navigator = page.showView(IPageLayout.ID_PROJECT_EXPLORER);
		assertNotNull("failed to open project explorer", navigator);

		// for project selection
		IWorkbenchPage activePage = getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart findView = activePage.findView(IPageLayout.ID_PROJECT_EXPLORER);
		ISelectionProvider selectionProvider = findView.getSite().getSelectionProvider();
		selectionProvider.setSelection(new StructuredSelection(workspaceProjects));
		// for property dialog
		PropertyDialog createDialogOn = PropertyDialog.createDialogOn(getShell(), null,
				selectionProvider.getSelection());
		Control[] element = createDialogOn.getCurrentPage().getControl().getParent().getChildren();
		checkForCopyButtonComposite(element);
	}

	/**
	 * @param element
	 * @throws IOException
	 * @throws UnsupportedFlavorException
	 */
	private void checkForCopyButtonComposite(Control[] element) throws UnsupportedFlavorException, IOException {
		for (Control control : element) {
			if (control instanceof Composite) {
				Control[] children = ((Composite) control).getChildren();
				checkForCopyButtonComposite(children);
			} else if ((control instanceof Button) && (control.getToolTipText() != null)
					&& (control.getToolTipText().equals(IDEWorkbenchMessages.ResourceInfo_path_button_tooltip))) {
				copyButtonSelection(element, control);
				break;
			}
		}

	}

	/**
	 * @param children
	 * @param childElement
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	private void copyButtonSelection(Control[] children, Control childElement)
			throws UnsupportedFlavorException, IOException {
			// for getting location value
			Control control = children[5];
			// for button click
			childElement.notifyListeners(SWT.Selection, new Event());
			assertNotNull(Toolkit.getDefaultToolkit().getSystemClipboard()
					.getData(DataFlavor.stringFlavor));
			assertEquals(((Text) control).getText(), Toolkit.getDefaultToolkit()
					.getSystemClipboard().getData(DataFlavor.stringFlavor));
	}

	public WizardProjectsImportPage getNewWizard() {
		ImportExportWizard wizard = new ImportExportWizard(ImportExportWizard.IMPORT);
		wizard.init(getWorkbench(), null);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings.getSection("ImportExportTests");
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings.addNewSection("ImportExportTests");
		}
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);

		WizardProjectsImportPage wizardProjectsImportPage = new WizardProjectsImportPage();

		Shell shell = getShell();

		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setSize(Math.max(100, dialog.getShell().getSize().x), 100);

		Composite parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new GridLayout());
		wizardProjectsImportPage.setWizard(dialog.getCurrentPage().getWizard());
		wizardProjectsImportPage.createControl(parent);
		return wizardProjectsImportPage;
	}

	private Shell getShell() {
		return DialogCheck.getShell();
	}

}
