/*******************************************************************************
 * Copyright (c) 2016 Rüdiger Herrmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rüdiger Herrmann - Insufficient is-disposed check in LineNumberRulerColumn::redraw - https://bugs.eclipse.org/bugs/show_bug.cgi?id=506427
 *******************************************************************************/
package org.eclipse.jface.text.tests.source;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class LineNumberRulerColumnTest {

	private Shell fParent;

	@BeforeEach
	public void setUp() {
		fParent= new Shell();
	}

	@AfterEach
	public void tearDown() {
		fParent.dispose();
	}

	@Test
	public void testRedrawAfterDispose() {
		LineNumberRulerColumn lineNumberRulerColumn= new LineNumberRulerColumn();
		CompositeRuler ruler= new CompositeRuler();
		ruler.addDecorator(0, lineNumberRulerColumn);
		SourceViewer sourceViewer= new SourceViewer(fParent, ruler, SWT.NONE);
		lineNumberRulerColumn.getControl().setSize(10, 10);

		sourceViewer.getTextWidget().dispose();

		lineNumberRulerColumn.redraw();
	}

	/**
	 * Painting a line range that outlived a document shrink must not ask the widget for lines it
	 * no longer has.
	 */
	@Test
	public void testPaintStaleRangeAfterDocumentShrank() {
		LineNumberRulerColumn lineNumberRulerColumn= new LineNumberRulerColumn();
		CompositeRuler ruler= new CompositeRuler();
		ruler.addDecorator(0, lineNumberRulerColumn);
		fParent.setLayout(new FillLayout());
		ProjectionViewer viewer= new ProjectionViewer(fParent, ruler, null, false, SWT.V_SCROLL) {
			@Override
			public int modelLine2WidgetLine(int modelLine) {
				// a mapping that still knows the lines the widget has already lost
				return modelLine;
			}
		};
		Document document= new Document("line\n".repeat(200));
		viewer.setDocument(document);
		fParent.setSize(300, 200);
		fParent.open();
		fParent.layout(true, true);
		viewer.getTextWidget().setTopIndex(150);

		Accessor accessor= new Accessor(lineNumberRulerColumn, LineNumberRulerColumn.class);
		GC gc= new GC(lineNumberRulerColumn.getControl());
		try {
			accessor.invoke("doubleBufferPaint", new Class<?>[] { GC.class }, gc);
			assertNotNull(accessor.get("fBuffer"));
			ILineRange staleRange= JFaceTextUtil.getVisibleModelLines(viewer);

			document.set("line\n".repeat(3));

			// what the buffer's drawer does when SWT runs it again now
			accessor.invoke("doPaint", new Class<?>[] { GC.class, ILineRange.class }, gc, staleRange);
		} finally {
			gc.dispose();
		}
	}

}
