/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.testing;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.testing.TestableEvent;
import org.eclipse.ui.testing.TestableObject;

/**
 * The Workbench's testable object facade to a test harness.
 * 
 * @since 3.0
 */
public class WorkbenchTestable extends TestableObject {
	
	private Display display;
	private IWorkbench workbench;
	
	/**
	 * Constructs a new workbench testable object.
	 */
	public WorkbenchTestable() {
	}

	/**
	 * Flushes any events from the SWT event queue.
	 */
	private void flushEventQueue() {
		while (display.readAndDispatch()) {
		}
	}

	/**
	 * Initializes the workbench testable with the display and workbench,
	 * and notifies all listeners that the tests can be run.
	 * 
	 * @param display the display
	 * @param workbench the workbench
	 */
	public void init(Display display, IWorkbench workbench) {
		Assert.isNotNull(display);
		Assert.isNotNull(workbench);
		this.display = display;
		this.workbench = workbench;
		fireTestableEvent(new TestableEvent(this, TestableEvent.CAN_RUN_TESTS));
	}
	
	/**
	 * The <code>WorkbenchTestable</code> implementation of this 
	 * <code>TestableObject</code> method ensures that the workbench
	 * has been set.
	 */
	public void testingStarting() {
		Assert.isNotNull(workbench);
	}
	
	/**
	 * The <code>WorkbenchTestable</code> implementation of this 
	 * <code>TestableObject</code> method flushes the event queue,
	 * runs the test in a <code>syncExec</code>, then flushes the
	 * event queue again.
	 */
	public void runTest(Runnable testRunnable) {
		Assert.isNotNull(workbench);
		flushEventQueue();
		display.syncExec(testRunnable);
		flushEventQueue();
	}
	
	/**
	 * The <code>WorkbenchTestable</code> implementation of this 
	 * <code>TestableObject</code> method flushes the event queue, 
	 * then closes the workbench.
	 */
	public void testingFinished() {
		flushEventQueue();
		Assert.isTrue(workbench.close());
	}
}
