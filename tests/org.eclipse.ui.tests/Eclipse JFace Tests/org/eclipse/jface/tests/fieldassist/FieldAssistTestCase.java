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
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

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
	// in progress
	public void testBug275525() {
			final boolean [] closeWindow = new boolean[1];
			closeWindow[0] = false;
			AbstractFieldAssistWindow window = getFieldAssistWindow();
			window.setPropagateKeys(false);
			window.setAutoActivationDelay(600);
			window.setAutoActivationCharacters(new char [] {ACTIVATE_CHAR});
			window.setContentProposalProvider(new IContentProposalProvider() {
				public IContentProposal[] getProposals(String contents,
						int position) {
					IContentProposal[] proposals = new IContentProposal[1];
					proposals[0] = new IContentProposal() {
						public String getContent() {
							return "Foo";
						}

						public int getCursorPosition() {
							return 0;
						}

						public String getDescription() {
							if (closeWindow[0]) {
								closeFieldAssistWindow();
								return "Description";
							}
							return null;
						}

						public String getLabel() {
							return "Foo";
						}
						
					};
					return proposals;
				}
				
			});
			window.open();
			setControlContent(SAMPLE_CONTENT);
			sendKeyDownToControl(ACTIVATE_CHAR);
			// autoactivate is started but nothing is up yet.  Set the flag
			// that will destroy the window during description access.
			closeWindow[0] = true;
	}
}
