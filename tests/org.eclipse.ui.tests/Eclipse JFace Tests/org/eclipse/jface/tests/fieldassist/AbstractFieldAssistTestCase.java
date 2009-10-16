/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *     IBM - ongoing development
 ******************************************************************************/
package org.eclipse.jface.tests.fieldassist;

import junit.framework.TestCase;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractFieldAssistTestCase extends TestCase {

	/**
	 * The window that is being tested.
	 */
	private AbstractFieldAssistWindow window;
	
	/**
	 * A shell used to take focus away from the field assist window
	 */
	private Shell anotherShell;

	/**
	 * The original number of shells at the beginning of the test.
	 */
	private int originalShellCount;

	/**
	 * Sets up this field assist test case. Subclasses should extend but not
	 * override.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Display display = getDisplay();
		anotherShell = new Shell(display);
		new Text(anotherShell, SWT.SINGLE);
		anotherShell.open();
		spinEventLoop();
		originalShellCount = display.getShells().length;
		window = createFieldAssistWindow();
		assertNotNull(window);
	}

	/**
	 * Tears down this field assist test case. Subclasses should extend but not
	 * override.
	 */
	protected void tearDown() throws Exception {
		if (window != null) {
			spinEventLoop();
		}
		closeFieldAssistWindow();
		anotherShell.close();

		super.tearDown();
	}
	
	protected Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	protected void closeFieldAssistWindow() {
		if (window != null) {
			window.close();
			window = null;
		}
	}

	/**
	 * Creates the field assist window that is to be tested.
	 */
	protected abstract AbstractFieldAssistWindow createFieldAssistWindow();

	/**
	 * Returns the created field assist window. May be null if
	 * {@link #createFieldAssistWindow()} has not been called yet or if the test
	 * is being torn down.
	 */
	protected AbstractFieldAssistWindow getFieldAssistWindow() {
		return window;
	}

	protected void spinEventLoop() {
		// spin the event loop again because we have some asyncExec calls in the
		// ContentProposalAdapter class

		Display disp = getDisplay();
		while (disp.readAndDispatch()) {
			;
		}
	}

	protected void ensurePopupIsUp() {
		// if our autoactivation delay is zero, we use an asyncExec to get the
		// popup up, hence, we need to spin the event loop
		if (window.getAutoActivationDelay() == 0) {
			spinEventLoop();
		} else {
			long time = System.currentTimeMillis();
			long target = time + window.getAutoActivationDelay();
			while (target > time) {
				spinEventLoop(); // remain responsive
				time = System.currentTimeMillis();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// nothing to do
			}
			spinEventLoop();
		}
	}

	/**
	 * Gives focus to the field assist control.
	 */
	protected void sendFocusInToControl() {
		window.getFieldAssistControl().setFocus();
		spinEventLoop();
	}
	
	/**
	 * Send focus somewhere besides the field assist shell.
	 * This involves optionally creating another shell.  If we 
	 * create another shell, we need to adjust the originalShellCount
	 */
	protected void sendFocusElsewhere() {
		anotherShell.setFocus();
		spinEventLoop();
	}
	
	/**
	 * Sends focus to the field assist popup.
	 */
	protected void sendFocusToPopup() {
		getFieldAssistWindow().getContentProposalAdapter().setProposalPopupFocus();
		spinEventLoop();
	}

	/**
	 * Sends an SWT KeyDown event for the specified character to the field
	 * assist control.
	 * 
	 * @param character
	 *            the character that has been pressed
	 */
	protected void sendKeyDownToControl(char character) {
		// fake a KeyDown event
		sendFocusInToControl();
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = character;
		assertTrue("unable to post event to display queue for test case", window.getDisplay().post(event));
		spinEventLoop();
	}

	/**
	 * Sends an SWT KeyDown event for the specified keystroke
	 * 
	 * @param character
	 *            the character that has been pressed
	 */
	protected void sendKeyDownToControl(KeyStroke keystroke) {
		// fake a KeyDown event
		sendFocusInToControl();
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = keystroke.getNaturalKey();
		assertTrue("unable to post event to display queue for test case", window.getDisplay().post(event));
		spinEventLoop();
	}

	/**
	 * Checks that there is only one shell up, the original field assist window.
	 */
	protected void assertOneShellUp() {
		spinEventLoop();
		assertEquals("There should only be one shell up, the dialog",
				originalShellCount + 1, window.getDisplay().getShells().length);
	}

	/**
	 * Checks that there are two shells up, the original field assist window and
	 * the proposals popup.
	 */
	protected void assertTwoShellsUp() {
		spinEventLoop();
		assertEquals(
				"There should two shells up, the dialog and the proposals dialog",
				originalShellCount + 2, window.getDisplay().getShells().length);
	}

	protected void setControlContent(String text) {
		window.getControlContentAdapter().setControlContents(
				window.getFieldAssistControl(), text, text.length());

	}

	protected String getControlContent() {
		return window.getControlContentAdapter().getControlContents(
				window.getFieldAssistControl());

	}

}
