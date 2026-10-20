/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.operation.IRunnableContext;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.texteditor.AbstractDocumentProvider;
import org.eclipse.ui.texteditor.StatusTextEditor;

/**
 * A minimal text editor for tests that need a real editor part rather than a bare
 * viewer, without pulling in the workspace or the IDE: its input is held in memory
 * and nothing is ever saved. Contributed by this bundle's {@code plugin.xml} and
 * opened by {@link #ID}, with no file name or content type association, so it is
 * never offered for a real file.
 */
public class TestTextEditor extends StatusTextEditor {

	public static final String ID = "org.eclipse.ui.workbench.texteditor.tests.testTextEditor"; //$NON-NLS-1$

	public TestTextEditor() {
		setDocumentProvider(new InMemoryDocumentProvider());
		// Normally established by AbstractDecoratedTextEditor, which belongs to a higher
		// layer. This scope is what makes an editor a text editor for key bindings.
		setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope" }); //$NON-NLS-1$
	}

	private static final class InMemoryDocumentProvider extends AbstractDocumentProvider {

		@Override
		protected IDocument createDocument(Object element) {
			return new Document(element instanceof TestTextEditorInput input ? input.getContent() : ""); //$NON-NLS-1$
		}

		@Override
		protected IAnnotationModel createAnnotationModel(Object element) {
			return null;
		}

		@Override
		protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document,
				boolean overwrite) {
			// nothing to save, the document only lives for the duration of a test
		}

		@Override
		protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
			return null;
		}

		// AbstractDocumentProvider defaults to read-only, for which the overlay hides
		// its replace field, unlike the editors it is actually used with.

		@Override
		public boolean isReadOnly(Object element) {
			return false;
		}

		@Override
		public boolean isModifiable(Object element) {
			return true;
		}
	}

}
