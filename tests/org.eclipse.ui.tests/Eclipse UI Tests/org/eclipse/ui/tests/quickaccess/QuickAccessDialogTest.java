/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.quickaccess;

import org.eclipse.core.commands.Command;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.quickaccess.QuickAccessDialog;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.4
 * 
 */
public class QuickAccessDialogTest extends UITestCase {

	/**
	 * @since 3.4
	 * 
	 */
	private static class TestQuickAccessDialog extends QuickAccessDialog {
		private TestQuickAccessDialog(IWorkbenchWindow window,
				Command invokingCommand) {
			super(window, invokingCommand);
		}

		Table getTable() {
			return table;
		}

		Text getFilterText() {
			return filterText;
		}
		protected void toggleShowAllMatches() {
			super.toggleShowAllMatches();
		}
	}

	/**
	 * @param testName
	 */
	public QuickAccessDialogTest(String testName) {
		super(testName);
	}

	public void testOpenQuickAccess() {
		final TestQuickAccessDialog dialog = new TestQuickAccessDialog(
				getWorkbench().getActiveWorkbenchWindow(), null);
		dialog.setBlockOnOpen(false);
		dialog.open();
		assertTrue("expecting items", processEventsUntil(new Condition() {
			public boolean compute() {
				return dialog.getTable().getItemCount() > 0;
			};
		}, 200));
		String oldFirstItemText = dialog.getTable().getItem(0).getText(1);
		dialog.getFilterText().setText("e");
		int count1 = dialog.getTable().getItemCount();
		assertTrue("expecting matching items",
				count1 > 0);
		assertNotSame("expecting different item", oldFirstItemText, dialog
				.getTable().getItem(0).getText(1));
		dialog.toggleShowAllMatches();
		int count2 = dialog.getTable().getItemCount();
		assertTrue("still expecting matching items",
				count2 > 0);
		assertTrue("expecting more matching items", count2> count1);
	}

}
