/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.operations;

import junit.framework.TestCase;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.ContextConsultingOperationApprover;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.LinearUndoEnforcer;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.runtime.IAdaptable;
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
	
	int preExec, postExec, preUndo, postUndo, preRedo, postRedo, add, remove, notOK, changed = 0;
	IOperationHistoryListener listener;

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
		history.execute(op1, null, null);
		history.execute(op2, null, null);
		history.execute(op3, null, null);
		history.execute(op4, null, null);
		history.execute(op5, null, null);
		history.execute(op6, null, null);
		preExec = 0; postExec = 0;
		preUndo = 0; postUndo = 0; 
		preRedo = 0; postRedo = 0;
		add = 0; remove = 0; notOK = 0;
		listener = new IOperationHistoryListener() {
			public void historyNotification(OperationHistoryEvent event) {
				switch (event.getEventType()) {
				case OperationHistoryEvent.ABOUT_TO_EXECUTE:
					preExec++;
					break;
				case OperationHistoryEvent.ABOUT_TO_UNDO:
					preUndo++;
					break;
				case OperationHistoryEvent.ABOUT_TO_REDO:
					preRedo++;
					break;
				case OperationHistoryEvent.DONE:
					postExec++;
					break;
				case OperationHistoryEvent.UNDONE:
					postUndo++;
					break;
				case OperationHistoryEvent.REDONE:
					postRedo++;
					break;
				case OperationHistoryEvent.OPERATION_ADDED:
					add++;
					break;
				case OperationHistoryEvent.OPERATION_REMOVED:
					remove++;
					break;
				case OperationHistoryEvent.OPERATION_NOT_OK:
					notOK++;
					break;
				case OperationHistoryEvent.OPERATION_CHANGED:
					changed++;
					break;
				}
			}
		};
		history.addOperationHistoryListener(listener);

	}

	protected void tearDown() throws Exception {
		super.tearDown();
		history.removeOperationHistoryListener(listener);
		history.dispose(IOperationHistory.GLOBAL_UNDO_CONTEXT, true, true);
	}

	public void testContextDispose() throws ExecutionException {
		assertSame(history.getUndoOperation(c1), op6);
		assertSame(history.getUndoOperation(c3), op6);
		history.dispose(c1, true, true);
		assertSame(history.getUndoOperation(c3), op6);
		assertFalse(op6.hasContext(c1));
		history.undo(c3, null, null);
		history.dispose(c3, true, false);
		assertFalse(history.canUndo(c3));
		assertTrue(history.canRedo(c3));
		history.redo(c3, null, null);
		IUndoableOperation[] ops = history.getUndoHistory(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertEquals(ops.length, 3);
		ops = history.getUndoHistory(c3);
		assertEquals(ops.length, 1);
		ops = history.getUndoHistory(c2);
		assertEquals(ops.length, 2);
	}

	public void testContextHistories() throws ExecutionException {
		assertSame(history.getUndoOperation(c1), op6);
		assertSame(history.getUndoOperation(c2), op5);
		assertSame(history.getUndoOperation(c3), op6);
		IStatus status = history.undo(c3, null, null);
		assertTrue("Status should be ok", status.isOK());
		assertSame(history.getRedoOperation(c3), op6);
		assertSame(history.getUndoOperation(c3), op3);
		assertTrue("Should be able to redo in c3", history.canRedo(c3));
		assertTrue("Should be able to redo in c1", history.canRedo(c1));
		history.redo(c1, null, null);
		assertSame(history.getUndoOperation(c3), op6);
		assertSame(history.getUndoOperation(c1), op6);
	}

	public void testHistoryLimit() throws ExecutionException {
		history.setLimit(c1, 2);
		assertTrue(history.getUndoHistory(c1).length == 2);
		history.add(op1);
		assertTrue(history.getUndoHistory(c1).length == 2);
		history.setLimit(c2, 1);
		assertTrue(history.getUndoHistory(c2).length == 1);
		assertFalse(op2.hasContext(c2));
		history.undo(c2, null, null);
		assertTrue(history.getRedoHistory(c2).length == 1);
		assertTrue(history.getUndoHistory(c2).length == 0);
		history.redo(c2, null, null);
		assertTrue(history.getRedoHistory(c2).length == 0);
		assertTrue(history.getUndoHistory(c2).length == 1);
	}
	
	public void testLocalHistoryLimits() throws ExecutionException {
		history.setLimit(c3, 2);
		assertTrue(history.getUndoHistory(c3).length == 2);
		// op2 should have context c3 removed as part of forcing the limit
		assertFalse(op2.hasContext(c3));
		assertTrue(history.getUndoHistory(c2).length == 2);
		history.setLimit(c2, 1);
		assertTrue(history.getUndoHistory(c2).length == 1);
		history.undo(c2, null, null);
		op2.addContext(c3);
		history.add(op2);
		assertSame(history.getUndoOperation(c2), op2);
		assertTrue(history.getUndoHistory(c2).length == 1);
	}
	
	public void testOpenOperation() throws ExecutionException {
		// clear out history which will also reset operation execution counts
		history.dispose(IOperationHistory.GLOBAL_UNDO_CONTEXT, true, true);
		history.openOperation(op1);
		op1.execute(null, null);
		op2.execute(null, null);
		history.add(op2);
		history.execute(op3, null, null);
		IUndoableOperation op = history.getUndoOperation(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertTrue("no operations should be in history yet", op == null);
		history.closeOperation(true, true);
		op = history.getUndoOperation(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertTrue("Operation should be batching", op == op1);
		op.removeContext(c2);
		assertFalse("Operation should not have context", op.hasContext(c2));
		assertTrue("Removal of open operation's context should not affect original operation", op2.hasContext(c2));
	}
	
	public void testUnsuccessfulOpenOperation() throws ExecutionException {
		// clear out history which will also reset operation execution counts
		history.dispose(IOperationHistory.GLOBAL_UNDO_CONTEXT, true, true);
		history.openOperation(op1);
		op1.execute(null, null);
		op2.execute(null, null);
		history.add(op2);
		history.execute(op3, null, null);
		IUndoableOperation op = history.getUndoOperation(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertTrue("no operations should be in history yet", op == null);
		history.closeOperation(false, true);
		op = history.getUndoOperation(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertNull("Unsuccessful operation should not be added to history", op);
		assertTrue("NOT_OK notification should have been received", notOK == 1);
		assertTrue("DONE should not be sent while batching", postExec == 0);
		assertTrue("ADDED should not have been sent while batching", add == 0);
	}
	
	public void testNotAddedOpenOperation() throws ExecutionException {
		// clear out history which will also reset operation execution counts
		history.dispose(IOperationHistory.GLOBAL_UNDO_CONTEXT, true, true);
		history.openOperation(op1);
		op1.execute(null, null);
		op2.execute(null, null);
		history.add(op2);
		history.execute(op3, null, null);
		IUndoableOperation op = history.getUndoOperation(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertTrue("no operations should be in history yet", op == null);
		history.closeOperation(true, false);
		op = history.getUndoOperation(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertNull("Operation should not be added to history", op);
		assertTrue("DONE notification should have been received", postExec == 1);
		assertTrue("ADDED should not have occurred or be sent while batching", add == 0);
	}
	
	public void testMultipleOpenOperation() throws ExecutionException {
		// clear out history which will also reset operation execution counts
		history.dispose(IOperationHistory.GLOBAL_UNDO_CONTEXT, true, true);
		history.openOperation(op1);
		op1.execute(null, null);
		op2.execute(null, null);
		history.add(op2);
		history.execute(op3, null, null);
		history.openOperation(op4);
		IUndoableOperation op = history.getUndoOperation(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertNull("Unexpected nested open should not add original", op);
		history.closeOperation(true, true);
		op = history.getUndoOperation(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertSame("Second operation should be closed", op, op4);
	}
	
	public void testAbortedOpenOperation() throws ExecutionException {
		history.dispose(IOperationHistory.GLOBAL_UNDO_CONTEXT, true, true);
		history.openOperation(op1);
		op1.execute(null, null);
		history.execute(op2, null, null);
		// flush history while operation is open
		history.dispose(IOperationHistory.GLOBAL_UNDO_CONTEXT, true, true);
		// op3 should be added as its own op since we flushed while open
		history.add(op3);
		// should really have no effect
		history.closeOperation(true, true);
		IUndoableOperation op = history.getUndoOperation(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		assertTrue("Open operation should be flushed", op == op3);
	}

	public void testOperationApproval() throws ExecutionException {
		history.addOperationApprover(new ContextConsultingOperationApprover());
		c2.setOperationApprover(new LinearUndoEnforcer());
		c3.setOperationApprover(new LinearUndoEnforcer());
		// the first undo should be fine
		IStatus status = history.undo(c2, null, null);
		assertTrue(status.isOK());

		// the second causes a linear violation on c3
		assertTrue(history.canUndo(c2));
		status = history.undo(c2, null, null);
		assertFalse(status.isOK());

		// undo the newer c3 items
		status = history.undo(c3, null, null);
		assertTrue(status.isOK());
		status = history.undo(c3, null, null);
		assertTrue(status.isOK());

		// now we should be okay in c2
		status = history.undo(c2, null, null);
		assertTrue(status.isOK());

		history.addOperationApprover(new IOperationApprover() {

			public IStatus proceedRedoing(IUndoableOperation o, IOperationHistory h, IAdaptable a) {
				return Status.CANCEL_STATUS;
			}
			public IStatus proceedUndoing(IUndoableOperation o, IOperationHistory h, IAdaptable a) {
				return Status.CANCEL_STATUS;
			}
		});
		// everything should fail now
		assertFalse(history.redo(c2, null, null).isOK());
		assertFalse(history.redo(c3, null, null).isOK());
		assertFalse(history.undo(c1, null, null).isOK());
		assertFalse(history.undo(c2, null, null).isOK());
		assertFalse(history.undo(c3, null, null).isOK());
	}

	public void testOperationFailure() throws ExecutionException {
		history.addOperationApprover(new IOperationApprover() {

			public IStatus proceedRedoing(IUndoableOperation o, IOperationHistory h, IAdaptable a) {
				return Status.OK_STATUS;
			}
			public IStatus proceedUndoing(IUndoableOperation o, IOperationHistory h, IAdaptable a) {
				if (o == op6)
					return Status.CANCEL_STATUS;
				if (o == op5)
					return new OperationStatus(0, "Error", null);
				return Status.OK_STATUS;
			}
		});

		// should fail but still keep op6 on the stack since it's cancelled
		IStatus status = history.undo(c3, null, null);
		assertFalse(status.isOK());
		assertSame(history.getUndoOperation(c3), op6);

		// should fail and remove op5 since it's an error
		status = history.undo(c2, null, null);
		assertFalse(status.isOK());
		assertNotSame("should not match", history.getUndoOperation(c2), op5);

		// should succeed
		status = history.undo(c2, null, null);
		assertTrue(status.isOK());
	}

	public void testOperationRedo() throws ExecutionException {
		history.undo(c2, null, null);
		history.undo(c2, null, null);
		history.undo(c3, null, null);
		history.undo(c3, null, null);
		assertSame(history.getRedoOperation(c2), op2);
		assertSame(history.getUndoOperation(c1), op4);
		assertTrue(history.canUndo(c1));
		assertFalse(history.canUndo(c2));
		assertFalse(history.canUndo(c3));
		assertTrue(preUndo == 4);
		assertTrue(postUndo == 4);
		history.redo(c2, null, null);
		assertTrue(postRedo == 1);
		assertTrue(history.canUndo(c2));
		assertTrue(history.canUndo(c3));
	}

	public void testOperationRemoval() throws ExecutionException {
		assertSame(history.getUndoOperation(c1), op6);
		history.remove(op6);
		assertSame(history.getUndoOperation(c1), op4);
		assertSame(history.getUndoOperation(c3), op3);
		history.remove(op4);
		assertSame(history.getUndoOperation(c1), op1);
		history.undo(c2, null, null);
		assertTrue(history.canRedo(c2));
		history.remove(op5);
		assertFalse(history.canRedo(c2));
	}

	public void testOperationUndo() throws ExecutionException {
		history.undo(c1, null, null);
		history.undo(c1, null, null);
		assertSame(history.getRedoOperation(c1), op4);
		assertSame(history.getUndoOperation(c1), op1);
		history.undo(c1, null, null);
		assertTrue(preUndo == 3);
		assertTrue(postUndo == 3);
		assertFalse("Shouldn't be able to undo in c1", history.canUndo(c1));
		assertTrue("Should be able to undo in c2", history.canUndo(c2));
		assertTrue("Should be able to undo in c3", history.canUndo(c3));
	}
	
	public void testHistoryFactory() {
		IOperationHistory anotherHistory = OperationHistoryFactory.getOperationHistory();
		assertNotNull(anotherHistory);
	}
	
	public void testOperationChanged() {
		history.remove(op1);
		history.operationChanged(op1);
		history.operationChanged(op2);
		assertTrue("should not notify about changes if not in the history", changed == 1);
	}
}
