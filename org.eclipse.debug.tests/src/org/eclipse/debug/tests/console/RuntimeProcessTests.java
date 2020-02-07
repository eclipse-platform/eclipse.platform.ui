/*******************************************************************************
 * Copyright (c) 2020 Paul Pazderski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.console;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.debug.tests.AbstractDebugTest;
import org.eclipse.debug.tests.TestUtil;

public class RuntimeProcessTests extends AbstractDebugTest {

	public RuntimeProcessTests() {
		super(RuntimeProcessTests.class.getSimpleName());
	}

	public RuntimeProcessTests(String name) {
		super(name);
	}

	/**
	 * Test behavior of {@link RuntimeProcess} if the wrapped process
	 * terminates.
	 */
	public void testProcessTerminated() throws Exception {
		AtomicInteger processTerminateEvents = new AtomicInteger();
		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				for (DebugEvent event : events) {
					if (event.getKind() == DebugEvent.TERMINATE) {
						processTerminateEvents.incrementAndGet();
					}
				}
			}
		});

		MockProcess mockProcess = new MockProcess(MockProcess.RUN_FOREVER);
		RuntimeProcess runtimeProcess = mockProcess.toRuntimeProcess();

		assertFalse("RuntimeProcess already terminated.", runtimeProcess.isTerminated());
		assertTrue(runtimeProcess.canTerminate());

		mockProcess.setExitValue(1);
		mockProcess.destroy();

		TestUtil.waitWhile(p -> !p.isTerminated(), runtimeProcess, 1000, p -> "RuntimePocess not terminated.");
		TestUtil.waitForJobs(getName(), 25, 500);
		assertEquals("Wrong number of terminate events.", 1, processTerminateEvents.get());
		assertEquals("RuntimeProcess reported wrong exit code.", 1, runtimeProcess.getExitValue());
	}

	/** Test {@link RuntimeProcess} terminating the wrapped process. */
	public void testTerminateProcess() throws Exception {
		AtomicInteger processTerminateEvents = new AtomicInteger();
		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				for (DebugEvent event : events) {
					if (event.getKind() == DebugEvent.TERMINATE) {
						processTerminateEvents.incrementAndGet();
					}
				}
			}
		});

		MockProcess mockProcess = new MockProcess(MockProcess.RUN_FOREVER);
		RuntimeProcess runtimeProcess = mockProcess.toRuntimeProcess();

		assertFalse("RuntimeProcess already terminated.", runtimeProcess.isTerminated());
		assertTrue(runtimeProcess.canTerminate());

		mockProcess.setExitValue(1);
		runtimeProcess.terminate();
		assertFalse("RuntimeProcess failed to terminated wrapped process.", mockProcess.isAlive());

		TestUtil.waitWhile(p -> !p.isTerminated(), runtimeProcess, 1000, p -> "RuntimePocess not terminated.");
		TestUtil.waitForJobs(getName(), 25, 500);
		assertEquals("Wrong number of terminate events.", 1, processTerminateEvents.get());
		assertEquals("RuntimeProcess reported wrong exit code.", 1, runtimeProcess.getExitValue());
	}
}
