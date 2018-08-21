/*******************************************************************************
* Copyright (c) 2018 Etienne Reichenbach and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Etienne Reichenbach - initial implementation
*******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import static org.eclipse.jface.text.DocumentRewriteSessionType.SEQUENTIAL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.internal.texteditor.quickdiff.DocumentLineDiffer;

/**
 * Tests for the {@link DocumentLineDiffer}.
 */
public class DocumentLineDifferTest {

	/** The document to connect to the {@link #fLineDiffer}. */
	private Document fDocument= new Document();


	/** The {@link DocumentLineDiffer line differ} under test. */
	private DocumentLineDiffer fLineDiffer= new DocumentLineDiffer();


	/**
	 * Test that when a document is {@link DocumentLineDiffer#connect(IDocument) connected} the
	 * differ is neither {@link DocumentLineDiffer#isSuspended() suspended} nor
	 * {@link DocumentLineDiffer#isSynchronized() synchronized} (i.e. it is initializing).
	 *
	 * @throws Exception unexpected exception
	 */
	@Test
	public void testLineDifferStateAfterConnectingDocument() throws Exception {
		// when
		fLineDiffer.connect(fDocument);

		// then
		assertFalse(fLineDiffer.isSuspended());
		assertFalse(fLineDiffer.isSynchronized());
	}

	/**
	 * Test that when {@link DocumentLineDiffer#suspend()} is called the differ is
	 * {@link DocumentLineDiffer#isSuspended() suspended}.
	 *
	 * @throws Exception unexpected exception
	 */
	@Test
	public void suspendSuspendsLineDiffer() throws Exception {
		// given
		fLineDiffer.connect(fDocument);

		// when
		fLineDiffer.suspend();

		// then
		assertTrue(fLineDiffer.isSuspended());
	}

	/**
	 * Test that after a suspended {@link DocumentLineDiffer line differ} is
	 * {@link DocumentLineDiffer#resume() resumed} is not {@link DocumentLineDiffer#isSuspended()
	 * suspended} anymore.
	 *
	 * @throws Exception unexpected exception
	 */
	@Test
	public void lineDifferNotSuspendedAfterResumeIsCalled() throws Exception {
		// given
		fLineDiffer.connect(fDocument);
		fLineDiffer.suspend();

		// when
		fLineDiffer.resume();

		// then
		assertFalse(fLineDiffer.isSuspended());
		assertFalse(fLineDiffer.isSynchronized());
	}

	/**
	 * Test that when the document connected to a non suspended {@link DocumentLineDiffer line
	 * differ} starts a rewrite session the differ gets suspended.
	 *
	 * @throws Exception unexpected exception
	 */
	@Test
	public void nonSuspendedLineDifferNotSuspendedAfterStartRewriteSession() throws Exception {
		// given
		fLineDiffer.connect(fDocument);

		// when
		fDocument.startRewriteSession(SEQUENTIAL);

		// then
		assertTrue(fLineDiffer.isSuspended());
	}

	/**
	 * Test that when a document connected to a suspended {@link DocumentLineDiffer line differ}
	 * stops the rewrite session, the differ gets suspended.
	 *
	 * @throws Exception unexpected exception
	 */
	@Test
	public void suspendedLineDifferStillSuspendedAfterStopRewriteSession() throws Exception {
		// given
		fLineDiffer.connect(fDocument);
		fLineDiffer.suspend();

		// when
		fDocument.stopRewriteSession(fDocument.getActiveRewriteSession());

		// then
		assertTrue(fLineDiffer.isSuspended());
	}

	/**
	 * Test that when the document connected to a suspended {@link DocumentLineDiffer line differ}
	 * goes through a rewrite session, the differ stays suspended.
	 *
	 * @throws Exception unexpected exception
	 */
	@Test
	public void suspendedLineDifferStaysSuspendedAfterDocumentRewriteSession() throws Exception {
		// given
		fLineDiffer.connect(fDocument);
		fLineDiffer.suspend();

		// when
		fDocument.startRewriteSession(SEQUENTIAL);
		fDocument.stopRewriteSession(fDocument.getActiveRewriteSession());

		// then
		assertTrue(fLineDiffer.isSuspended());
	}

	/**
	 * Test that when a document connected to a non suspended {@link DocumentLineDiffer line differ}
	 * goes through a rewrite session, the differ stays in the non suspended state.
	 *
	 * @throws Exception unexpected exception
	 */
	@Test
	public void nonSuspendedLineDifferStaysNonSuspendedAfterDocumentRewriteSession() throws Exception {
		// given
		fLineDiffer.connect(fDocument);

		// when
		fDocument.startRewriteSession(SEQUENTIAL);
		fDocument.stopRewriteSession(fDocument.getActiveRewriteSession());

		// then
		assertFalse(fLineDiffer.isSuspended());
	}

}
