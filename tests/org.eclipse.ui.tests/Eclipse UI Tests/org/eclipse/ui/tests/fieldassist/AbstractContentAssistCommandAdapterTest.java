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

package org.eclipse.ui.tests.fieldassist;

import org.eclipse.jface.tests.fieldassist.AbstractFieldAssistTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

public abstract class AbstractContentAssistCommandAdapterTest extends
		AbstractFieldAssistTestCase {

	protected void executeContentAssistHandler() throws Exception {
		// retrieve the content assist handler and run it
		IHandlerService handlerService = PlatformUI
				.getWorkbench().getService(IHandlerService.class);
		handlerService.executeCommand(
				IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST, null);
	}

	public void testHandlerPromptsPopup() throws Exception {
		getFieldAssistWindow().open();

		sendFocusInToControl();
		executeContentAssistHandler();

		assertTwoShellsUp();
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
	 */
	public void testBug271339EmptyAutoActivationCharacters() throws Exception {
		getFieldAssistWindow().open();

		sendFocusInToControl();
		executeContentAssistHandler();

		assertTwoShellsUp();

		sendKeyDownToControl('o');
		assertTwoShellsUp();
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
	 */
	public void testBug271339EmptyAutoActivationCharacters2() throws Exception {
		getFieldAssistWindow().open();

		sendFocusInToControl();
		sendKeyDownToControl('o');

		assertOneShellUp();
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
	 */
	public void testBug271339EmptyAutoActivationCharacters3() throws Exception {
		getFieldAssistWindow().open();

		sendFocusInToControl();
		executeContentAssistHandler();

		assertTwoShellsUp();

		sendKeyDownToControl('o');
		assertTwoShellsUp();

		sendKeyDownToControl(SWT.BS);
		assertTwoShellsUp();
	}
}
