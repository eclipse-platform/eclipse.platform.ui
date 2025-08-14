/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.tests.operations;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Stress tests the Operations Framework API to find any interaction problems
 * with the workbench.
 *
 * @since 3.1
 */
@RunWith(JUnit4.class)
public class WorkbenchOperationStressTests extends UITestCase {

	static int STRESS_TEST_REPETITIONS = 2000;

	static int OPEN_WINDOW_REPETITIONS = 2;

	public WorkbenchOperationStressTests() {
		super(WorkbenchOperationStressTests.class.getSimpleName());
	}

	/*
	 * Exercise the operations history just before closing the workbench. This
	 * can find problems with the action handlers assuming a workbench is open.
	 *
	 * In progress - this still isn't catching the case from the bug, but is a good
	 * start.
	 */
	@Test
	public void test115761() throws ExecutionException {
		for (int j = 0; j < OPEN_WINDOW_REPETITIONS; j++) {
			IWorkbenchWindow secondWorkbenchWindow = null;
			secondWorkbenchWindow = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
			Display display = secondWorkbenchWindow.getShell().getDisplay();
			IOperationHistory workbenchHistory = secondWorkbenchWindow
					.getWorkbench().getOperationSupport().getOperationHistory();
			IUndoContext workbenchContext = secondWorkbenchWindow
					.getWorkbench().getOperationSupport().getUndoContext();
			workbenchHistory.setLimit(workbenchContext, STRESS_TEST_REPETITIONS);

			for (int i = 0; i < STRESS_TEST_REPETITIONS; i++) {
				IUndoableOperation op = new TestOperation("test");
				op.addContext(workbenchContext);
				workbenchHistory.execute(op, null, null);
			}
			for (int i = 0; i < STRESS_TEST_REPETITIONS; i++) {
				if (i % 2 == 0) {
					workbenchHistory.undo(workbenchContext, null, null);
				}
				if (i % 5 == 0) {
					workbenchHistory.redo(workbenchContext, null, null);
				}
			}

			secondWorkbenchWindow.close();
			boolean go = true;
			while (go) {
				go = display.readAndDispatch();

			}
		}
	}
}
