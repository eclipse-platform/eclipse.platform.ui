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

import junit.framework.TestCase;

import org.eclipse.core.commands.operations.ContextConsultingOperationApprover;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.LinearUndoEnforcer;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Tests the Operations Framework API.
 * 
 * @since 3.1
 */
public class OperationsAPITest extends TestCase {

	ObjectUndoContext c1, c2, c3;
	IOperationHistory history;

	IUndoableOperation op1, op2, op3, op4, op5, op6;

	public OperationsAPITest() {
		super();
	}

	/**
	 * @param testName
	 */
	public OperationsAPITest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		history = new DefaultOperationHistory();
		c1 = new ObjectUndoContext("c1");
		c2 = new ObjectUndoContext("c2");
		c3 = new ObjectUndoContext("c3");
		op1 = new TestOperation("op1", "Test Operation 1");
		op1.addContext(c1);
		op2 = new TestOperation("op2", "Test Operation 2");
		op2.addContext(c2);
		op2.addContext(c3);
		op3 = new TestOperation("op3", "Test Operation 3");
		op3.addContext(c3);
		op4 = new TestOperation("op4", "Test Operation 4");
		op4.addContext(c1);
		op5 = new TestOperation("op5", "Test Operation 5");
		op5.addContext(c2);
		op6 = new TestOperation("op6", "Test Operation 6");
		op6.addContext(c3);
		op6.addContext(c1);
		history.execute(op1, null);
		history.execute(op2, null);
		history.execute(op3, null);
		history.execute(op4, null);
		history.execute(op5, null);
		history.execute(op6, null);

	}

	protected void tearDown() throws Exception {
		super.tearDown();
		history.dispose(null, true, true);
	}

	public void testContextDispose() {
		assertSame(history.getUndoOperation(c1), op6);
		assertSame(history.getUndoOperation(c3), op6);
		history.dispose(c1, true, true);
		assertSame(history.getUndoOperation(c3), op6);
		assertFalse(op6.hasContext(c1));
		history.undo(c3, null);
		history.dispose(c3, true, false);
		assertFalse(history.canUndo(c3));
		assertTrue(history.canRedo(c3));
		history.redo(c3, null);
		IUndoableOperation[] ops = history.getUndoHistory(null);
		assertEquals(ops.length, 3);
		ops = history.getUndoHistory(c3);
		assertEquals(ops.length, 1);
		ops = history.getUndoHistory(c2);
		assertEquals(ops.length, 2);
	}

	public void testContextHistories() {
		assertSame(history.getUndoOperation(c1), op6);
		assertSame(history.getUndoOperation(c2), op5);
		assertSame(history.getUndoOperation(c3), op6);
		IStatus status = history.undo(c3, null);
		assertTrue("Status should be ok", status.isOK());
		assertSame(history.getRedoOperation(c3), op6);
		assertSame(history.getUndoOperation(c3), op3);
		assertTrue("Should be able to redo in c3", history.canRedo(c3));
		assertTrue("Should be able to redo in c1", history.canRedo(c1));
		history.redo(c1, null);
		assertSame(history.getUndoOperation(c3), op6);
		assertSame(history.getUndoOperation(c1), op6);
	}

	public void testHistoryLimit() {
		history.setLimit(c1, 2);
		assertTrue(history.getUndoHistory(c1).length == 2);
		history.add(op1);
		assertTrue(history.getUndoHistory(c1).length == 2);
		history.setLimit(c2, 1);
		assertTrue(history.getUndoHistory(c2).length == 1);
		assertFalse(op2.hasContext(c2));
		history.undo(c2, null);
		assertTrue(history.getRedoHistory(c2).length == 1);
		assertTrue(history.getUndoHistory(c2).length == 0);
		history.redo(c2, null);
		assertTrue(history.getRedoHistory(c2).length == 0);
		assertTrue(history.getUndoHistory(c2).length == 1);
	}
	
	public void testLocalHistoryLimits() {
		history.setLimit(c3, 2);
		assertTrue(history.getUndoHistory(c3).length == 2);
		// op2 should have context c3 removed as part of forcing the limit
		assertFalse(op2.hasContext(c3));
		assertTrue(history.getUndoHistory(c2).length == 2);
		history.setLimit(c2, 1);
		assertTrue(history.getUndoHistory(c2).length == 1);
		history.undo(c2, null);
		op2.addContext(c3);
		history.add(op2);
		assertSame(history.getUndoOperation(c2), op2);
		assertTrue(history.getUndoHistory(c2).length == 1);
	}

	public void testOperationApproval() {
		history.addOperationApprover(new ContextConsultingOperationApprover());
		c2.setOperationApprover(new LinearUndoEnforcer());
		c3.setOperationApprover(new LinearUndoEnforcer());
		// the first undo should be fine
		IStatus status = history.undo(c2, null);
		assertTrue(status.isOK());

		// the second causes a linear violation on c3
		assertTrue(history.canUndo(c2));
		status = history.undo(c2, null);
		assertFalse(status.isOK());

		// undo the newer c3 items
		status = history.undo(c3, null);
		assertTrue(status.isOK());
		status = history.undo(c3, null);
		assertTrue(status.isOK());

		// now we should be okay in c2
		status = history.undo(c2, null);
		assertTrue(status.isOK());

		history.addOperationApprover(new IOperationApprover() {

			public IStatus proceedRedoing(IUndoableOperation o, IOperationHistory h) {
				return Status.CANCEL_STATUS;
			}
			public IStatus proceedUndoing(IUndoableOperation o, IOperationHistory h) {
				return Status.CANCEL_STATUS;
			}
		});
		// everything should fail now
		assertFalse(history.redo(c2, null).isOK());
		assertFalse(history.redo(c3, null).isOK());
		assertFalse(history.undo(c1, null).isOK());
		assertFalse(history.undo(c2, null).isOK());
		assertFalse(history.undo(c3, null).isOK());
	}

	public void testOperationFailure() {
		history.addOperationApprover(new IOperationApprover() {

			public IStatus proceedRedoing(IUndoableOperation o, IOperationHistory h) {
				return Status.OK_STATUS;
			}
			public IStatus proceedUndoing(IUndoableOperation o, IOperationHistory h) {
				if (o == op6)
					return Status.CANCEL_STATUS;
				if (o == op5)
					return new OperationStatus(0, "Error", null);
				return Status.OK_STATUS;
			}
		});

		// should fail but still keep op6 on the stack since it's cancelled
		IStatus status = history.undo(c3, null);
		assertFalse(status.isOK());
		assertSame(history.getUndoOperation(c3), op6);

		// should fail and remove op5 since it's an error
		status = history.undo(c2, null);
		assertFalse(status.isOK());
		assertNotSame("should not match", history.getUndoOperation(c2), op5);

		// should succeed
		status = history.undo(c2, null);
		assertTrue(status.isOK());
	}

	public void testOperationRedo() {
		history.undo(c2, null);
		history.undo(c2, null);
		history.undo(c3, null);
		history.undo(c3, null);
		assertSame(history.getRedoOperation(c2), op2);
		assertSame(history.getUndoOperation(c1), op4);
		assertTrue(history.canUndo(c1));
		assertFalse(history.canUndo(c2));
		assertFalse(history.canUndo(c3));
		history.redo(c2, null);
		assertTrue(history.canUndo(c2));
		assertTrue(history.canUndo(c3));
	}

	public void testOperationRemoval() {
		assertSame(history.getUndoOperation(c1), op6);
		history.remove(op6);
		assertSame(history.getUndoOperation(c1), op4);
		assertSame(history.getUndoOperation(c3), op3);
		history.remove(op4);
		assertSame(history.getUndoOperation(c1), op1);
		history.undo(c2, null);
		assertTrue(history.canRedo(c2));
		history.remove(op5);
		assertFalse(history.canRedo(c2));
	}

	public void testOperationUndo() {
		history.undo(c1, null);
		history.undo(c1, null);
		assertSame(history.getRedoOperation(c1), op4);
		assertSame(history.getUndoOperation(c1), op1);
		history.undo(c1, null);
		assertFalse("Shouldn't be able to undo in c1", history.canUndo(c1));
		assertTrue("Should be able to undo in c2", history.canUndo(c2));
		assertTrue("Should be able to undo in c3", history.canUndo(c3));
	}
	
	public void testHistoryFactory() {
		IOperationHistory anotherHistory = OperationHistoryFactory.getOperationHistory();
		assertNotNull(anotherHistory);
	}
}
