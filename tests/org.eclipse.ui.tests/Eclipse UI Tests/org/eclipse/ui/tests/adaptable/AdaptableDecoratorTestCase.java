/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *******************************************************************************/
package org.eclipse.ui.tests.adaptable;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @version 1.0
 */
public class AdaptableDecoratorTestCase implements ILabelProviderListener {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private DecoratorDefinition fullDefinition;

	private DecoratorDefinition lightDefinition;

	private boolean updated = false;

	public String ADAPTED_NAVIGATOR_ID = "org.eclipse.ui.tests.adaptable.adaptedHierarchy";

	protected IProject testProject;

	protected IFolder testFolder;

	protected IFile testFile;

	@Before
	public void doSetUp() throws Exception {
		createTestFile();
		showAdaptedNav();

		WorkbenchPlugin.getDefault().getDecoratorManager().addListener(this);

		DecoratorDefinition[] definitions = WorkbenchPlugin.getDefault()
				.getDecoratorManager().getAllDecoratorDefinitions();
		for (DecoratorDefinition definition : definitions) {
			if (definition.getId().equals(
					"org.eclipse.ui.tests.adaptable.decorator")) {
				fullDefinition = definition;
			}
			if (definition.getId().equals(
					"org.eclipse.ui.tests.decorators.lightweightdecorator")) {
				lightDefinition = definition;
			}
		}
	}

	private DecoratorManager getDecoratorManager() {
		return WorkbenchPlugin.getDefault().getDecoratorManager();
	}

	@After
	public void doTearDown() throws Exception {
		if (testProject != null) {
			testProject.delete(true, null);
			testProject = null;
			testFolder = null;
			testFile = null;
		}

		getDecoratorManager().removeListener(this);
	}

	/**
	 * Test enabling the contributor
	 */
	@Test
	public void testEnableDecorator() {
		getDecoratorManager().updateForEnablementChange();
		fullDefinition.setEnabled(true);
		lightDefinition.setEnabled(true);
		getDecoratorManager().updateForEnablementChange();

	}

	/**
	 * Test disabling the contributor
	 */
	@Test
	public void testDisableDecorator() {
		getDecoratorManager().updateForEnablementChange();
		fullDefinition.setEnabled(false);
		lightDefinition.setEnabled(false);
		getDecoratorManager().updateForEnablementChange();
	}

	/**
	 * Refresh the full decorator.
	 */
	@Test
	public void testRefreshFullContributor() {
		updated = false;
		getDecoratorManager().updateForEnablementChange();
		fullDefinition.setEnabled(true);
		lightDefinition.setEnabled(false);
		getDecoratorManager().updateForEnablementChange();
		assertTrue("Got an update", updated);
		updated = false;
	}

	/**
	 * Refresh the full decorator.
	 */
	@Test
	public void testRefreshLightContributor() {
		updated = false;
		getDecoratorManager().updateForEnablementChange();
		lightDefinition.setEnabled(true);
		fullDefinition.setEnabled(false);
		getDecoratorManager().updateForEnablementChange();
		assertTrue("Got an update", updated);
		updated = false;

	}

	@Override
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		updated = true;
	}

	/**
	 * Shows the Adapted Resource Navigator in a new test window.
	 */
	protected void showAdaptedNav() throws PartInitException {
		IWorkbenchWindow window = openTestWindow();
		window.getActivePage().showView(ADAPTED_NAVIGATOR_ID);
	}

	protected void createTestProject() throws CoreException {
		if (testProject == null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			testProject = workspace.getRoot().getProject("AdaptedTestProject");
			testProject.create(null);
			testProject.open(null);
		}
	}

	protected void createTestFolder() throws CoreException {
		if (testFolder == null) {
			createTestProject();
			testFolder = testProject.getFolder("AdaptedTestFolder");
			testFolder.create(false, false, null);
		}
	}

	protected void createTestFile() throws CoreException {
		if (testFile == null) {
			createTestFolder();
			testFile = testFolder.getFile("AdaptedFoo.txt");
			testFile.create(
					new ByteArrayInputStream("Some content.".getBytes()),
					false, null);
		}
	}

}
