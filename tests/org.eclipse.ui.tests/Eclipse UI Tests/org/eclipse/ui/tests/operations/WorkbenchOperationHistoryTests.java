/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.operations;

import org.eclipse.core.commands.operations.IOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.ObjectOperationContext;
import org.eclipse.core.commands.operations.OperationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests the Operations Framework API.
 * 
 * @since 3.1
 */
public class WorkbenchOperationHistoryTests extends UITestCase {
	OperationContext context, c1, c2;

	IOperationHistory history;

	IOperation op1, op2, op3, op4, op5, op6;

	/**
	 * @param testName
	 */
	public WorkbenchOperationHistoryTests(String name) {
		super(name);
	}

	protected void doSetUp() throws Exception {
		history = fWorkbench.getOperationSupport().getOperationHistory();
		context = fWorkbench.getOperationSupport().getOperationContext();
		c1 = new ObjectOperationContext("c1");
		c2 = new ObjectOperationContext("c2");
		op1 = new TestOperation("op1", "Test Operation 1");
		op1.addContext(context);
		op2 = new TestOperation("op2", "Test Operation 2");
		op2.addContext(context);
		op2.addContext(c1);
		op3 = new TestOperation("op3", "Test Operation 3");
		op3.addContext(c2);
		op4 = new TestOperation("op4", "Test Operation 4");
		op4.addContext(context);
		op5 = new TestOperation("op5", "Test Operation 5");
		op5.addContext(c1);
		op6 = new TestOperation("op6", "Test Operation 6");
		op6.addContext(context);
		op6.addContext(c2);
		history.execute(op1, null);
		history.execute(op2, null);
		history.execute(op3, null);
		history.execute(op4, null);
		history.execute(op5, null);
		history.execute(op6, null);

	}

	protected void doTearDown() throws Exception {
		history.dispose(null, true, true);
	}

	public void testWorkbenchOperationApproval() {
		// Enforcing of linear undo should be in effect for the workbench
		// context.
		// The first undo in c1 should be fine
		IStatus status = history.undo(c1, null);
		assertTrue(status.isOK());

		// the second undo in c1 causes a linear violation on the workbench
		// context
		assertTrue(history.canUndo(c1));
		status = history.undo(c1, null);
		assertFalse(status.isOK());

		// undo the newer context items
		status = history.undo(context, null);
		assertTrue(status.isOK());
		status = history.undo(context, null);
		assertTrue(status.isOK());

		// now we should be ok to undo c1
		status = history.undo(c1, null);
		assertTrue(status.isOK());
	}
}
