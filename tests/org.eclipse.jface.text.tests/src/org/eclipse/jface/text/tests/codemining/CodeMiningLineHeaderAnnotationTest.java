/*******************************************************************************
 * Copyright (c) 2025 SAP SE
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.codemining;

import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.internal.text.codemining.CodeMiningLineHeaderAnnotation;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.AbstractInlinedAnnotation;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;
import org.eclipse.jface.text.tests.util.DisplayHelper;

public class CodeMiningLineHeaderAnnotationTest {

	private SourceViewer fViewer;

	private Shell fShell;

	private Document document;

	@Before
	public void setUp() {
		fShell= new Shell(Display.getDefault());
		fShell.setSize(500, 200);
		fShell.setLayout(new FillLayout());
		fViewer= new SourceViewer(fShell, null, SWT.NONE);
		final StyledText textWidget= fViewer.getTextWidget();
		document= new Document("a");
		textWidget.setText(document.get());
		fViewer.setDocument(document, new AnnotationModel());
		final Display display= textWidget.getDisplay();
		fShell.open();
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().isVisible();
			}
		}.waitForCondition(display, 3000));
		DisplayHelper.sleep(textWidget.getDisplay(), 1000);
	}

	@After
	public void tearDown() {
		fViewer= null;
	}

	@Test
	public void testGetHeightDoesNotReturnZero() throws Exception {
		var cut= new CodeMiningLineHeaderAnnotation(new Position(0, 0), fViewer);
		var s= new InlinedAnnotationSupport();
		s.install(fViewer, new AnnotationPainter(fViewer, null));
		var m= AbstractInlinedAnnotation.class.getDeclaredMethod("setSupport", InlinedAnnotationSupport.class);
		m.setAccessible(true);
		m.invoke(cut, s);
		cut.update(Arrays.asList(new LineHeaderCodeMining(0, document, null) {
			@Override
			public String getLabel() {
				return "mining";
			}
		}), null);
		// https: //github.com/eclipse-platform/eclipse.platform.ui/issues/2786
		assertNotEquals(0, cut.getHeight()); // getHeight should not return 0, otherwise editor content starts jumping around
	}
}
