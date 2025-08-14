/*******************************************************************************
 * Copyright (c) 2022, 2025 Avaloq Group AG (http://www.avaloq.com).
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Andrew Lamb (Avaloq Group AG) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.eclipse.ui.tests.harness.util.DisplayHelper.runEventLoop;
import static org.eclipse.ui.tests.harness.util.DisplayHelper.waitForCondition;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;

public class DoubleClickTest extends AbstratGenericEditorTest {
	private static final String EDITOR_TEXT= """
		one two three
		four five six
		seven eight nine""";

	@Override
	protected void createAndOpenFile() throws Exception {
		createAndOpenFile("foo.txt", EDITOR_TEXT);
	}

	@Test
	public void testDefaultDoubleClick() throws Exception {
		checkDoubleClickSelectionForCaretOffset(EDITOR_TEXT.indexOf("five") + 1, "five");
	}

	@Test
	public void testEnabledWhenDoubleClick() throws Exception {
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", EDITOR_TEXT);

		checkDoubleClickSelectionForCaretOffset(EDITOR_TEXT.indexOf("five") + 1, "four five six");

		EnabledPropertyTester.setEnabled(false);
	}

	private void checkDoubleClickSelectionForCaretOffset(int pos, String expectedSelection) throws Exception {
		editor.selectAndReveal(pos, 0);
		final StyledText editorTextWidget= (StyledText) editor.getAdapter(Control.class);
		runEventLoop(editorTextWidget.getDisplay(), 0);
		waitForCondition(editorTextWidget.getDisplay(), 3000, ()->
				editorTextWidget.isFocusControl() && editorTextWidget.getSelection().x == pos
		);
		editorTextWidget.getShell().forceActive();
		editorTextWidget.getShell().setActive();
		editorTextWidget.getShell().setFocus();
		editorTextWidget.getShell().getDisplay().wake();
		Rectangle target = editorTextWidget.getCaret().getBounds();
		doubleClick(editorTextWidget, target.x + 5, target.y + 5);
		Assert.assertEquals(expectedSelection, editorTextWidget.getSelectionText());
	}
	
	private void doubleClick(StyledText widget, int x, int y) {
		widget.getDisplay().setCursorLocation(widget.toDisplay(x, y));
		runEventLoop(widget.getDisplay(), 0);

		Event mouseDownEvent= new Event();
		mouseDownEvent.button = 1;
		mouseDownEvent.display = widget.getDisplay();
		mouseDownEvent.doit = true;
		mouseDownEvent.type = SWT.MouseDown;
		mouseDownEvent.widget = widget;
		mouseDownEvent.x = x;
		mouseDownEvent.y = y;

		Event mouseUpEvent= new Event();
		mouseUpEvent.button = 1;
		mouseUpEvent.display = widget.getDisplay();
		mouseUpEvent.doit = true;
		mouseUpEvent.type = SWT.MouseUp;
		mouseUpEvent.widget = widget;
		mouseUpEvent.x = x;
		mouseUpEvent.y = y;

		postEvent(widget, mouseDownEvent);
		postEvent(widget, mouseUpEvent);
		postEvent(widget, mouseDownEvent);
		postEvent(widget, mouseUpEvent);
	}
	
	private void postEvent(StyledText widget, Event event) {
		event.time = (int) System.currentTimeMillis();
		Listener[] listeners= widget.getListeners(event.type);
		for (Listener listener : listeners) {
			listener.handleEvent(event);
		}
		runEventLoop(widget.getDisplay(), 0);
	}
}
