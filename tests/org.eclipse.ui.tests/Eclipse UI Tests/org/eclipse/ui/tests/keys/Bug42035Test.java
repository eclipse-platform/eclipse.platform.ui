/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.keys;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ParseException;
import org.eclipse.ui.keys.SWTKeySupport;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test for Bug 42035.
 * 
 * @since 3.0
 */
public class Bug42035Test extends UITestCase {

    /**
     * Tests the given key event to see if generates the three key strokes
     * represented by the strings.
     * 
     * @param keyEvent
     *            The key event to generate key strokes from; must not be
     *            <code>null</code>
     * @param firstMatch
     *            The text format of the first match; must not be <code>null</code>.
     * @param secondMatch
     *            The text format of the second match; must not be <code>null</code>.
     * @param thirdMatch
     *            The text format of the third match; must not be <code>null</code>.
     * @throws ParseException
     *             If anyone of the text formats provided cannot be parsed.
     */
    private static void testKeyEvent(Event keyEvent, String firstMatch,
            String secondMatch, String thirdMatch) throws ParseException {
        KeyStroke desiredKeyStroke = null;
        KeyStroke actualKeyStroke = null;

        // Test the first-level match.
        desiredKeyStroke = KeyStroke.getInstance(firstMatch); //$NON-NLS-1$
        actualKeyStroke = SWTKeySupport
                .convertAcceleratorToKeyStroke(SWTKeySupport
                        .convertEventToUnmodifiedAccelerator(keyEvent));
        assertEquals(
                "Unmodified character with all modifiers doesn't match.", desiredKeyStroke, actualKeyStroke); //$NON-NLS-1$

        // Test the second-level match.
        desiredKeyStroke = KeyStroke.getInstance(secondMatch); //$NON-NLS-1$
        actualKeyStroke = SWTKeySupport
                .convertAcceleratorToKeyStroke(SWTKeySupport
                        .convertEventToUnshiftedModifiedAccelerator(keyEvent));
        assertEquals(
                "Modified character with no shift doesn't match.", desiredKeyStroke, actualKeyStroke); //$NON-NLS-1$

        // Test the third-level match.
        desiredKeyStroke = KeyStroke.getInstance(thirdMatch); //$NON-NLS-1$
        actualKeyStroke = SWTKeySupport
                .convertAcceleratorToKeyStroke(SWTKeySupport
                        .convertEventToModifiedAccelerator(keyEvent));
        assertEquals(
                "Modified character with all modifiers doesn't match.", desiredKeyStroke, actualKeyStroke); //$NON-NLS-1$
    }

    /**
     * Constructs a new instance of this test case.
     * 
     * @param testName
     *            The name of the test
     */
    public Bug42035Test(String testName) {
        super(testName);
    }

    /**
     * Tests that "Ctrl+" generates "Ctrl+", "Ctrl+" and "Ctrl+".
     * 
     * @throws ParseException
     *             If "CTRL+" cannot be processed.
     */
    public void testCtrl() throws ParseException {
        Event keyEvent = new Event();
        keyEvent.keyCode = 0x40000;
        keyEvent.character = 0x00;
        keyEvent.stateMask = SWT.NONE;

        KeyStroke desiredKeyStroke = KeyStroke.getInstance("CTRL+"); //$NON-NLS-1$
        KeyStroke actualKeyStroke = SWTKeySupport
                .convertAcceleratorToKeyStroke(SWTKeySupport
                        .convertEventToUnmodifiedAccelerator(keyEvent));
        assertEquals(
                "Unmodified character with all modifiers doesn't match", desiredKeyStroke, actualKeyStroke); //$NON-NLS-1$
    }

    /**
     * Tests that "Ctrl+Enter" generates "Ctrl+Enter", "Ctrl+Enter" and
     * "Ctrl+Enter".
     * 
     * @throws ParseException
     *             If "CTRL+ENTER" cannot be processed.
     */
    public void testCtrlEnter() throws ParseException {
        Event keyEvent = new Event();
        keyEvent.keyCode = 0x0D;
        keyEvent.character = 0x0D;
        keyEvent.stateMask = SWT.CTRL;

        testKeyEvent(keyEvent, "CTRL+CR", "CTRL+CR", "CTRL+CR"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    /**
     * Tests that "Ctrl+J" generates "Ctrl+M", "Ctrl+M" and "Ctrl+M".
     * 
     * @throws ParseException
     *             If "CTRL+M" cannot be processed.
     */
    public void testCtrlM() throws ParseException {
        Event keyEvent = new Event();
        keyEvent.keyCode = 0x6D;
        keyEvent.character = 0x0D;
        keyEvent.stateMask = SWT.CTRL;

        testKeyEvent(keyEvent, "CTRL+M", "CTRL+M", "CTRL+M"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    /**
     * Tests that "Ctrl+Shift+2" generates "Ctrl+Shift+2", "Ctrl+@" and
     * "Ctrl+Shift+@". This simulates a U.S. keyboard layout.
     * 
     * @throws ParseException
     *             If "CTRL+SHIFT+2", "CTRL+@" or "CTRL+SHIFT+@" cannot be
     *             processed.
     */
    public void testCtrlShift2() throws ParseException {
        Event keyEvent = new Event();
        keyEvent.keyCode = '2';
        keyEvent.character = 0x00;
        keyEvent.stateMask = SWT.CTRL | SWT.SHIFT;

        testKeyEvent(keyEvent, "CTRL+SHIFT+2", "CTRL+@", "CTRL+SHIFT+@"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    /**
     * Tests that "Ctrl+Shift+7" generates "Ctrl+Shift+7", "Ctrl+/" and
     * "Ctrl+Shift+/". This simulates a Swiss-German keyboard layout.
     * 
     * @throws ParseException
     *             If "CTRL+SHIFT+7", "CTRL+/" or "CTRL+SHIFT+/" cannot be
     *             processed.
     */
    public void testCtrlShift7_SwissGerman() throws ParseException {
        Event keyEvent = new Event();
        keyEvent.keyCode = '7';
        keyEvent.character = '/';
        keyEvent.stateMask = SWT.CTRL | SWT.SHIFT;

        testKeyEvent(keyEvent, "CTRL+SHIFT+7", "CTRL+/", "CTRL+SHIFT+/"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    /**
     * Tests that "Ctrl+Shift+7" generates "Ctrl+Shift+7", "Ctrl+&" and
     * "Ctrl+Shift+&". This simulates a US keyboard layout.
     * 
     * @throws ParseException
     *             If "CTRL+SHIFT+7", "CTRL+&" or "CTRL+SHIFT+&" cannot be
     *             processed.
     */
    public void testCtrlShift7_US() throws ParseException {
        Event keyEvent = new Event();
        keyEvent.keyCode = '7';
        keyEvent.character = '&';
        keyEvent.stateMask = SWT.CTRL | SWT.SHIFT;

        testKeyEvent(keyEvent, "CTRL+SHIFT+7", "CTRL+&", "CTRL+SHIFT+&"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }
}
