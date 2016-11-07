/*******************************************************************************
 * Copyright (c) 2016 Rüdiger Herrmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rüdiger Herrmann - Insufficient is-disposed check in LineNumberRulerColumn::redraw - https://bugs.eclipse.org/bugs/show_bug.cgi?id=506427
 *******************************************************************************/
package org.eclipse.jface.text.tests.source;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;

public class LineNumberRulerColumnTest {

	private Shell fParent;

	@Before
	public void setUp() {
		fParent= new Shell();
	}

	@After
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

}
