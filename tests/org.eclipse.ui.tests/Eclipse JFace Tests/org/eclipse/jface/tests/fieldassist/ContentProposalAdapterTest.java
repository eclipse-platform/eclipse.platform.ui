/*******************************************************************************
* Copyright (c) 2017 Benjamin Leipold and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Benjamin Leipold - initial API and implementation
*******************************************************************************/
package org.eclipse.jface.tests.fieldassist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public class ContentProposalAdapterTest {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	/**
	 * A shell that hosts the decorated text control
	 */
	private Shell controlShell;

	/**
	 * Text control to be decorated by {@code contentProposalAdapter}
	 */
	private Text text;

	/**
	 * {@code ContentProposalAdapter} to test
	 */
	private ContentProposalAdapter contentProposalAdapter;

	/**
	 * Display of this test case.
	 */
	private Display display;

	/**
	 * {@code true} if {@code display} has to be disposed in {@link #tearDown()}
	 */
	private boolean disposeDisplay;

	/**
	 * The original number of shells at the beginning of the test.
	 */
	private int originalShellCount;

	/**
	 * bug 520372: ContentProposalAdapter with autoActivationDelay pops up although
	 * control has already lost focus
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=520372
	 *
	 * Tests whether no proposal popup was opened if TAB was pressed within
	 * activation delay.
	 */
	@Test
	public void testBug520372AutoActivationDelayTab() throws Exception {
		sendKeyDownToControl('o');
		sendKeyDownToControl(SWT.TAB);
		ensurePopupIsUp();

		assertOneShellUp();
	}

	/**
	 * bug 520372: ContentProposalAdapter with autoActivationDelay pops up although
	 * control has already lost focus
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=520372
	 *
	 * Tests whether no proposal popup was opened if CR was pressed within
	 * activation delay.
	 */
	@Test
	public void testBug520372AutoActivationDelayCR() throws Exception {
		sendKeyDownToControl('o');
		sendKeyDownToControl(SWT.CR);
		ensurePopupIsUp();

		assertOneShellUp();
	}

	/**
	 * bug 520372: ContentProposalAdapter with autoActivationDelay pops up although
	 * control has already lost focus
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=520372
	 *
	 * Tests whether no proposal popup was opened if ESC was pressed within
	 * activation delay.
	 */
	@Test
	public void testBug520372AutoActivationDelayESC() throws Exception {
		sendKeyDownToControl('o');
		sendKeyDownToControl(SWT.ESC);
		ensurePopupIsUp();

		assertOneShellUp();
	}

	// most of the following code is copied from AbstractFieldAssistTestCase

	@Before
	public final void setUp() throws Exception {
		Display display = getDisplay();
		originalShellCount = display.getShells().length;
		controlShell = new Shell(display);
		text = new Text(controlShell, SWT.SINGLE);
		controlShell.open();
		spinEventLoop();
		contentProposalAdapter = createContentProposalAdapter(this.text);
		assertNotNull(contentProposalAdapter);
	}

	@After
	public final void tearDown() throws Exception {
		if (controlShell != null) {
			spinEventLoop();
			controlShell.close();
		}
		if (display != null) {
			if (disposeDisplay) {
				display.dispose();
			}
			this.display = null;
		}
	}

	private Display getDisplay() {
		if (display == null) {
			Display newDisplay = Display.getCurrent();
			if (newDisplay == null) {
				newDisplay = new Display();
				disposeDisplay = true;
			}
			display = newDisplay;
		}
		return display;
	}

	/**
	 * Gives focus to the field assist control.
	 */
	private void sendFocusInToControl() {
		text.setFocus();
		spinEventLoop();
	}

	/**
	 * Sends an SWT KeyDown event for the specified character to the field assist
	 * control.
	 *
	 * @param character
	 *            the character that has been pressed
	 */
	private void sendKeyDownToControl(char character) {
		// fake a KeyDown event
		sendFocusInToControl();
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = character;
		assertTrue("unable to post event to display queue for test case", text.getDisplay().post(event));
		spinEventLoop();
	}

	private void spinEventLoop() {
		// spin the event loop again because we have some asyncExec calls in the
		// ContentProposalAdapter class

		Display disp = getDisplay();
		while (disp.readAndDispatch()) {
		}
	}

	private ContentProposalAdapter createContentProposalAdapter(Control control) {
		ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(control, new TextContentAdapter(),
				createContentProposalProvider(), null, null);
		contentProposalAdapter.setAutoActivationDelay(2000);
		return contentProposalAdapter;
	}

	private IContentProposalProvider createContentProposalProvider() {
		SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(getProposals());
		return proposalProvider;
	}

	private String[] getProposals() {
		return new String[] { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };
	}

	private void ensurePopupIsUp() {
		// if our autoactivation delay is zero, we use an asyncExec to get the
		// popup up, hence, we need to spin the event loop
		if (contentProposalAdapter.getAutoActivationDelay() == 0) {
			spinEventLoop();
		} else {
			long time = System.currentTimeMillis();
			long target = time + contentProposalAdapter.getAutoActivationDelay();
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
	 * Checks that there is only one shell up, the original field assist window.
	 */
	private void assertOneShellUp() {
		spinEventLoop();
		assertEquals("There should only be one shell up, the dialog", originalShellCount + 1,
				text.getDisplay().getShells().length);
	}
}
