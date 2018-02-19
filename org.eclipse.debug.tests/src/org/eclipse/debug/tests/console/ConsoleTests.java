/*******************************************************************************
 * Copyright (c) 2017, 2018 Andreas Loth and others.
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

import org.eclipse.debug.tests.AbstractDebugTest;
import org.eclipse.debug.tests.TestUtil;

import junit.framework.TestCase;


public class ConsoleTests extends AbstractDebugTest {

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
		TestUtil.waitForJobs(getName(), 200, 5000);
		TestCase.assertEquals("Document should be empty", "", document.get()); //$NON-NLS-1$ //$NON-NLS-2$
		try (IOConsoleOutputStream outStream = console.newOutputStream()) {
			outStream.write(testStringBuffer, 0, 6);
			// half of Ã¶ (\u00f6) is written so we don't expect this char in
			// output but all previous chars can be decoded
			TestUtil.waitForJobs(getName(), 200, 5000);
			TestCase.assertEquals("First 4 chars should be written", testString.substring(0, 4), document.get()); //$NON-NLS-1$
			outStream.write(testStringBuffer, 6, 6);
			// all remaining bytes are written so we expect the whole string
			// including the Ã¶ (\u00f6) which was at buffer boundary
			TestUtil.waitForJobs(getName(), 200, 5000);
			TestCase.assertEquals("whole test string should be written", testString, document.get()); //$NON-NLS-1$
		}
		TestUtil.waitForJobs(getName(), 200, 5000);
		// after closing the stream, the document content should still be the
		// same
		TestCase.assertEquals("closing the stream should not alter the document", testString, document.get()); //$NON-NLS-1$
	}

	public void testConsoleOutputStreamLastR() throws IOException, InterruptedException {
		String testString = "a\r"; //$NON-NLS-1$
		byte[] testStringBuffer = testString.getBytes(StandardCharsets.UTF_8);
		TestCase.assertEquals("Test string \"" + testString + "\" should consist of 2 UTF-8 bytes", 2, testStringBuffer.length); //$NON-NLS-1$ //$NON-NLS-2$
		MessageConsole console = new MessageConsole("Test Console 2", //$NON-NLS-1$
				IConsoleConstants.MESSAGE_CONSOLE_TYPE, null, StandardCharsets.UTF_8.name(), true);
		IDocument document = console.getDocument();
		TestUtil.waitForJobs(getName(), 200, 5000);
		TestCase.assertEquals("Document should be empty", "", document.get()); //$NON-NLS-1$ //$NON-NLS-2$
		try (IOConsoleOutputStream outStream = console.newOutputStream()) {
			outStream.write(testStringBuffer);
			// everything but pending \r should be written
			TestUtil.waitForJobs(getName(), 200, 5000);
			TestCase.assertEquals("First char should be written", testString.substring(0, 1), document.get()); //$NON-NLS-1$
		}
		TestUtil.waitForJobs(getName(), 200, 5000);
		// after closing the stream, the document content should still be the
		// same
		TestCase.assertEquals("closing the stream should write the pending \\r", testString, document.get()); //$NON-NLS-1$
	}

	public void testConsoleOutputStreamDocumentClosed() throws IOException {
		MessageConsole console = new MessageConsole("Test Console 3", //$NON-NLS-1$
				IConsoleConstants.MESSAGE_CONSOLE_TYPE, null, StandardCharsets.UTF_8.name(), true);
		IDocument document = console.getDocument();
		try (IOConsoleOutputStream outStream = console.newOutputStream()) {
			outStream.write("write1"); //$NON-NLS-1$
			document.getDocumentPartitioner().disconnect();
			try {
				outStream.write("write2"); //$NON-NLS-1$
				TestCase.fail("IOException with message \"Document is closed\" expected"); //$NON-NLS-1$
			} catch (IOException ioe) {
				TestCase.assertEquals("Document is closed", ioe.getMessage()); //$NON-NLS-1$
			}
		}
	}

	public void testConsoleOutputStreamClosed() throws IOException {
		MessageConsole console = new MessageConsole("Test Console 4", //$NON-NLS-1$
				IConsoleConstants.MESSAGE_CONSOLE_TYPE, null, StandardCharsets.UTF_8.name(), true);
		try (IOConsoleOutputStream outStream = console.newOutputStream()) {
			outStream.write("test1".getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$
			outStream.close();
			try {
				outStream.write("test2".getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$
				TestCase.fail("IOException with message \"Output Stream is closed\" expected"); //$NON-NLS-1$
			} catch (IOException ioe) {
				TestCase.assertEquals("Output Stream is closed", ioe.getMessage()); //$NON-NLS-1$
			}
		}
	}

	public void testConsoleOutputStreamDocumentStreamClosed() throws IOException {
		MessageConsole console = new MessageConsole("Test Console 5", //$NON-NLS-1$
				IConsoleConstants.MESSAGE_CONSOLE_TYPE, null, StandardCharsets.UTF_8.name(), true);
		IDocument document = console.getDocument();
		try (IOConsoleOutputStream outStream = console.newOutputStream()) {
			outStream.write("write1"); //$NON-NLS-1$
			document.getDocumentPartitioner().disconnect();
			try {
				outStream.write("write2"); //$NON-NLS-1$
				TestCase.fail("IOException with message \"Document is closed\" expected"); //$NON-NLS-1$
			} catch (IOException ioe) {
				TestCase.assertEquals("Document is closed", ioe.getMessage()); //$NON-NLS-1$
			}
			try {
				outStream.write("write3"); //$NON-NLS-1$
				TestCase.fail("IOException with message \"Output Stream is closed\" expected"); //$NON-NLS-1$
			} catch (IOException ioe) {
				TestCase.assertEquals("Output Stream is closed", ioe.getMessage()); //$NON-NLS-1$
			}
		}
	}

	public void testSetNullEncoding() throws IOException {
		MessageConsole console = new MessageConsole("Test Console 6", null); //$NON-NLS-1$
		try (IOConsoleOutputStream outStream = console.newOutputStream()) {
			outStream.setEncoding(null);
		}
	}

}
