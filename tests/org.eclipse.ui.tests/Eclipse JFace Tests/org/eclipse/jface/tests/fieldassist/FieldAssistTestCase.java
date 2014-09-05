/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.fieldassist;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

/**
 * This class contains test cases appropriate for generic field assist
 * tests in various controls.  Tests that are not appropriate for the
 * pre-configured content assist command adapter should go here.
 * 
 * @since 3.6
 *
 */
public abstract class FieldAssistTestCase extends AbstractFieldAssistTestCase {
	static final String SAMPLE_CONTENT = "s";
	static final char ACTIVATE_CHAR = 'i';
	static final char EXTRA_CHAR = 'b';
	
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
		setControlContent(SAMPLE_CONTENT);
		sendKeyDownToControl(ACTIVATE_CHAR);
		ensurePopupIsUp();
		assertTwoShellsUp();
		sendFocusElsewhere();
		spinEventLoop();
		assertOneShellUp();
	}	
	
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
		assertEquals("1.0", SAMPLE_CONTENT + new String(new char [] {ACTIVATE_CHAR}), getControlContent());
	}	
	
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
		assertEquals("1.0", SAMPLE_CONTENT + new String(new char [] {ACTIVATE_CHAR, EXTRA_CHAR}), getControlContent());
	}	
	
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
		window.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				closeFieldAssistWindow();
			}
		});
		spinEventLoop();
	}
	
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
		assertTrue("1.0", decoration.isVisible());
		decoration.hide();
		assertFalse("1.1", decoration.isVisible());
		decoration.setShowOnlyOnFocus(true);
		sendFocusElsewhere();
		sendFocusInToControl();
		spinEventLoop();
		assertFalse("1.2", decoration.isVisible());
		decoration.show();
		assertTrue("1.3", decoration.isVisible());
		sendFocusElsewhere();
		spinEventLoop();
		assertFalse("1.4", decoration.isVisible());
		decoration.setShowOnlyOnFocus(false);
		assertTrue("1.5", decoration.isVisible());
		window.getFieldAssistControl().setVisible(false);
		assertFalse("1.6", decoration.isVisible());
		decoration.hide();
		window.getFieldAssistControl().setVisible(true);
		assertFalse("1.7", decoration.isVisible());
		decoration.show();
		assertTrue("1.8", decoration.isVisible());
	}
	
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
		assertFalse("1.0", window.getContentProposalAdapter().hasProposalPopupFocus());
		window.getContentProposalAdapter().setProposalPopupFocus();
		spinEventLoop();
		assertTrue("1.1", window.getContentProposalAdapter().hasProposalPopupFocus());
		
		// Setting focus to another shell deactivates the popup
		sendFocusElsewhere();
		spinEventLoop();
		assertOneShellUp();
		assertFalse("1.2", window.getContentProposalAdapter().hasProposalPopupFocus());
	}
	
	public void testPopupIsOpen() {
		AbstractFieldAssistWindow window = getFieldAssistWindow();
		window.setPropagateKeys(false);
		KeyStroke stroke = KeyStroke.getInstance(SWT.F4);
		window.setKeyStroke(stroke);
		window.open();
		
		assertFalse("1.0", window.getContentProposalAdapter().isProposalPopupOpen());
		sendKeyDownToControl(stroke);
		assertTwoShellsUp();
		assertTrue("1.1", window.getContentProposalAdapter().isProposalPopupOpen());
		
		// Setting focus to another shell deactivates the popup
		sendFocusElsewhere();
		spinEventLoop();
		assertOneShellUp();
		assertFalse("1.2", window.getContentProposalAdapter().isProposalPopupOpen());
	}
	
	/**
	 * Replace mode is easier to test because we can check that the bounds
	 * does not intersect the control.  In insertion mode, the popup is 
	 * supposed to overlap the control (using the insertion cursor to track
	 * position).
	 */
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
		assertFalse("Popup is blocking the control", popupBounds.intersects(controlBounds));
	}
	
	/**
	 * Replace mode is easier to test because we can check that the bounds
	 * does not intersect the control.  In insertion mode, the popup is 
	 * supposed to overlap the control (using the insertion cursor to track
	 * position).
	 */
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
		assertFalse("Popup is blocking the control", popupBounds.intersects(controlBounds));
	}
}
