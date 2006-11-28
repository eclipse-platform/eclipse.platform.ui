/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.compare.internal.WorkQueue;
import org.eclipse.compare.internal.Worker;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class AsyncExecTests extends TestCase {

	public AsyncExecTests() {
		super();
	}

	public AsyncExecTests(String name) {
		super(name);
	}
	
	public void testQueueAdd() {
		WorkQueue q = new WorkQueue();
		assertTrue(q.isEmpty());
		IRunnableWithProgress r = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				// Nothing to do for now
			}
		};
		IRunnableWithProgress r2 = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				// Nothing to do for now
			}
		};
		// Ensure that adding an element adds it
		q.add(r);
		assertTrue(q.size() == 1);
		assertTrue(q.contains(r));
		assertTrue(q.remove() == r);
		assertTrue(q.isEmpty());
		// Ensure that adding an element again replaces it
		q.add(r);
		q.add(r);
		assertTrue(q.size() == 1);
		assertTrue(q.contains(r));
		// Ensure remove order matches add order
		q.add(r2);
		assertTrue(q.size() == 2);
		assertTrue(q.contains(r));
		assertTrue(q.contains(r2));
		assertTrue(q.remove() == r);
		assertTrue(q.size() == 1);
		assertTrue(q.remove() == r2);
		assertTrue(q.isEmpty());
		// Ensure remove order adjusted when same element added
		q.add(r);
		q.add(r2);
		q.add(r);
		assertTrue(q.size() == 2);
		assertTrue(q.contains(r));
		assertTrue(q.contains(r2));
		assertTrue(q.remove() == r2);
		assertTrue(q.size() == 1);
		assertTrue(q.remove() == r);
		assertTrue(q.isEmpty());
	}
	
	public void testWorker() throws InvocationTargetException, InterruptedException {
		final Worker w = new Worker("");
		final List worked = new ArrayList();
		IRunnableWithProgress r = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				assertTrue(w.isWorking());
				assertTrue(w.hasWork());
				worked.add(this);
			}
		};
		IRunnableWithProgress r2 = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				assertTrue(w.isWorking());
				assertTrue(w.hasWork());
				worked.add(this);
			}
		};
		// Test one task
		w.add(r);
		assertTrue(w.hasWork());
		assertFalse(w.isWorking());
		assertTrue(worked.isEmpty());
		w.run(new NullProgressMonitor());
		assertFalse(w.hasWork());
		assertFalse(w.isWorking());
		assertTrue(worked.size() == 1);
		assertTrue(worked.get(0) == r);
		// Test two tasks
		worked.clear();
		w.add(r);
		assertTrue(w.hasWork());
		assertFalse(w.isWorking());
		w.add(r2);
		assertTrue(w.hasWork());
		assertFalse(w.isWorking());
		w.run(new NullProgressMonitor());
		assertFalse(w.hasWork());
		assertFalse(w.isWorking());
		assertTrue(worked.size() == 2);
		assertTrue(worked.get(0) == r);
		assertTrue(worked.get(1) == r2);
		// Test re-add order
		worked.clear();
		w.add(r);
		assertTrue(w.hasWork());
		assertFalse(w.isWorking());
		w.add(r2);
		assertTrue(w.hasWork());
		assertFalse(w.isWorking());
		w.add(r);
		assertTrue(w.hasWork());
		assertFalse(w.isWorking());
		w.run(new NullProgressMonitor());
		assertFalse(w.hasWork());
		assertFalse(w.isWorking());
		assertTrue(worked.size() == 2);
		assertTrue(worked.get(1) == r);
		assertTrue(worked.get(0) == r2);
	}
	
	public void testCancelOnRequeue() throws InvocationTargetException, InterruptedException {
		final Worker w = new Worker("");
		final List worked = new ArrayList();
		IRunnableWithProgress r = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				if (worked.isEmpty()) {
					worked.add(this);
					w.add(this);
					assertTrue(monitor.isCanceled());
					throw new OperationCanceledException();
				}
				assertTrue(w.isWorking());
				assertTrue(w.hasWork());
				worked.add(this);
			}
		};
		IRunnableWithProgress r2 = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				assertTrue(w.isWorking());
				assertTrue(w.hasWork());
				worked.add(this);
			}
		};
		worked.clear();
		w.add(r);
		assertTrue(w.hasWork());
		assertFalse(w.isWorking());
		w.add(r2);
		assertTrue(w.hasWork());
		assertFalse(w.isWorking());
		w.run(new NullProgressMonitor());
		assertFalse(w.hasWork());
		assertFalse(w.isWorking());
		assertTrue(worked.size() == 3);
		assertTrue(worked.get(0) == r);
		assertTrue(worked.get(1) == r2);
		assertTrue(worked.get(2) == r);
	}

}
