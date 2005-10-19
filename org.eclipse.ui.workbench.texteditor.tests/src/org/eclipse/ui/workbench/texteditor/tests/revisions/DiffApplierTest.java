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
package org.eclipse.ui.workbench.texteditor.tests.revisions;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.revisions.Revision;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

import org.eclipse.jface.internal.text.revisions.ChangeRegion;
import org.eclipse.jface.internal.text.revisions.DiffApplier;
import org.eclipse.ui.internal.texteditor.quickdiff.DocumentLineDiffer;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider;

/**
 * 
 * @since 3.2
 */
public class DiffApplierTest extends TestCase {
	public static Test suite() {
		return new TestSuite(DiffApplierTest.class); 
	}

	private static final String CONTENT=
			"one   \n" +
			"two   \n" +
			"three \n" +
			"four  \n" +
			"five  \n" +
			"six   \n" +
			"seven1\n" +
			"seven2\n" +
			"seven3\n" +
			"seven4\n" +
			"seven5\n" +
			"eight \n";
	private static final int LINE_LENGTH= 7;
	
	private Document fDocument;
	private DocumentLineDiffer fDiffer;
	
	protected void setUp() throws Exception {
		IDocument reference= new Document(CONTENT);
		fReferenceProvider= new TestReferenceProvider(reference);
		fDiffer= new DocumentLineDiffer();
		setUpDiffer(fDiffer);
		fDocument= new Document(CONTENT);
		BooleanFuture future= waitForSynchronization(fDiffer);
		fDiffer.connect(fDocument);
		assertTrue(future.get());
		
		fApplier= new DiffApplier();
		fRevision1= new MyRevision();
		fRevision2= new MyRevision();
		fRegions= new ArrayList();
		fRegions.add(new ChangeRegion(fRevision1, new LineRange(0, 2)));
		fRegions.add(new ChangeRegion(fRevision2, new LineRange(2, 2)));
		fRegions.add(new ChangeRegion(fRevision1, new LineRange(4, 2)));
		fRegions.add(new ChangeRegion(fRevision2, new LineRange(6, 5)));
		fRegions.add(new ChangeRegion(fRevision1, new LineRange(11, 1)));
		
		fApplier.applyDiff(fRegions, fDiffer, fDocument.getNumberOfLines());
	}

	public void testNoDiff() throws Exception {
		assertRangesEqual(fRegions);
	}
	
	public void testShiftOne() throws Exception {
		replace(0, 1, null);
		
		assertSingleEqualRange(0, 1, 1);
		assertRangesEqual(1);
	}

	public void testRemoveFirstLine() throws Exception {
		replace(0, LINE_LENGTH, null);
		
		assertSingleEqualRange(0, 0, 1);
		assertRangesShifted(1, -1);
	}
	
	public void testRemoveSecondLine() throws Exception {
		replace(LINE_LENGTH, LINE_LENGTH, null);
		
		assertSingleEqualRange(0, 0, 1);
		assertRangesShifted(1, -1);
	}
	
	public void testAddFirstLine() throws Exception {
		replace(0, 0, "added  \n");
		
		assertRangesShifted(0, 1);
	}
	
	public void testAddSecondLine() throws Exception {
		replace(LINE_LENGTH, 0, "added \n");
		
		assertRange(0, 0, 0, 1);
		assertRange(0, 1, 2, 1);
		assertRangesShifted(1, 1);
	}
	
	public void testAddThirdLine() throws Exception {
		replace(LINE_LENGTH * 2, 0, "added \n");
		
		assertRangesEqual(0, 1);
		assertRangesShifted(1, 1);
	}
	
	public void testRemoveFirstRegion() throws Exception {
		replace(0, LINE_LENGTH * 2, null);
		
		assertRegionEmpty(0);
		assertRangesShifted(1, -2);
	}
	
	public void testReplaceFirstRegion() throws Exception {
		replace(0, LINE_LENGTH * 2, "added\nadded\n");
		
		assertRegionEmpty(0);
		assertRangesEqual(1);
	}
	
	public void testRemoveOverlappingRegion() throws Exception {
		replace(LINE_LENGTH, LINE_LENGTH * 2, null);
		
		assertRange(0, 0, 0, 1);
		assertRange(1, 0, 1, 1);
		assertRangesShifted(2, -2);
	}
	
	public void testReplaceOverlappingRegion() throws Exception {
		replace(LINE_LENGTH, LINE_LENGTH * 2, "added\nadded\n");
		
		assertRange(0, 0, 0, 1);
		assertRange(1, 0, 3, 1);
		assertRangesEqual(2);
	}
	
	public void testRemoveInnerLines() throws Exception {
		replace(LINE_LENGTH * 8, LINE_LENGTH * 2, null);
		
		assertRangesEqual(0, 3);
		assertRange(3, 0, 6, 2);
		assertRange(3, 1, 8, 1);
		assertRangesShifted(4, -2);
	}
	
	public void testReplaceInnerLines() throws Exception {
		replace(LINE_LENGTH * 8, LINE_LENGTH * 2, "added\nadded\n");
		
		assertRangesEqual(0, 3);
		assertRange(3, 0, 6, 2);
		assertRange(3, 1, 10, 1);
		assertRangesEqual(4);
	}
	
	public void testAddInnerLines() throws Exception {
		replace(LINE_LENGTH * 8, 0, "added\nadded\n");
		
		assertRangesEqual(0, 3);
		assertRange(3, 0, 6, 2);
		assertRange(3, 1, 10, 3);
		assertRangesShifted(4, 2);
	}
	
	public void testRemoveLastLine() throws Exception {
		replace(LINE_LENGTH * 11, LINE_LENGTH, null);
		
		assertRangesEqual(0, 4);
		assertRegionEmpty(4);
	}
	
	public void testReplaceLastLine() throws Exception {
		replace(LINE_LENGTH * 11, LINE_LENGTH, "added\n");
		
		assertRangesEqual(0, 4);
		assertRegionEmpty(4);
	}
	
	public void testAddLastLine() throws Exception {
		replace(LINE_LENGTH * 12, 0, "added\n");
		
		assertRangesEqual(0, 5);
	}
	
	private void assertRegionEmpty(int region) {
		assertTrue(((ChangeRegion) fRegions.get(region)).getAdjustedRanges().isEmpty());
	}

	private void assertRange(int region, int subrange, int line, int lines) {
		RangeUtil.assertEqualRange(new LineRange(line, lines), (ILineRange) ((ChangeRegion) fRegions.get(region)).getAdjustedRanges().get(subrange));
	}
	
	private void assertSingleEqualRange(int region, int line, int lines) {
		RangeUtil.assertEqualSingleRange(new LineRange(line, lines), ((ChangeRegion) fRegions.get(region)).getAdjustedRanges());
	}

	private void replace(int offset, int length, String text) throws BadLocationException, InterruptedException {
		BooleanFuture future= waitForSynchronization(fDiffer);
		fDocument.replace(offset, length, text);
		assertTrue(future.get());
		fApplier.applyDiff(fRegions, fDiffer, fDocument.getNumberOfLines());
	}
	
	private void assertRangesEqual(int fromIndex) {
		assertRangesEqual(fromIndex, fRegions.size());
	}
	
	private void assertRangesEqual(int fromIndex, int toIndex) {
		assertRangesEqual(fRegions.subList(fromIndex, toIndex));
	}
	
	private static void assertRangesEqual(List regions) {
		for (Iterator it= regions.iterator(); it.hasNext();) {
			ChangeRegion region= (ChangeRegion) it.next();
			RangeUtil.assertEqualSingleRange(region.getOriginalRange(), region.getAdjustedRanges());
		}
	}

	private void assertRangesShifted(int fromIndex, int shift) {
		assertRangesShifted(fromIndex, fRegions.size(), shift);
	}
	
	private void assertRangesShifted(int fromIndex, int toIndex, int shift) {
		assertRangesShifted(fRegions.subList(fromIndex, toIndex), shift);
	}
	
	private static void assertRangesShifted(List regions, int shift) {
		for (Iterator it= regions.iterator(); it.hasNext();) {
			ChangeRegion region= (ChangeRegion) it.next();
			ILineRange expected= new LineRange(region.getOriginalRange().getStartLine() + shift, region.getOriginalRange().getNumberOfLines());
			RangeUtil.assertEqualSingleRange(expected, region.getAdjustedRanges());
		}
	}
	
	protected void tearDown() throws Exception {
		if (fFirstException != null)
			throw fFirstException;
		
		super.tearDown();
	}
	
	private TestReferenceProvider fReferenceProvider;
	protected final void setUpDiffer(DocumentLineDiffer differ) {
		differ.setReferenceProvider(fReferenceProvider);
	}
	
	/**
	 * Immediately returns a future variable for the synchronization
	 * state of the differ. The caller is responsible for setting up the
	 * reference provider and connecting the differ to a document.
	 * 
	 * @param differ the document line differ.
	 * @return a future variable for the differ's synchronization state
	 */
	protected final BooleanFuture waitForSynchronization(final DocumentLineDiffer differ) {
		final BooleanFuture future= new BooleanFuture();
		Thread thread= new Thread(new Runner() {
			void runProtected() {
				synchronized (differ) {
					try {
						differ.notifyAll();
						differ.wait(MAX_WAIT); // the initialization job notifies waiters upon finishing
					} catch (InterruptedException x) {
						return;
					}
				}
				future.set(differ.isSynchronized());
			}
		});
		future.setThread(thread);
		synchronized (differ) {
			thread.start();
			try {
				differ.wait();
			} catch (InterruptedException x) {
				throw new Error(x);
			}
		}
		
		return future;
	}
	private Exception fFirstException;
	private synchronized void reportException(Exception x) {
		if (fFirstException == null)
			fFirstException= x;
	}
	
	static boolean equals(ILineRange one, ILineRange two) {
		return one.getStartLine() == two.getStartLine() && one.getNumberOfLines() == two.getNumberOfLines();
	}

	private static class MyRevision extends Revision {
		public String getId() {
			return null;
		}
		public Object getHoverInfo() {
			return null;
		}
		public Date getDate() {
			return null;
		}
		public RGB getColor() {
			return null;
		}
	}

	private static final class TestReferenceProvider implements IQuickDiffReferenceProvider {

		private final IDocument fDocument;
		
		public TestReferenceProvider(IDocument reference) {
			fDocument= reference;
		}
		
		public IDocument getReference(IProgressMonitor monitor) throws CoreException {
			return fDocument;
		}

		public void dispose() {
		}

		public String getId() {
			return "testProvider";
		}

		public void setActiveEditor(ITextEditor editor) {
		}

		public boolean isEnabled() {
			return true;
		}

		public void setId(String id) {
		}

	}
	
	private abstract class Runner implements Runnable {

		public final void run() {
			try {
				runProtected();
			} catch (Exception x) {
				reportException(x);
			}
		}

		abstract void runProtected() throws Exception;
		
	}

	protected static final class BooleanFuture {
		private final Object fLock= new Object();
		private Boolean fResult= null;
		private Thread fThread;
		private boolean fIsCanceled= false;
		
		/**
		 * Returns the value of the future variable, waiting for
		 * completion if not yet done.
		 * 
		 * @return the boolean value of the future variable
		 * @throws InterruptedException
		 */
		public boolean get() throws InterruptedException {
			synchronized (fLock) {
				while (fResult == null && !fIsCanceled)
					fLock.wait();
				return fResult.booleanValue();
			}
		}
		
		void set(boolean b) {
			synchronized (fLock) {
				fResult= Boolean.valueOf(b);
				fLock.notifyAll();
			}
		}

		/**
		 * Returns <code>true</code> if the future has completed
		 * (computed, cancelled, interrupted), <code>false</code> if
		 * not.
		 * 
		 * @return <code>true</code> if <code>get()</code> will
		 *         return without blocking, <code>false</code> if it
		 *         may block
		 */
		public boolean isDone() {
			synchronized (fLock) {
				return fResult != null || fIsCanceled;
			}
		}

		/**
		 * Cancels waiting for this future variable.
		 */
		public void cancel() {
			synchronized (fLock) {
				if (fResult != null)
					return;
				if (fThread != null)
					fThread.interrupt();
				fIsCanceled= true;
			}
		}

		void setThread(Thread thread) {
			fThread= thread;
		}
	}
	private static final long MAX_WAIT= 10000; // wait 10 seconds at most

	private DiffApplier fApplier;

	private Revision fRevision1;

	private Revision fRevision2;

	private List fRegions;
}
