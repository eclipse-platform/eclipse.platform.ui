/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.tests.datatransfer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.wizards.datatransfer.SmartImportWizard;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.12
 *
 */
public class SmartImportTests extends UITestCase {

	private WizardDialog dialog;

	/**
	 * @param testName
	 */
	public SmartImportTests(String testName) {
		super(testName);
	}

	@Override
	public void doSetUp() throws Exception {
		super.doSetUp();
		clearAll();
	}

	@Override
	public void doTearDown() throws Exception {
		super.doTearDown();
		clearAll();
	}

	private void clearAll() throws CoreException {
		if (dialog != null && !dialog.getShell().isDisposed()) {
			dialog.close();
		}
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			project.delete(false, false, new NullProgressMonitor());
		}
	}

	public void runSmartImport(File source) throws OperationCanceledException, InterruptedException {
		SmartImportWizard wizard = new SmartImportWizard();
		wizard.setInitialImportSource(source);
		this.dialog = new WizardDialog(getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.setBlockOnOpen(false);
		dialog.open();
		final Button okButton = getFinishButton(dialog.buttonBar);
		processEventsUntil(new Condition() {
			@Override
			public boolean compute() {
				return okButton.isEnabled();
			}
		}, -1);
		wizard.performFinish();
		wizard.getImportJob().join();
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

	public void testImport6Projects() throws IOException, OperationCanceledException, InterruptedException {
		URL url = FileLocator
				.toFileURL(getClass().getResource("/data/org.eclipse.datatransferArchives/ProjectsArchive.zip"));
		File file = new File(url.getFile());
		runSmartImport(file);
		assertEquals(6, ResourcesPlugin.getWorkspace().getRoot().getProjects().length);
	}

	public void testImportModularProjectsWithSameName()
			throws IOException, OperationCanceledException, InterruptedException {
		URL url = FileLocator
				.toFileURL(getClass().getResource("/data/org.eclipse.datatransferArchives/project"));
		File file = new File(url.getFile());
		runSmartImport(file);
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
}
