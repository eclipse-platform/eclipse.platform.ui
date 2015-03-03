/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.operations;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests the Operations Framework API.
 *
 * @since 3.1
 */
public class WorkbenchOperationHistoryTests extends UITestCase {
	IUndoContext context, contextA, contextB;

	IOperationHistory history;

	IUndoableOperation op1, op2, op3, op4, op5, op6;

	/**
	 * @param testName
	 */
	public WorkbenchOperationHistoryTests(String name) {
		super(name);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		history = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
		context = PlatformUI.getWorkbench().getOperationSupport().getUndoContext();
		contextA = new ObjectUndoContext("A");
		contextB = new ObjectUndoContext("B");
		op1 = new TestOperation("op1");
		op1.addContext(context);
		op2 = new TestOperation("op2");
		op2.addContext(context);
		op2.addContext(contextA);
		op3 = new TestOperation("op3");
		op3.addContext(contextB);
		op4 = new TestOperation("op4");
		op4.addContext(context);
		op5 = new TestOperation("op5");
		op5.addContext(contextA);
		op6 = new TestOperation("op6");
		op6.addContext(context);
		op6.addContext(contextB);
		history.execute(op1, null, null);
		history.execute(op2, null, null);
		history.execute(op3, null, null);
		history.execute(op4, null, null);
		history.execute(op5, null, null);
		history.execute(op6, null, null);

	}

	@Override
	protected void doTearDown() throws Exception {
		history.dispose(IOperationHistory.GLOBAL_UNDO_CONTEXT, true, true, true);
		super.doTearDown();
	}

	public void testWorkspaceAdapter() {
		IUndoContext workspaceContext = ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
		assertTrue("Should have context registered on workspace", workspaceContext == context);
	}

	public void testMatchingContext() {
		IUndoContext newContext = new IUndoContext() {
			@Override
			public String getLabel() { return "Matching Test Context"; }
			@Override
			public boolean matches(IUndoContext otherContext) { return false; }
		};
		assertFalse(newContext.matches(context));
		((ObjectUndoContext)context).addMatch(newContext);
		assertTrue(history.getUndoHistory(context).length == history.getUndoHistory(newContext).length);
		assertTrue(op1.hasContext(newContext));
		assertFalse(op3.hasContext(context));
		op3.addContext(newContext);
		assertTrue(op3.hasContext(context));
	}
}
