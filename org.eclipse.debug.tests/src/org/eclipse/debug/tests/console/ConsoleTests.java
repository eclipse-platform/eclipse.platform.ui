/*******************************************************************************
 * Copyright (c) 2017 Andreas Loth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Loth - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.tests.console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;

import junit.framework.TestCase;


public class ConsoleTests extends TestCase {

	public ConsoleTests() {
		super("ConsoleTests"); //$NON-NLS-1$
	}

	public ConsoleTests(String name) {
		super(name);
	}

	public void testConsoleOutputStreamEncoding() throws IOException, InterruptedException {
		String testString = "abc\u00e4\u00f6\u00fcdef"; //$NON-NLS-1$
		// abcdef need 1 byte in UTF-8 each
		// Ã¤Ã¶Ã¼ (\u00e4\u00f6\u00fc) need 2 bytes each
		byte[] testStringBuffer = testString.getBytes(StandardCharsets.UTF_8);
		TestCase.assertEquals("Test string \"" + testString + "\" should consist of 12 UTF-8 bytes", 12, testStringBuffer.length); //$NON-NLS-1$ //$NON-NLS-2$
		MessageConsole console = new MessageConsole("Test Console", //$NON-NLS-1$
				IConsoleConstants.MESSAGE_CONSOLE_TYPE, null, StandardCharsets.UTF_8.name(), true);
		IDocument document = console.getDocument();
		TestHelper.waitForJobs();
		TestCase.assertEquals("Document should be empty", "", document.get()); //$NON-NLS-1$ //$NON-NLS-2$
		try (IOConsoleOutputStream outStream = console.newOutputStream()) {
			outStream.write(testStringBuffer, 0, 6);
			// half of Ã¶ (\u00f6) is written so we don't expect this char in
			// output but all previous chars can be decoded
			TestHelper.waitForJobs();
			TestCase.assertEquals("First 4 chars should be written", testString.substring(0, 4), document.get()); //$NON-NLS-1$
			outStream.write(testStringBuffer, 6, 6);
			// all remaining bytes are written so we expect the whole string
			// including the Ã¶ (\u00f6) which was at buffer boundary
			TestHelper.waitForJobs();
			TestCase.assertEquals("whole test string should be written", testString, document.get()); //$NON-NLS-1$
		}
	}

}
