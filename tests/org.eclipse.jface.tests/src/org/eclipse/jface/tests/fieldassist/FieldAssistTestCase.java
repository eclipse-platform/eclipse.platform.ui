/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.fieldassist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

/**
 * This class contains test cases appropriate for generic field assist
 * tests in various controls.  Tests that are not appropriate for the
 * pre-configured content assist command adapter should go here.
 *
 * @since 3.6
 */
public abstract class FieldAssistTestCase extends AbstractFieldAssistTestCase {
	static final String SAMPLE_CONTENT = "s";
	static final char ACTIVATE_CHAR = 'i';
	static final char EXTRA_CHAR = 'b';

	@Test
	public void testAutoactivateNoDelay() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationDelay(0);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
	}

	@Test
	public void testAutoactivateWithDelay() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationDelay(600);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
	}

	@Test
	public void testExplicitActivate() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		KeyStroke stroke = KeyStroke.getInstance(SWT.F4);
		window.setKeyStroke(stroke);
		window.open();
		sendKeyDownToControl(stroke);
		assertTwoShellsUp();
	}

	@Test
	public void testPopupDeactivates() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationDelay(0);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
		sendFocusElsewhere();
		spinEventLoop();
		assertOneShellUp();
	}

	@Test
	public void testPropagateKeysOff() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
		sendKeyDownToControl(EXTRA_CHAR);
		assertEquals(SAMPLE_CONTENT + new String(new char [] {ACTIVATE_CHAR}), getControlContent(), "1.0");
	}

	@Test
	public void testPropagateKeysOn() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(true);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
		sendKeyDownToControl(EXTRA_CHAR);
		assertEquals(SAMPLE_CONTENT + new String(new char [] {ACTIVATE_CHAR, EXTRA_CHAR}), getControlContent(), "1.0");
	}

	@Test
	public void testBug262022() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
		// cursor key down will cause an asyncExec that recomputes proposals.  Before we process the event, let's
		// kill the window in an async.  That should cause the event to be processed and then the window killed before
		// the subsequent async.
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.keyCode = SWT.ARROW_LEFT;
		window.getDisplay().post(event);
		window.getDisplay().asyncExec(this::closeFieldAssistWindow);
		spinEventLoop();
	}

	@Test
	public void testBug279953() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		assertOneShellUp();
		ControlDecoration decoration = new ControlDecoration(getFieldAssistWindow().getFieldAssistControl(), SWT.RIGHT);
		decoration.setImage(FieldDecorationRegistry.getDefault()
			.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
		decoration.setDescriptionText("");
		decoration.showHoverText("");
		assertOneShellUp();
	}

	@Test
	public void testDecorationIsVisible() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.open();
		assertOneShellUp();
		ControlDecoration decoration = new ControlDecoration(getFieldAssistWindow().getFieldAssistControl(), SWT.RIGHT);
		decoration.setImage(FieldDecorationRegistry.getDefault()
			.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
		decoration.setDescriptionText("foo");
		spinEventLoop();
		assertTrue(decoration.isVisible(), "1.0");
		decoration.hide();
		assertFalse(decoration.isVisible(), "1.1");
		decoration.setShowOnlyOnFocus(true);
		sendFocusElsewhere();
		sendFocusInToControl();
		spinEventLoop();
		assertFalse(decoration.isVisible(), "1.2");
		decoration.show();
		assertTrue(decoration.isVisible(), "1.3");
		sendFocusElsewhere();
		spinEventLoop();
		assertFalse(decoration.isVisible(), "1.4");
		decoration.setShowOnlyOnFocus(false);
		assertTrue(decoration.isVisible(), "1.5");
		window.getFieldAssistControl().setVisible(false);
		assertFalse(decoration.isVisible(), "1.6");
		decoration.hide();
		window.getFieldAssistControl().setVisible(true);
		assertFalse(decoration.isVisible(), "1.7");
		decoration.show();
		assertTrue(decoration.isVisible(), "1.8");
	}

	@Test
	public void testPopupFocus() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		KeyStroke stroke = KeyStroke.getInstance(SWT.F4);
		window.setKeyStroke(stroke);
		window.open();
		sendKeyDownToControl(stroke);
		assertTwoShellsUp();

		// Send focus to the control (not the popup)
		window.getFieldAssistControl().setFocus();
		spinEventLoop();
		assertFalse(window.getContentProposalAdapter().hasProposalPopupFocus(), "1.0");
		window.getContentProposalAdapter().setProposalPopupFocus();
		spinEventLoop();
		assertTrue(window.getContentProposalAdapter().hasProposalPopupFocus(), "1.1");

		// Setting focus to another shell deactivates the popup
		sendFocusElsewhere();
		spinEventLoop();
		assertOneShellUp();
		assertFalse(window.getContentProposalAdapter().hasProposalPopupFocus(), "1.2");
	}

	@Test
	public void testPopupIsOpen() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		KeyStroke stroke = KeyStroke.getInstance(SWT.F4);
		window.setKeyStroke(stroke);
		window.open();

		assertFalse(window.getContentProposalAdapter().isProposalPopupOpen(), "1.0");
		sendKeyDownToControl(stroke);
		assertTwoShellsUp();
		assertTrue(window.getContentProposalAdapter().isProposalPopupOpen(), "1.1");

		// Setting focus to another shell deactivates the popup
		sendFocusElsewhere();
		spinEventLoop();
		assertOneShellUp();
		assertFalse(window.getContentProposalAdapter().isProposalPopupOpen(), "1.2");
	}

	/**
	 * Replace mode is easier to test because we can check that the bounds
	 * does not intersect the control.  In insertion mode, the popup is
	 * supposed to overlap the control (using the insertion cursor to track
	 * position).
	 */
	@Test
	public void testBug256651ReplaceMode() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		window.open();
		Display display = getDisplay();
		Rectangle displayBounds = display.getBounds();
		window.getShell().setLocation(0, displayBounds.height - window.getShell().getBounds().height);
		assertOneShellUp();
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
		sendFocusToPopup();
		Shell popupShell = display.getActiveShell();
		Rectangle popupBounds = popupShell.getBounds();
		Rectangle controlBounds = getFieldAssistWindow().getFieldAssistControl().getBounds();
		controlBounds = getDisplay().map(getFieldAssistWindow().getFieldAssistControl().getParent(), null, controlBounds);
		assertFalse(popupBounds.intersects(controlBounds), "Popup is blocking the control");
	}

	/**
	 * Replace mode is easier to test because we can check that the bounds
	 * does not intersect the control.  In insertion mode, the popup is
	 * supposed to overlap the control (using the insertion cursor to track
	 * position).
	 */
	@Test
	public void testDefaultPopupPositioningReplaceMode() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
		window.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		window.open();
		Display display = getDisplay();
		window.getShell().setLocation(0, 0);
		assertOneShellUp();
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
		sendFocusToPopup();
		Shell popupShell = display.getActiveShell();
		Rectangle popupBounds = popupShell.getBounds();
		Rectangle controlBounds = getFieldAssistWindow().getFieldAssistControl().getBounds();
		controlBounds = getDisplay().map(getFieldAssistWindow().getFieldAssistControl().getParent(), null, controlBounds);
		assertFalse(popupBounds.intersects(controlBounds), "Popup is blocking the control");
	}
}
