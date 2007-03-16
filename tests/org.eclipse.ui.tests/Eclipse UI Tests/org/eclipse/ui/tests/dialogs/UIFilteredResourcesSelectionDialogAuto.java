/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.dialogs;

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

public class UIFilteredResourcesSelectionDialogAuto extends
		UIAbstractFilteredResourcesSelectionDialog {

	/**
	 * Checks if content provider displays expected resources from the history.
	 * 
	 * @throws Exception
	 */
	public void testReadHistory() throws Exception {

		final MockedFilteredResourcesSelectionDialog dialog = createDialog();

		dialog.open();

		Object[] items = dialog.getHistoryItems();

		assertEquals(historyResources.length, items.length);
		Set itemsSet = new HashSet(historyResources.length);
		itemsSet.addAll(Arrays.asList(items));
		for (int i = 0; i < historyResources.length; i++) {
			assertEquals(true, itemsSet.contains(historyResources[i]));
		}

		closeDialog(dialog);
	}
	
	private static MockedFilteredResourcesSelectionDialog createDialog() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		final Shell parent = window.getShell();
		final IContainer input = ResourcesPlugin.getWorkspace().getRoot();
		final MockedFilteredResourcesSelectionDialog dialog = new MockedFilteredResourcesSelectionDialog(
				parent, true, input, IResource.FILE);

		if (dialog.getShell() == null) {
			dialog.create();
		}
		Point testDialogSize = dialog.getShell().getSize();
		dialog.setBlockOnOpen(false);
		dialog.getShell().setLocation(window.getShell().getSize().x + 1, 0);
		dialog.getShell().setSize(testDialogSize);
		dialog.getShell().addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				e.doit = false;
			}
		});
		return dialog;
	}

	private static void closeDialog(
			MockedFilteredResourcesSelectionDialog dialog) {
		if (dialog.getShell() != null) {
			dialog.getShell().dispose();
			dialog.close();
		}
	}

	private static class MockedFilteredResourcesSelectionDialog extends
			FilteredResourcesSelectionDialog {

		public MockedFilteredResourcesSelectionDialog(Shell shell,
				boolean multi, IContainer container, int typesMask) {
			super(shell, multi, container, typesMask);
		}

		// override protected modifiers ...

		public Object[] getHistoryItems() {
			return getSelectionHistory().getHistoryItems();
		}

	}

}
