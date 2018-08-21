/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.text.undo.DocumentUndoEvent;
import org.eclipse.text.undo.DocumentUndoManager;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextViewerUndoManager;

/**
 * Tests for TextViewerUndoManager.
 *
 * @since 3.2
 */
public class TextViewerUndoManagerTest extends AbstractUndoManagerTest {

	@Override
	protected IUndoManager createUndoManager(int maxUndoLevel) {
		return new TextViewerUndoManager(maxUndoLevel);
	}

	//--- DocumentUndoManager only ---

	public void internalTestTransferNonTextOp(final boolean isUndoable) throws Exception {
		Object context= new Object();
		DocumentUndoManager tempUndoManager= new DocumentUndoManager(new Document());
		tempUndoManager.connect(context);

		IUndoableOperation operation= new AbstractOperation("") {
			@Override
			public boolean canUndo() {
				return isUndoable;
			}
			@Override
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				return Status.OK_STATUS;
			}
			@Override
			public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				return Status.OK_STATUS;
			}
			@Override
			public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				return Status.OK_STATUS;
			}
		};
		operation.addContext(tempUndoManager.getUndoContext());
		OperationHistoryFactory.getOperationHistory().add(operation);

		assertEquals(isUndoable, tempUndoManager.undoable());

		final DocumentUndoManager undoManager= new DocumentUndoManager(new Document());
		Object newContext= new Object();
		undoManager.connect(newContext);

		undoManager.addDocumentUndoListener(event -> fail());

		undoManager.transferUndoHistory(tempUndoManager);
		tempUndoManager.disconnect(context);

		assertEquals(isUndoable, undoManager.undoable());
		undoManager.undo();
		assertFalse(undoManager.undoable());

		undoManager.disconnect(newContext);
	}
	
	@Test
	public void testTransferNonUndoableNonTextOp() throws Exception {
		internalTestTransferNonTextOp(false);
	}
	
	@Test
	public void testTransferUndoableNonTextOp() throws Exception {
		internalTestTransferNonTextOp(true);
	}
	
	@Test
	public void testCanUndo() throws Exception {
		IDocument doc= new Document();
		final DocumentUndoManager undoManager= new DocumentUndoManager(doc);
		Object context= new Object();
		undoManager.connect(context);

		undoManager.addDocumentUndoListener(event -> {
			if (event.getEventType() == DocumentUndoEvent.ABOUT_TO_UNDO)
				assertTrue(undoManager.undoable());
			else if (event.getEventType() == DocumentUndoEvent.UNDONE)
				assertFalse(undoManager.undoable());
		});

		doc.set("foo");

		assertTrue(undoManager.undoable());
		undoManager.undo();
		assertFalse(undoManager.undoable());

		undoManager.disconnect(context);
	}

}
