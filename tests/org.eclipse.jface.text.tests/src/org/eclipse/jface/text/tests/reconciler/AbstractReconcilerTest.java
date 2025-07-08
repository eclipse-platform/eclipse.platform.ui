/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.jface.text.tests.reconciler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.AbstractReconciler;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.tests.TestTextViewer;


/**
 * Reconciler tests. Uses barrier synchronization and a call log to assert
 * correct order of reconciling events.
 *
 * @since 3.1
 */
// TODO test reconciler arguments (delay > 0 etc.)
// TODO incremental reconciler tests
public class AbstractReconcilerTest {

	/**
	 * Modified barrier: there are two threads: the main (testing) thread
	 * creating the barrier, and the reconciler thread. When both threads have
	 * met at the barrier, the main thread is released and can perform
	 * assertions while being sure that the reconciler is dormant. After the
	 * tests have been performed, the main thread must call <code>wakeAll</code>
	 * to release the reconciler thread.
	 */
	static class Barrier {
		private final Object fMutex= new Object();
		private final int fParticipants;
		private final Thread fMainThread;

		private int fWaiting= 0;
		private boolean fMainThreadArrived= false;
		private boolean fIsInactive= false;

		Barrier() {
			fParticipants= 2;
			fMainThread= Thread.currentThread();
		}

		public void await() {
			synchronized (fMutex) {
				if (fIsInactive)
					return;

				fWaiting++;

				boolean isMainThread= Thread.currentThread() == fMainThread;
				if (isMainThread)
					fMainThreadArrived= true;

				if (allArrived()) {
					if (!fMainThreadArrived) {
						fWaiting--;
						throw new RuntimeException(getClass() + " can't join barrier if only the main thread is missing!");
					}

					if (!isMainThread)
						notifyMainThread();
				}
				if (!allArrived() || !isMainThread) {
					try {
						if (!isMainThread)
							fMutex.wait();
						else {
							fMutex.wait(5000); // don't wait forever for bad reconcilers
							if (!allArrived())
								fail("reconciler never ran in 5 seconds");
						}
					} catch (InterruptedException e) {
						// threads must not be interrupted
						throw new Error();
					}
				}
			}
		}

		private boolean allArrived() {
			return fWaiting == fParticipants;
		}

		private void notifyMainThread() {
			fMutex.notify();
		}

		public void wakeAll() {
			synchronized (fMutex) {
				fWaiting= 0;
				fMainThreadArrived= false;
				fMutex.notifyAll();
			}
		}

		public void shutdown() {
			synchronized (fMutex) {
				fIsInactive= true;
				fMutex.notifyAll();
			}
		}
	}

	private Accessor fAccessor;
	private Barrier fBarrier;
	private List<String> fCallLog;
	private ITextViewer fViewer;
	protected AbstractReconciler fReconciler;
	private Document fDocument;

	private IProgressMonitor fProgressMonitor;


	@Before
	public void setUp() {
		fBarrier= new Barrier();
		fCallLog= Collections.synchronizedList(new ArrayList<>());
		fReconciler= new AbstractReconciler() {
					@Override
					protected void initialProcess() {
						fCallLog.add("initialProcess");
						fBarrier.await();
					}
					@Override
					protected void process(DirtyRegion dirtyRegion) {
						fCallLog.add("process");
						fBarrier.await();
					}
					@Override
					protected void reconcilerDocumentChanged(IDocument newDocument) {
						fCallLog.add("reconcilerDocumentChanged");
					}
					@Override
					protected void aboutToWork() {
				AbstractReconcilerTest.this.aboutToWork(this);
					}
					@Override
					protected void aboutToBeReconciled() {
						fCallLog.add("aboutToBeReconciled");
					}
					@Override
					protected void reconcilerReset() {
						fCallLog.add("reconcilerReset");
					}
					@Override
					public IReconcilingStrategy getReconcilingStrategy(String contentType) {
						return null;
					}
				};
		fReconciler.setIsIncrementalReconciler(false);
		fReconciler.setDelay(getDelay());

		fProgressMonitor= new NullProgressMonitor();
		fReconciler.setProgressMonitor(fProgressMonitor);

		fViewer= new TestTextViewer();
		fReconciler.install(fViewer);

		fAccessor= new Accessor(fReconciler, AbstractReconciler.class);
		Object object= fAccessor.get("fWorker");
		fAccessor= new Accessor(object, object.getClass());
	}

	int getDelay() {
		return 50; // make tests run faster
	}

	void aboutToWork(@SuppressWarnings("unused") AbstractReconciler reconciler) {
		// nothing
	}

	@After
	public void tearDown() throws Exception {
		fBarrier.shutdown();
		fReconciler.uninstall();
	}

	@Test
	public void testInitialReconcile() throws InterruptedException {
		// initially the reconciler is neither active nor dirty
		// XXX shouldn't it be dirty?
		assertFalse(isActive());
		assertFalse(isDirty());

		// set up initial document
		fDocument= new Document("foo");
		fViewer.setDocument(fDocument);
		assertEquals("reconcilerDocumentChanged", fCallLog.remove(0));
		assertEquals("aboutToBeReconciled", fCallLog.remove(0));

		fBarrier.await();
		assertEquals("initialProcess", fCallLog.remove(0));
		// XXX shouldn't it be dirty and active during initialProcess?
		assertFalse(isActive());
		assertFalse(isDirty());
		fBarrier.wakeAll();

		// wait until clean
		pollUntilClean();
		assertFalse(isActive());
		assertFalse(isDirty());
	}

	@Test
	public void testDirtyingWhenClean() throws BadLocationException, InterruptedException {
		installDocument();

		dirty();
		assertEquals("aboutToBeReconciled", fCallLog.remove(0));
		assertEquals("reconcilerReset", fCallLog.remove(0));

		fBarrier.await();
		assertEquals("process", fCallLog.remove(0));
		assertTrue(isActive());
		assertTrue(isDirty());
		fBarrier.wakeAll();

		// wait until clean
		pollUntilClean();
		assertFalse(isActive());
		assertFalse(isDirty());
	}


	private void dirty() throws BadLocationException {
		fDocument.replace(0,0,"bar");
	}

	@Test
	public void testDirtyingWhenRunning() throws InterruptedException, BadLocationException {
		installDocument();

		dirty();
		fBarrier.await();
		assertTrue(isActive());
		assertTrue(isDirty());
		fCallLog.clear();
		dirty();

		if (fProgressMonitor.isCanceled())
			assertEquals("aboutToBeReconciled", fCallLog.remove(0));
		else {
			// no aboutToBeReconciled since the reconciler is still running
			// when the second edition comes in
		}

		assertEquals("reconcilerReset", fCallLog.remove(0));
		fBarrier.wakeAll();

		fBarrier.await();
		assertEquals("process", fCallLog.remove(0));
		fBarrier.wakeAll();
		pollUntilClean();
		assertFalse(isActive());
		assertFalse(isDirty());
	}

	@Test
	public void testCancellingWhenClean() throws InterruptedException, BadLocationException {
		installDocument();

		// dirty again
		dirty();
		fBarrier.await();
		fBarrier.wakeAll();

		// cancel
		fCallLog.clear();
		fReconciler.uninstall();
		pollUntilInactive();
		assertTrue(fCallLog.isEmpty());
		assertFalse(isActive());
		// XXX fails since AbstractReconciler does not update state before leaving
//		assertFalse(isDirty()); // fails
	}

	@Test
	public void testCancellingWhenRunning() throws InterruptedException, BadLocationException {
		installDocument();

		// dirty and cancel
		dirty();
		fBarrier.await();
		fCallLog.clear();
		fReconciler.uninstall();
		fBarrier.wakeAll();
		pollUntilInactive();
		assertTrue(fCallLog.isEmpty());
		assertFalse(isActive());
		// XXX fails since AbstractReconciler does not update state before leaving
//		assertFalse(isDirty());
	}

	@Test
	public void testReplacingDocumentWhenClean() throws InterruptedException {
		installDocument();

		// replace
		fCallLog.clear();
		fViewer.setDocument(new Document("bar"));
		assertEquals("reconcilerDocumentChanged", fCallLog.remove(0));
		assertEquals("aboutToBeReconciled", fCallLog.remove(0));
		assertEquals("reconcilerReset", fCallLog.remove(0));
		fBarrier.await();
		assertEquals("process", fCallLog.remove(0));
		fBarrier.wakeAll();

		pollUntilClean();
		assertFalse(isActive());
		assertFalse(isDirty());
	}

	@Test
	public void testReplacingDocumentWhenRunning() throws InterruptedException, BadLocationException {
		installDocument();

		// dirty and replace
		dirty();
		fBarrier.await();
		fCallLog.clear();
		fViewer.setDocument(new Document("bar"));
		assertEquals("reconcilerDocumentChanged", fCallLog.remove(0));
		assertEquals("reconcilerReset", fCallLog.remove(0));
		assertTrue(fCallLog.isEmpty());
		fBarrier.wakeAll();

		// XXX this fails, which is a bug - replacing the document should
		// cancel the progress monitor
//		fBarrier.await();
//		assertEquals("process", fCallLog.remove(0));
//		fBarrier.wakeAll();
	}

	void installDocument() throws InterruptedException {
		fDocument= new Document("foo");
		fViewer.setDocument(fDocument);

		// initial process
		fBarrier.await();
		fBarrier.wakeAll();

		pollUntilClean();
		fCallLog.clear();
	}

	void pollUntilClean() throws InterruptedException {
		// wait for reconciler to become clean
		long start= System.currentTimeMillis();
		while (isDirty()) {
			long current= System.currentTimeMillis();
			if (current > start + 5000)
				fail("waited > 5s for reconciler to complete");
			synchronized (this) {
				wait(50);
			}
		}
	}

	void pollUntilInactive() throws InterruptedException {
		// wait for reconciler to become clean
		long start= System.currentTimeMillis();
		while (isActive()) {
			long current= System.currentTimeMillis();
			if (current > start + 5000)
				fail("waited > 5s for reconciler to complete");
			synchronized (this) {
				wait(50);
			}
		}
	}

	boolean isActive() {
		Object bool= fAccessor.invoke("isActive", null);
		return ((Boolean) bool).booleanValue();
	}

	boolean isCanceled() {
		Object bool= fAccessor.invoke("isCanceled", null);
		return ((Boolean)bool).booleanValue();
	}

	boolean isDirty() {
		Object bool= fAccessor.invoke("isDirty", null);
		return ((Boolean) bool).booleanValue();
	}


}
