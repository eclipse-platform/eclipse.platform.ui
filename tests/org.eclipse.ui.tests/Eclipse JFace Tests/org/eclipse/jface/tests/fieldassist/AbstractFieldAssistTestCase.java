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
import org.eclipse.ui.PlatformUI;

public abstract class AbstractFieldAssistTestCase extends TestCase {
	static final char ACTIVATE_CHAR = 'i';
	static final String SAMPLE_CONTENT = "s";
	
	/**
	 * The window that is being tested.
	 */
	private AbstractFieldAssistWindow window;

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
		originalShellCount = Display.getDefault().getShells().length;
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

			// clean-up
			window.close();
			window = null;
		}

		super.tearDown();
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
		while (window.getDisplay().readAndDispatch())
			;
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
				spinEventLoop();  // remain responsive
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
	 * Sends an SWT FocisIn event to the field assist control.
	 */
	protected void sendFocusInToControl() {
		Event event = new Event();
		event.type = SWT.FocusIn;
		window.getFieldAssistControl().notifyListeners(SWT.FocusIn, event);
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
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = character;
		window.getFieldAssistControl().notifyListeners(SWT.KeyDown, event);
	}
	
	/**
	 * Sends an SWT KeyDown event for the specified keystroke
	 * 
	 * @param character
	 *            the character that has been pressed
	 */
	protected void sendKeyDownToControl(KeyStroke keystroke) {
		// fake a KeyDown event
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = keystroke.getNaturalKey();
		window.getFieldAssistControl().notifyListeners(SWT.KeyDown, event);
	}

	/**
	 * Checks that there is only one shell up, the original field assist window.
	 */
	protected void assertOneShellUp() {
		assertEquals("There should only be one shell up, the dialog",
				originalShellCount + 1, window.getDisplay().getShells().length);
	}

	/**
	 * Checks that there are two shells up, the original field assist window and
	 * the proposals popup.
	 */
	protected void assertTwoShellsUp() {
		assertEquals(
				"There should two shells up, the dialog and the proposals dialog",
				originalShellCount + 2, window.getDisplay().getShells().length);
	}
	
	protected void setControlContent(String text) {
		window.getControlContentAdapter().setControlContents(window.getFieldAssistControl(), text, text.length());

	}
	
	protected String getControlContent() {
		return window.getControlContentAdapter().getControlContents(window.getFieldAssistControl());

	}

	public void testAutoactivateNoDelay() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationDelay(0);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
	}	
	
	public void testAutoactivateWithDelay() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationDelay(600);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
	}	
	
	public void testExplicitActivate() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		KeyStroke stroke = KeyStroke.getInstance(SWT.F4);
		window.setKeyStroke(stroke);
		window.open();
		sendKeyDownToControl(stroke);
		assertTwoShellsUp();
	}
	
	public void testPopupDeactivates() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationDelay(0);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setFocus();
		spinEventLoop();
		assertOneShellUp();
	}	
	
	/*
	 * This test doesn't prove much.  The propagate keys behavior relies on doit flags set in the event to
	 * cause or prevent propagation.  However we are faking the keydown event so the control won't be involved
	 * at all since the event was not native.
	 */
	public void testPropagateKeysOff() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
		sendKeyDownToControl('B');
		assertEquals("1.0", SAMPLE_CONTENT, getControlContent());
	}	
}
