/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.text.quicksearch.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.text.quicksearch.internal.ui.QuickSearchDialog;
import org.eclipse.text.quicksearch.internal.ui.QuickSearchQuickAccessComputer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.Test;

public class QuickAccessComputerTest {

	private final class QuickSearchDialogExtension extends QuickSearchDialog {
		private QuickSearchDialogExtension(IWorkbenchWindow window) {
			super(window);
		}

		@Override
		public Object[] getResult() {
			computeResult();
			return super.getResult();
		}
	}

	@Test
	public void testQuickAccessComputer() throws CoreException, IOException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		project.create(null);
		project.open(null);
		IFile file = project.getFile("platform_tips.html");
		try (InputStream contents = getClass().getResourceAsStream("platform_tips.html")) {
			file.create(contents, true, null);
		}
		QuickSearchDialogExtension dialog = new QuickSearchDialogExtension(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		String request = "Eclipse full screen mode";
		dialog.setInitialPattern(request);
		dialog.setBlockOnOpen(false);
		dialog.open();
		assertTrue(DisplayHelper.waitForCondition(dialog.getShell().getDisplay(), 2000, () -> dialog.getResult().length > 0));
		dialog.close();
		assertEquals(1, new QuickSearchQuickAccessComputer().computeElements(request, new NullProgressMonitor()).length);
	}
}
