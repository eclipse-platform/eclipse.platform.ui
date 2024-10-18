/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResourceSelectionFilteringDialogTest {

	private static SeeThroughFilteredResourcesSelectionDialog createDialog() {
		SeeThroughFilteredResourcesSelectionDialog dialog = new SeeThroughFilteredResourcesSelectionDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), false,
				ResourcesPlugin.getWorkspace().getRoot(), IResource.FILE);
		dialog.setBlockOnOpen(false);
		return dialog;
	}

	private IProject project;

	@Before
	public void doSetUp() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(getClass().getSimpleName() + System.currentTimeMillis());
		project.create(null);
		project.open(null);
	}

	@Test
	public void testMatch() throws CoreException {
		File folder = new File(project.getLocation().toFile(), "a/b/c");
		folder.mkdirs();
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		IFile file = project.getFile("a/b/c/f");
		file.create(new ByteArrayInputStream(new byte[0]), true, null);
		SeeThroughFilteredResourcesSelectionDialog dialog = createDialog();
		try {
			dialog.setInitialPattern("c/f");
			dialog.open();
			dialog.refresh();
			Assert.assertTrue(DisplayHelper.waitForCondition(dialog.getShell().getDisplay(), 3000,
					() -> file.equals(dialog.getSelectedItems().getFirstElement())
			));
		} finally {
			dialog.close();
		}
	}

	@After
	public void doTearDown() throws Exception {
		project.delete(true, null);
	}

	private static class SeeThroughFilteredResourcesSelectionDialog extends FilteredResourcesSelectionDialog {

		public SeeThroughFilteredResourcesSelectionDialog(Shell shell, boolean multi, IContainer container,
				int typesMask) {
			super(shell, multi, container, typesMask);
		}

		@Override
		public StructuredSelection getSelectedItems() {
			return super.getSelectedItems();
		}
	}
}
