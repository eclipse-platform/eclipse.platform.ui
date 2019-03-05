/*******************************************************************************
 * Copyright (c) 2014-2019 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sergey Prigogin (Google) - initial API and implementation
 * - Mickael Istria (Red Hat Inc.) - [Bug 544708] Ctrl+Home
 *******************************************************************************/
package org.eclipse.jface.text.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

/**
 * Basic tests for TextViewer.
 */
public class TextViewerTest {

	@Rule public ScreenshotOnFailureRule screenshotRule = new ScreenshotOnFailureRule();

	@Test
	public void testSetRedraw_Bug441827() throws Exception {
		Shell shell= new Shell();
		try {
			TextViewer textViewer= new TextViewer(shell, SWT.NONE);
			Document document= new Document("abc");
			textViewer.setDocument(document);
			int len= document.getLength();
			// Select the whole document with the caret at the beginning.
			textViewer.setSelectedRange(len, -len);
			assertEquals(0, textViewer.getSelectedRange().x);
			assertEquals(len, textViewer.getSelectedRange().y);
			assertEquals(0, textViewer.getTextWidget().getCaretOffset());
			textViewer.setRedraw(false);
			textViewer.setRedraw(true);
			// Check that the selection and the caret position are preserved.
			assertEquals(0, textViewer.getSelectedRange().x);
			assertEquals(len, textViewer.getSelectedRange().y);
			assertEquals(0, textViewer.getTextWidget().getCaretOffset());
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testCtrlHomeViewportListener() {
		Assume.assumeFalse("See bug 541415. For whatever reason, this shortcut doesn't work on Mac", Util.isMac());
		Shell shell= new Shell();
		try {
			shell.setLayout(new FillLayout());
			shell.setSize(500, 200);
			SourceViewer textViewer= new SourceViewer(shell, null, SWT.NONE);
			textViewer.setDocument(new Document(generate5000Lines()));
			shell.open();
			textViewer.revealRange(4000, 1);
			AtomicBoolean notifyHomeReached = new AtomicBoolean();
			ctrlEnd(textViewer);
			DisplayHelper.sleep(textViewer.getTextWidget().getDisplay(), 1000);
			textViewer.addViewportListener(offset -> notifyHomeReached.set(offset == 0));
			ctrlHome(textViewer);
			assertTrue(new DisplayHelper() {
				@Override
				protected boolean condition() {
					return notifyHomeReached.get();
				}
			}.waitForCondition(textViewer.getTextWidget().getDisplay(), 3000));
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testCtrlEndViewportListener() {
		Assume.assumeFalse("See bug 541415. For whatever reason, this shortcut doesn't work on Mac", Util.isMac());
		Shell shell= new Shell();
		try {
			shell.setLayout(new FillLayout());
			shell.setSize(500, 200);
			SourceViewer textViewer= new SourceViewer(shell, null, SWT.NONE);
			Document document= new Document(generate5000Lines());
			textViewer.setDocument(document);
			shell.open();
			AtomicBoolean notifyEndReached = new AtomicBoolean();
			textViewer.addViewportListener(offset ->
				notifyEndReached.set(offset > 4000));
			ctrlEnd(textViewer);
			assertTrue(new DisplayHelper() {
				@Override
				protected boolean condition() {
					return notifyEndReached.get();
				}
			}.waitForCondition(textViewer.getControl().getDisplay(), 3000));
		} finally {
			shell.dispose();
		}
	}

	public static void ctrlEnd(ITextViewer viewer) {
		postKeyEvent(viewer.getTextWidget(), SWT.END, SWT.CTRL, SWT.KeyDown);
	}

	public static void ctrlHome(ITextViewer viewer) {
		postKeyEvent(viewer.getTextWidget(), SWT.HOME, SWT.CTRL, SWT.KeyDown);
	}

	private static void postKeyEvent(Control widget, int keyCode, int stateMask, int type) {
		Display display= widget.getDisplay();
		widget.setFocus();
		DisplayHelper.driveEventQueue(display);
		Event event = new Event();
		event.widget = widget;
		event.keyCode = keyCode;
		event.stateMask = stateMask;
		event.type = type;
		event.doit = true;
		// display.post(event) seem not always work, see bug 541415
		Listener[] listeners= widget.getListeners(type);
		for (Listener listener : listeners) {
			listener.handleEvent(event);
		}
		DisplayHelper.driveEventQueue(display);
	}


	public static String generate5000Lines() {
		StringBuilder b = new StringBuilder("start");
		for (int i = 0; i < 5000; i++) {
			b.append('\n');
		}
		b.append("end");
		return b.toString();
	}

}
