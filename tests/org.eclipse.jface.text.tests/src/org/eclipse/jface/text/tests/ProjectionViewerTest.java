/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.projection.IProjectionPosition;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class ProjectionViewerTest {

	private static final class ProjectionPosition extends Position implements IProjectionPosition {

		public ProjectionPosition(IDocument document) {
			super(0, document.getLength());
		}

		@Override
		public IRegion[] computeProjectionRegions(IDocument document) throws BadLocationException {
			int firstNewLine= document.get().indexOf('\n');
			int secondNewLine= document.get().indexOf('\n', firstNewLine + 1);
			return new IRegion[] { new Region(0, firstNewLine + 1), new Region(secondNewLine + 1, document.getLength() - secondNewLine - 1) };
		}

		@Override
		public int computeCaptionOffset(IDocument document) throws BadLocationException {
			return document.get().indexOf('\n') + 1;
		}

	}

	@Test
	public void testCopyPaste() {
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		ProjectionViewer viewer = new ProjectionViewer(shell, null, null, false, SWT.NONE);
		Document document= new Document("/*\n * content\n */");
		viewer.setDocument(document, new AnnotationModel());
		viewer.enableProjection();
		viewer.getProjectionAnnotationModel().addAnnotation(new ProjectionAnnotation(false), new ProjectionPosition(document));
		shell.setVisible(true);
		viewer.getTextOperationTarget().doOperation(ProjectionViewer.COLLAPSE_ALL);
		viewer.getTextOperationTarget().doOperation(ITextOperationTarget.SELECT_ALL);
		try {
			assertEquals(document.get(), ((ITextSelection) viewer.getSelection()).getText());
			viewer.getTextOperationTarget().doOperation(ITextOperationTarget.COPY);
			assertEquals(document.get(), new Clipboard(viewer.getTextWidget().getDisplay()).getContents(TextTransfer.getInstance()));
		} finally {
			shell.dispose();
		}
	}
}
