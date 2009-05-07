/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 271339 
 *          [FieldAssist] Add CC text field content assist doesn't work as expected when narrowing suggestions
 *******************************************************************************/

package org.eclipse.ui.tests.fieldassist;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;

/**
 * Tests the Operations Framework API.
 * 
 * @since 3.1
 */
public class FieldAssistAPITest extends TestCase {

	private Dialog dialog;

	public FieldAssistAPITest() {
		super();
	}

	/**
	 * @param testName
	 */
	public FieldAssistAPITest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
		}
		super.tearDown();
	}

	public void testFieldDecorationRegistry() {
		int originalMaxHeight = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight();
		int originalMaxWidth = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		// System.out.println(new Rectangle(0, 0, originalMaxWidth,
		// originalMaxHeight));
		Image imageLarge = IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ)
				.createImage();
		// System.out.println(imageLarge.getBounds());
		// This image is known to be larger than the default images
		// Test that the maximum increases
		FieldDecorationRegistry.getDefault().registerFieldDecoration("TESTID",
				"Test image", imageLarge);
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight() == imageLarge.getBounds().height);
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth() == imageLarge.getBounds().width);

		// This image is known to be smaller. Test that the maximum decreases
		Image imageSmall = IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_DLCL_QUICK_FIX_DISABLED)
				.createImage();
		// System.out.println(imageSmall.getBounds());
		FieldDecorationRegistry.getDefault().registerFieldDecoration("TESTID",
				"Test image", imageSmall);
		int currentMaxHeight = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight();
		assertTrue(currentMaxHeight < imageLarge.getBounds().height);
		int currentMaxWidth = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		assertTrue(currentMaxWidth < imageLarge.getBounds().width);

		// Registering another small one shouldn't change things
		FieldDecorationRegistry.getDefault().registerFieldDecoration("TESTID2",
				"Test image",
				"org.eclipse.jface.fieldassist.IMG_DEC_FIELD_CONTENT_PROPOSAL");
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight() == currentMaxHeight);
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth() == currentMaxWidth);

		// After we unregister the new decoration2, the maximums should be their
		// original values.
		FieldDecorationRegistry.getDefault()
				.unregisterFieldDecoration("TESTID");
		FieldDecorationRegistry.getDefault().unregisterFieldDecoration(
				"TESTID2");
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight() == originalMaxHeight);
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth() == originalMaxWidth);

	}

	/**
	 * Tests that a ContentAssistCommandAdapter that has no autoactivation
	 * characters set will not have its proposals disappear when a user invokes
	 * content assist and then subsequently inserts a character that matches the
	 * first character of a suggested proposal.
	 * <p>
	 * <ol>
	 * <li>User invokes content assist</li>
	 * <li>"one", "two", "three"...shows up</li>
	 * <li>User hits the 'O' key</li>
	 * <li>The list shows up (the bug was reporting that the list disappeared)</li>
	 * </ol>
	 * 
	 * @see org.eclipse.jface.tests.fieldassist.FieldAssistAPITest
	 */
	public void testBug271339EmptyAutoActivationCharacters() throws Exception {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		Display display = shell.getDisplay();

		// record the number of shells we have up and active
		int shellCount = display.getShells().length;

		// the text control of our dialog
		final Text[] textField = { null };

		dialog = new MessageDialog(shell, null, null, null,
				MessageDialog.INFORMATION, new String[] { "OK" }, 0) {
			protected Control createCustomArea(Composite parent) {
				String[] proposals = new String[] { "one", "two", "three",
						"four", "five", "six", "seven", "eight", "nine", "ten" };

				Composite container = new Composite(parent, SWT.NULL);
				GridDataFactory.fillDefaults().grab(true, true).applyTo(
						container);
				GridLayoutFactory.swtDefaults().numColumns(2)
						.applyTo(container);

				Label label = new Label(container, SWT.NULL);
				label.setText("Test Content Assist bug 271339");

				textField[0] = new Text(container, SWT.FLAT);
				SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(
						proposals);
				proposalProvider.setFiltering(true);

				// use an empty character array because no characters should
				// prompt for autoactivation
				ContentAssistCommandAdapter adapter = new ContentAssistCommandAdapter(
						textField[0], new TextContentAdapter(),
						proposalProvider, null, new char[0], true);
				adapter
						.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

				GridDataFactory.fillDefaults().grab(true, false).applyTo(
						textField[0]);

				return container;
			}
		};

		// we don't want to block the tests
		dialog.setBlockOnOpen(false);
		dialog.open();

		// grant the text field focus
		textField[0].setFocus();
		// spin the event loop to make sure the text field gets focus
		while (display.readAndDispatch())
			;

		// retrieve the content assist handler and run it
		IHandlerService handlerService = (IHandlerService) workbench
				.getService(IHandlerService.class);
		handlerService.executeCommand(
				IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST, null);

		assertEquals(
				"There should be two more shells up, the dialog and the proposals dialog",
				shellCount + 2, display.getShells().length);

		// fake a KeyDown event
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = 'o';
		textField[0].notifyListeners(SWT.KeyDown, event);

		// now we insert the character 'o', this will send out a Modify event
		textField[0].insert("o"); //$NON-NLS-1$

		assertEquals(
				"There should still be two more shells up, the dialog and the proposals dialog",
				shellCount + 2, display.getShells().length);

		// spin the event loop again because we have some asyncExec calls in the
		// ContentProposalAdapter class
		while (display.readAndDispatch())
			;

		// clean-up
		dialog.close();
	}

	/**
	 * Tests that a ContentAssistCommandAdapter that has no autoactivation
	 * characters set will not have its proposals appear when a user inserts a
	 * character that matches the first character of a suggested proposal.
	 * <p>
	 * <ol>
	 * <li>User hits the 'O' key</li>
	 * <li>While "one" matches, the proposals should not appear as no
	 * autoactivation characters have been set</li>
	 * </ol>
	 * 
	 * @see org.eclipse.jface.tests.fieldassist.FieldAssistAPITest
	 */
	public void testBug271339EmptyAutoActivationCharacters2() throws Exception {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		Display display = shell.getDisplay();

		// record the number of shells we have up and active
		int shellCount = display.getShells().length;

		// the text control of our dialog
		final Text[] textField = { null };

		dialog = new MessageDialog(shell, null, null, null,
				MessageDialog.INFORMATION, new String[] { "OK" }, 0) {
			protected Control createCustomArea(Composite parent) {
				String[] proposals = new String[] { "one", "two", "three",
						"four", "five", "six", "seven", "eight", "nine", "ten" };

				Composite container = new Composite(parent, SWT.NULL);
				GridDataFactory.fillDefaults().grab(true, true).applyTo(
						container);
				GridLayoutFactory.swtDefaults().numColumns(2)
						.applyTo(container);

				Label label = new Label(container, SWT.NULL);
				label.setText("Test Content Assist bug 271339");

				textField[0] = new Text(container, SWT.FLAT);
				SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(
						proposals);
				proposalProvider.setFiltering(true);

				// use an empty character array because no characters should
				// prompt for autoactivation
				ContentAssistCommandAdapter adapter = new ContentAssistCommandAdapter(
						textField[0], new TextContentAdapter(),
						proposalProvider, null, new char[0], true);
				adapter
						.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

				GridDataFactory.fillDefaults().grab(true, false).applyTo(
						textField[0]);

				return container;
			}
		};

		// we don't want to block the tests
		dialog.setBlockOnOpen(false);
		dialog.open();

		// grant the text field focus
		textField[0].setFocus();
		// spin the event loop to make sure the text field gets focus
		while (display.readAndDispatch())
			;

		// fake a KeyDown event
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = 'o';
		textField[0].notifyListeners(SWT.KeyDown, event);

		// now we insert the character 'o', this will send out a Modify event
		textField[0].insert("o"); //$NON-NLS-1$

		// we have no autoactivation characters, the proposals should not appear
		assertEquals(
				"There should only be one more extra shell, the dialog itself",
				shellCount + 1, display.getShells().length);

		// spin the event loop again because we have some asyncExec calls in the
		// ContentProposalAdapter class
		while (display.readAndDispatch())
			;

		// clean-up
		dialog.close();
	}

	/**
	 * Tests that a ContentAssistCommandAdapter that has no autoactivation
	 * characters set will stay open if the user backspaces over a narrowing
	 * proposal character.
	 * <p>
	 * <ol>
	 * <li>User invokes content assist</li>
	 * <li>"one", "two", "three"...shows up</li>
	 * <li>User hits the 'O' key</li>
	 * <li>The list narrows</li>
	 * <li>user hits backspace</li>
	 * <li>the popup should remain open</li>
	 * </ol>
	 * 
	 * @see org.eclipse.jface.tests.fieldassist.FieldAssistAPITest
	 */
	public void testBug271339EmptyAutoActivationCharacters3() throws Exception {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		Display display = shell.getDisplay();

		// record the number of shells we have up and active
		int shellCount = display.getShells().length;

		// the text control of our dialog
		final Text[] textField = { null };

		dialog = new MessageDialog(shell, null, null, null,
				MessageDialog.INFORMATION, new String[] { "OK" }, 0) {
			protected Control createCustomArea(Composite parent) {
				String[] proposals = new String[] { "one", "two", "three",
						"four", "five", "six", "seven", "eight", "nine", "ten" };

				Composite container = new Composite(parent, SWT.NULL);
				GridDataFactory.fillDefaults().grab(true, true).applyTo(
						container);
				GridLayoutFactory.swtDefaults().numColumns(2)
						.applyTo(container);

				Label label = new Label(container, SWT.NULL);
				label.setText("Test Content Assist bug 271339");

				textField[0] = new Text(container, SWT.FLAT);
				SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(
						proposals);
				proposalProvider.setFiltering(true);

				// use an empty character array because no characters should
				// prompt for autoactivation
				ContentAssistCommandAdapter adapter = new ContentAssistCommandAdapter(
						textField[0], new TextContentAdapter(),
						proposalProvider, null, new char[0], true);
				adapter
						.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

				GridDataFactory.fillDefaults().grab(true, false).applyTo(
						textField[0]);

				return container;
			}
		};

		// we don't want to block the tests
		dialog.setBlockOnOpen(false);
		dialog.open();

		// grant the text field focus
		textField[0].setFocus();
		// spin the event loop to make sure the text field gets focus
		while (display.readAndDispatch())
			;

		// retrieve the content assist handler and run it
		IHandlerService handlerService = (IHandlerService) workbench
				.getService(IHandlerService.class);
		handlerService.executeCommand(
				IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST, null);

		assertEquals(
				"There should be two more shells up, the dialog and the proposals dialog",
				shellCount + 2, display.getShells().length);

		// fake a KeyDown event
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = 'o';
		textField[0].notifyListeners(SWT.KeyDown, event);

		// now we insert the character 'o', this will send out a Modify event
		textField[0].insert("o"); //$NON-NLS-1$

		assertEquals(
				"There should still be two more shells up, the dialog and the proposals dialog",
				shellCount + 2, display.getShells().length);
		
		// fake a Backspace
		event = new Event();
		event.type = SWT.KeyDown;
		event.character = SWT.BS;
		textField[0].notifyListeners(SWT.KeyDown, event);
		
		// now we remove the o, this will trigger a modify
		textField[0].setText("");  //$NON-NLS-1$
	
		assertEquals(
				"There should still be two more shells up, the dialog and the proposals dialog",
				shellCount + 2, display.getShells().length);

		// spin the event loop again because we have some asyncExec calls in the
		// ContentProposalAdapter class
		while (display.readAndDispatch())
			;

		// clean-up
		dialog.close();
	}
}
